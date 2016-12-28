package com.mymkf.lframe.test;

import android.app.Activity;
import android.os.Bundle;

import com.mymkf.lframe.R;
import com.mymkf.lframe.tools.StatusBarCompat;
import com.mymkf.lframe.view.HorizontalColorView;

/**
 * Created by mymkf on 2016/12/28.
 */
public class TestActivity extends Activity implements HorizontalColorView.OnColorChangedListener {

    private HorizontalColorView mCtrlView;
    private HorizontalColorView mPassiveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        StatusBarCompat.translucentStatusBar(this);

        initView();
    }

    private void initView() {
        mCtrlView = (HorizontalColorView) findViewById(R.id.hcv_test_ctrl);
        mPassiveView = (HorizontalColorView) findViewById(R.id.hcv_test_passive);

        mCtrlView.setOnColorChangedListener(this);
    }

    /**
     * 颜色改变时的回调.
     *
     * @param color 被选中的颜色.
     * @param x     手指在屏幕内x坐标.
     */
    @Override
    public void onColorChanged(int color, float x) {

    }

    /**
     * 手指离开选色游标时回调.
     *
     * @param color 被选中的颜色.
     */
    @Override
    public void onColorSelected(int color) {
        mPassiveView.setColor(color);
    }
}
