# CircleSliceImageView

<p align="center"><img src="/banner.jpg"></p>

=================

<img src="/preview/preview.gif" alt="sample" title="sample" width="300" height="435" align="right" vspace="52" />

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![](https://jitpack.io/v/hantrungkien/CircleSliceImageView.svg)](https://jitpack.io/#hantrungkien/CircleSliceImageView)

This library was developed and extended based on https://github.com/lopspower/CircularImageView

This is an Android project allowing to realize a circular and slice ImageView in the simplest way possible.

USAGE
-----

To make a circular ImageView add CircularImageView in your layout XML and add CircularImageView library in your project or you can also grab it via Gradle:

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
dependencies {
	        implementation 'com.github.hantrungkien:CircleSliceImageView:v1.0.0'
	}

```

XML
-----

```xml
 <com.kienht.csiv.CircleSliceImageView
            android:id="@+id/circleSliceImageView"
            android:layout_width="200dp"
            android:layout_height="200dp"
            android:src="@drawable/logo" />
```

You must use the following properties in your XML to change your CircularImageView.


##### Properties:

* `app:civ_border`              (boolean)   -> default true
* `app:civ_border_color`        (color)     -> default WHITE
* `app:civ_border_width`        (dimension) -> default 4dp
* `app:civ_background_color`    (color) -> default WHITE
* `app:civ_shadow`              (boolean)   -> default false
* `app:civ_shadow_color`        (color)     -> default BLACK
* `app:civ_shadow_radius`       (float)     -> default 8.0f
* `app:civ_shadow_gravity`      (center, top, bottom, start or end) -> default bottom

JAVA
-----

```java
CircularImageView circularImageView = (CircularImageView)findViewById(R.id.yourCircularImageView);
// Set Border
circularImageView.setBorderColor(getResources().getColor(R.color.GrayLight));
circularImageView.setBorderWidth(10);
// Add Shadow with default param
circularImageView.addShadow();
// or with custom param
circularImageView.setShadowRadius(15);
circularImageView.setShadowColor(Color.RED);
circularImageView.setBackgroundColor(Color.RED);
circularImageView.setShadowGravity(CircularImageView.ShadowGravity.CENTER);
```

LIMITATIONS
-----

* By default the ScaleType is CENTER_CROP. You can also used CENTER_INSIDE but the others one are not supported.
* Enabling adjustViewBounds is not supported as this requires an unsupported ScaleType.

LINK
-----

A special thanks go to [Mr. Igalata](https://github.com/lopspower)

LICENCE
-----

CircularImageView by [Lopez Mikhael](http://mikhaellopez.com/) is licensed under a [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
