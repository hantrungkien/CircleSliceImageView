package com.kienht.csiv

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import java.security.SecureRandom

/**
 * @author kienht
 * @since 23/07/2018
 */
class CircleSliceImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    companion object {
        @JvmField
        var TAG: String = CircleSliceImageView::class.java.simpleName

        const val DEFAULT_BORDER_WIDTH = 4f
        const val DEFAULT_SLICE_BORDER_WIDTH = 4f
        const val DEFAULT_SHADOW_RADIUS = 8.0f
    }

    private val defaultBorderSize = DEFAULT_BORDER_WIDTH * getContext().resources.displayMetrics.density
    var borderWidth: Float? = defaultBorderSize
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private var canvasSize: Int? = null

    var shadowRadius: Float? = 0f
        set(value) {
            field = value
            invalidate()
        }

    var shadowColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var shadowGravity = ShadowGravity.BOTTOM
        set(value) {
            field = value
            invalidate()
        }

    var isShadowEnable = false
        set(value) {
            field = value
            shadowRadius = DEFAULT_SHADOW_RADIUS
            drawShadow(shadowRadius!!, shadowColor)
            shadowGravity = shadowGravityFromValue(ShadowGravity.BOTTOM.getValue())
        }

    private var customColorFilter: ColorFilter? = null
    private var customDrawable: Drawable? = null

    private var image: Bitmap? = null
    private var paint: Paint? = null
    private var paintBorder: Paint? = null
    private var paintBackground: Paint? = null
    private var paintSliceBorder: Paint? = null

    private val mSliceRect = RectF()

    private val defSliceBorderSize = DEFAULT_SLICE_BORDER_WIDTH * getContext().resources.displayMetrics.density
    var sliceBorderWidth: Float = defSliceBorderSize
        set(value) {
            field = value
            paintSliceBorder!!.strokeWidth = value
            requestLayout()
            invalidate()
        }

    var sliceStartAngle = 0
        set(value) {
            field = value
            invalidate()
        }

    var sliceSections = 2
        set(value) {
            field = value
            fullArcSliceLength = 360 / value
            colorArcLineLength = fullArcSliceLength - 2 * (fullArcSliceLength / 10)
            invalidate()
        }

    private var fullArcSliceLength: Int = 0
    private var colorArcLineLength: Int = 0

    var isSliceRandomColor = false
        set(value) {
            field = value
            invalidate()
        }

    var isSpaceSliceWithImage = true
        set(value) {
            field = value
            invalidate()
        }

    var mode = 0
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    init {
        paint = Paint()
        paint!!.isAntiAlias = true

        paintBorder = Paint()
        paintBorder!!.isAntiAlias = true

        paintSliceBorder = Paint()
        paintSliceBorder!!.isAntiAlias = true
        paintSliceBorder!!.strokeCap = Paint.Cap.ROUND
        paintSliceBorder!!.style = Paint.Style.STROKE
        paintSliceBorder!!.strokeWidth = defSliceBorderSize

        paintBackground = Paint()
        paintBackground!!.isAntiAlias = true
        paintBackground!!.style = Paint.Style.FILL_AND_STROKE

        sliceSections = 2

        val attributes = context!!.obtainStyledAttributes(attrs, R.styleable.CircleSliceImageView)

        mode = attributes.getInteger(R.styleable.CircleSliceImageView_civ_mode, Mode.DEFAULT_MODE.getValue())

        when (mode) {
            Mode.DEFAULT_MODE.getValue() -> {

            }
            Mode.SLICE_MODE.getValue() -> {
                sliceSections = attributes.getInteger(R.styleable.CircleSliceImageView_civ_slice_sections, 2)
                sliceStartAngle = attributes.getInteger(R.styleable.CircleSliceImageView_civ_slice_start_angle, 0)
                isSliceRandomColor = attributes.getBoolean(R.styleable.CircleSliceImageView_civ_slice_random_color, false)
                isSpaceSliceWithImage = attributes.getBoolean(R.styleable.CircleSliceImageView_civ_slice_space_with_image, false)
                sliceBorderWidth = attributes.getDimension(R.styleable.CircleSliceImageView_civ_slice_border_width, defSliceBorderSize)
                setSliceBorderColor(attributes.getColor(R.styleable.CircleSliceImageView_civ_slice_border_color, Color.BLACK))
            }
            Mode.BORDER_MODE.getValue() -> {
                borderWidth = attributes.getDimension(R.styleable.CircleSliceImageView_civ_border_width, defaultBorderSize)
                setBorderColor(attributes.getColor(R.styleable.CircleSliceImageView_civ_border_color, Color.BLACK))

                if (attributes.getBoolean(R.styleable.CircleSliceImageView_civ_shadow, false)) {
                    shadowRadius = DEFAULT_SHADOW_RADIUS
                    drawShadow(attributes.getFloat(R.styleable.CircleSliceImageView_civ_shadow_radius, shadowRadius!!),
                            attributes.getColor(R.styleable.CircleSliceImageView_civ_shadow_color, shadowColor))
                    val shadowGravityIntValue = attributes.getInteger(R.styleable.CircleSliceImageView_civ_shadow_gravity, ShadowGravity.BOTTOM.getValue())
                    shadowGravity = shadowGravityFromValue(shadowGravityIntValue)
                }
            }
        }

        setBackgroundColor(attributes.getColor(R.styleable.CircleSliceImageView_civ_background_color, Color.WHITE))

        attributes.recycle()
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        if (this.customColorFilter === colorFilter)
            return
        this.customColorFilter = colorFilter
        customDrawable = null // To force re-update shader
        invalidate()
    }

    override fun getScaleType(): ImageView.ScaleType {
        val currentScaleType = super.getScaleType()
        return if (currentScaleType == null || currentScaleType != ImageView.ScaleType.CENTER_INSIDE) {
            ImageView.ScaleType.CENTER_CROP
        } else {
            currentScaleType
        }
    }

    override fun setScaleType(scaleType: ImageView.ScaleType) {
        if (scaleType != ImageView.ScaleType.CENTER_CROP && scaleType != ImageView.ScaleType.CENTER_INSIDE) {
            throw IllegalArgumentException(String.format("ScaleType %s not supported. " + "Just ScaleType.CENTER_CROP & ScaleType.CENTER_INSIDE are available for this library.", scaleType))
        } else {
            super.setScaleType(scaleType)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureWidth(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)

        setMeasuredDimension(width, height)

        mSliceRect.left = sliceBorderWidth / 2
        mSliceRect.right = width.toFloat() - sliceBorderWidth / 2
        mSliceRect.top = sliceBorderWidth / 2
        mSliceRect.bottom = height.toFloat() - sliceBorderWidth / 2
    }

    public override fun onDraw(canvas: Canvas) {
        // Load the bitmap
        loadBitmap()

        // Check if image isn't null
        if (image == null)
            return

        if (!isInEditMode) {
            canvasSize = Math.min(canvas.width, canvas.height)
        }

        var circleCenter = 0
        when (mode) {
            Mode.DEFAULT_MODE.getValue() -> {
                circleCenter = canvasSize!! / 2
                canvas.drawCircle(circleCenter.toFloat(), circleCenter.toFloat(), circleCenter.toFloat(), paintBackground)

                canvas.drawCircle(circleCenter.toFloat(), circleCenter.toFloat(), circleCenter.toFloat(), paint)
            }
            Mode.SLICE_MODE.getValue() -> {
                circleCenter = canvasSize!! / 2
                for (i in 0 until sliceSections) {
                    if (isSliceRandomColor) {
                        paintSliceBorder!!.color = getRandomColor()
                    }
                    canvas.drawArc(mSliceRect, (i * fullArcSliceLength + sliceStartAngle).toFloat(), colorArcLineLength.toFloat(), false, paintSliceBorder)
                }

                val radius: Float = if (isSpaceSliceWithImage) {
                    circleCenter.toFloat() - sliceBorderWidth - sliceBorderWidth / 2
                } else {
                    circleCenter.toFloat() - sliceBorderWidth
                }

                canvas.drawCircle(circleCenter.toFloat(), circleCenter.toFloat(), radius, paintBackground)

                canvas.drawCircle(circleCenter.toFloat(), circleCenter.toFloat(), radius, paint)
            }
            Mode.BORDER_MODE.getValue() -> {
                circleCenter = (canvasSize!! - borderWidth!! * 2).toInt() / 2

                val margeWithShadowRadius = shadowRadius!! * 2

                canvas.drawCircle(circleCenter + borderWidth!!, circleCenter + borderWidth!!,
                        circleCenter + borderWidth!! - margeWithShadowRadius, paintBorder)

                canvas.drawCircle(circleCenter + borderWidth!!, circleCenter + borderWidth!!,
                        circleCenter - margeWithShadowRadius, paintBackground)

                canvas.drawCircle(circleCenter + borderWidth!!, circleCenter + borderWidth!!,
                        circleCenter - margeWithShadowRadius, paint)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasSize = Math.min(w, h)
        if (image != null) {
            updateShader()
        }
    }

    public fun addShadow() {
        if (shadowRadius == 0f)
            shadowRadius = DEFAULT_SHADOW_RADIUS
        drawShadow(shadowRadius!!, shadowColor)
        invalidate()
    }

    public override fun setBackgroundColor(backgroundColor: Int) {
        paintBackground!!.color = backgroundColor
        invalidate()
    }

    public fun setBorderColor(borderColor: Int) {
        paintBorder!!.color = borderColor
        invalidate()
    }

    public fun setSliceBorderColor(sliceBorderColor: Int) {
        paintSliceBorder!!.color = sliceBorderColor
        invalidate()
    }

    private fun loadBitmap() {
        if (customDrawable == drawable) {
            return
        }

        customDrawable = drawable
        image = drawableToBitmap(drawable)
        updateShader()
    }

    private fun drawShadow(shadowRadius: Float, shadowColor: Int) {
        this.shadowRadius = shadowRadius
        this.shadowColor = shadowColor
        setLayerType(View.LAYER_TYPE_SOFTWARE, paintBorder)

        var dx = 0.0f
        var dy = 0.0f

        when (shadowGravity) {
            ShadowGravity.CENTER -> {
                dx = 0.0f
                dy = 0.0f
            }
            ShadowGravity.TOP -> {
                dx = 0.0f
                dy = -shadowRadius / 2
            }
            ShadowGravity.BOTTOM -> {
                dx = 0.0f
                dy = shadowRadius / 2
            }
            ShadowGravity.START -> {
                dx = -shadowRadius / 2
                dy = 0.0f
            }
            ShadowGravity.END -> {
                dx = shadowRadius / 2
                dy = 0.0f
            }
        }

        paintBorder!!.setShadowLayer(shadowRadius, dx, dy, shadowColor)
    }

    private fun updateShader() {
        if (image == null)
            return

        // Create Shader
        val shader = BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        // Center Image in Shader
        var scale = 0f
        var dx = 0f
        var dy = 0f

        when (scaleType) {
            ImageView.ScaleType.CENTER_CROP -> if (image!!.width * height > width * image!!.height) {
                scale = height / image!!.height.toFloat()
                dx = (width - image!!.width * scale) * 0.5f
            } else {
                scale = width / image!!.width.toFloat()
                dy = (height - image!!.height * scale) * 0.5f
            }
            ImageView.ScaleType.CENTER_INSIDE -> if (image!!.width * height < width * image!!.height) {
                scale = height / image!!.height.toFloat()
                dx = (width - image!!.width * scale) * 0.5f
            } else {
                scale = width / image!!.width.toFloat()
                dy = (height - image!!.height * scale) * 0.5f
            }
        }

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        shader.setLocalMatrix(matrix)

        // Set Shader in Paint
        paint!!.shader = shader

        // Apply colorFilter
        paint!!.colorFilter = customColorFilter
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        } else if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            // Create Bitmap object out of the drawable
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    private fun measureWidth(measureSpec: Int): Int {
        val result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize
        } else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize!!
        }

        return result
    }

    private fun measureHeight(measureSpecHeight: Int): Int {
        val result: Int
        val specMode = View.MeasureSpec.getMode(measureSpecHeight)
        val specSize = View.MeasureSpec.getSize(measureSpecHeight)

        if (specMode == View.MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize!!
        }

        return result
    }

    private fun getRandomColor(): Int {
        val secureRandom = SecureRandom()
        return Color.HSVToColor(150, floatArrayOf(secureRandom.nextInt(359).toFloat(), 1f, 1f))
    }

    private fun shadowGravityFromValue(value: Int): ShadowGravity {
        return when (value) {
            1 -> ShadowGravity.CENTER
            2 -> ShadowGravity.TOP
            3 -> ShadowGravity.BOTTOM
            4 -> ShadowGravity.START
            5 -> ShadowGravity.END
            else -> {
                throw IllegalArgumentException("This value is not supported for ShadowGravity: $value")
            }
        }
    }

    enum class ShadowGravity {
        CENTER, TOP, BOTTOM, START, END;

        fun getValue(): Int {
            return when (this) {
                CENTER -> 1
                TOP -> 2
                BOTTOM -> 3
                START -> 4
                END -> 5
                else -> {
                    throw IllegalArgumentException("Not value available for this ShadowGravity: " + this)
                }
            }
        }
    }

    enum class Mode {
        DEFAULT_MODE, SLICE_MODE, BORDER_MODE;

        fun getValue(): Int {
            return when (this) {
                DEFAULT_MODE -> 0
                SLICE_MODE -> 1
                BORDER_MODE -> 2
                else -> {
                    throw IllegalArgumentException("Not value available for this CircularImageMode: " + this)
                }
            }
        }
    }
}