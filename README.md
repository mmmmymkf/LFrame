# LFrame
* 现有功能:<br/>
  * RGB水平颜色选择View.

## Effect
  * Gif
  ![](https://github.com/mmmmymkf/LFrame/screenshot/ColorView.gif)
<br/>
  * Image
  ![](https://github.com/mmmmymkf/LFrame/screenshot/ColorView.jpg)
<br/>
## Reference

```Java
    <com.mymkf.lframe.view.HorizontalColorView
        android:id="@+id/hcv_test_passive"
        android:layout_width="match_parent"
        android:layout_height="20dp"
        app:thumbRadius="15dp"
        app:thumbInnerRadius="8dp"
        app:colorBarHeight="1dp"
        app:colorMark="@mipmap/bg_color_cursor"/>
```
<br/>
```Java
    HorizontalColorView.setOnColorChangedListener(this);
<br/>
    /**
     * 颜色改变时的回调.
     *
     * @param color 被选中的颜色.
     * @param x     手指在屏幕内x坐标.
     */
    @Override
    public void onColorChanged(int color, float x) {
        ...
    }
<br/>
    /**
     * 手指离开选色游标时回调.
     *
     * @param color 被选中的颜色.
     */
    @Override
    public void onColorSelected(int color) {
        ...
    }
```
