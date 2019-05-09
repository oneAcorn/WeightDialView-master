# WeightDialView-master
<a href="https://996.icu"><img src="https://img.shields.io/badge/link-996.icu-red.svg" alt="996.icu" /></a>
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)

<h3>可触摸选择刻度,有惯性,指针自动补正的刻度盘盘</h3>

<h3>效果图</h3>

拖动,惯性,自动补正效果:

![github](https://github.com/oneAcorn/WeightDialView-master/blob/master/20190509_214921.gif)

隐藏刻度线,更改背景图,设置总刻度效果(注:图中总刻度为12,不是60,所以松手时,指针补正至5点)

![github](https://github.com/oneAcorn/WeightDialView-master/blob/master/20190509_215053.gif)

<h3>一 引用方法</h3>

1.在root build.gradle中加入

```gradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2.在项目的 build.gradle中加入

```gradle
dependencies {
	        implementation 'com.github.oneAcorn:WeightDialView-master:1.0.2'
	}
```

<h3>二 使用方法</h3>

1.在xml中声明,total_scale为总刻度

```xml
    <view
        android:id="@+id/weightdialview"
        class="com.acorn.weightdiallibrary.WeightDialView"
        android:layout_width="300dp"
        android:layout_height="300dp"
        app:total_scale="100"
        />
```

2.设置初始相关参数

```java
        weightDialView.setCircle(10); //设置当前圈数为10
        weightDialView.setScale(7);  //设置当前指向刻度为7
```



3.增加/减少总刻度

![github](https://github.com/oneAcorn/WeightDialView-master/blob/master/20190427_111527.gif)

```java
    //增加总刻度
    public void addTotalScale(View view) {
        weightDialView.setTotalScale(weightDialView.getTotalScale() + 10);
    }

    //减少总刻度
    public void reduceTotalScale(View view) {
        try {
            weightDialView.setTotalScale(weightDialView.getTotalScale() - 10);
        } catch (Exception e) {
            //总刻度不能小于0
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
```

4.增加/减少指针与圆心的距离比

![github](https://github.com/oneAcorn/WeightDialView-master/blob/master/20190427_111600.gif)

```java
    //增加指针与圆心的距离比
    public void addThumbDistance(View view) {
        try {
            //0~1,越小越靠近边缘,越大越靠近圆心
            weightDialView.setThumbDistance(weightDialView.getThumbDistance() - 0.05f);
        } catch (Exception e) {
            //distance必须大于等于0或小于等于1
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        notifyStatus();
    }

    //减少指针与圆心的距离比
    public void reduceThumbDistance(View view) {
        try {
            //0~1,越小越靠近边缘,越大越靠近圆心
            weightDialView.setThumbDistance(weightDialView.getThumbDistance() + 0.05f);
        } catch (Exception e) {
            //distance必须大于等于0或小于等于1
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        notifyStatus();
    }
```
   
5.显示/隐藏刻度线,显示/隐藏背景图

![github](https://github.com/oneAcorn/WeightDialView-master/blob/master/20190509_215053.gif)

```java
weightDialView.showScaleLine(); //显示刻度线
weightDialView.hideScaleLine(); //隐藏刻度线
```

```java
    private boolean flag = false;

    public void toggleBackground(View view) {
        if (flag) {
            weightDialView.setCircleBackground(null); //隐藏背景图
        } else {
            //显示背景图
            weightDialView.setCircleBackground(ImageUtil
                    .drawableToBitamp(getResources()
                            .getDrawable(R.mipmap.watch_dial)));
            //设置总刻度12
            weightDialView.setTotalScale(12);
        }
        bgBtn.setText(flag ? "显示背景图" : "隐藏背景图");
        flag = !flag;
    }
```

6.设置监听器

```java
    weightDialView.setOnScaleChangeListener(new WeightDialView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(int newScale, boolean isClockwise, int circles) {
                listenerTv.setText("当前刻度:" + newScale + ",圈数:" + circles + ",顺时针:" + isClockwise);
            }
        });
```
