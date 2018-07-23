package com.kienht.circlesliceimageview

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import com.kienht.csiv.CircleSliceImageView
import com.larswerkman.lobsterpicker.OnColorListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmField
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_default.setOnClickListener {
            circleSliceImageView.mode = CircleSliceImageView.CircularImageMode.DEFAULT_MODE.getValue()
        }

        button_slice.setOnClickListener {
            circleSliceImageView.mode = CircleSliceImageView.CircularImageMode.SLICE_MODE.getValue()
            circleSliceImageView.sliceSections = 5
            circleSliceImageView.sliceStartAngle = 90
        }

        button_border.setOnClickListener {
            circleSliceImageView.mode = CircleSliceImageView.CircularImageMode.BORDER_MODE.getValue()
            circleSliceImageView.isShadowEnable = true
        }

        seekBarBorderWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                circleSliceImageView.borderWidth = (progress * resources.displayMetrics.density.toInt()).toFloat()
                circleSliceImageView.sliceBorderWidth = (progress * resources.displayMetrics.density.toInt()).toFloat()

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        seekBarShadowRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                circleSliceImageView.shadowRadius = progress.toFloat()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        shadeslider.addOnColorListener(object : OnColorListener {
            override fun onColorSelected(color: Int) {
            }

            override fun onColorChanged(color: Int) {
                circleSliceImageView.setBorderColor(color)
                circleSliceImageView.shadowColor = color
                circleSliceImageView.setSliceBorderColor(color)
            }

        })
    }


}
