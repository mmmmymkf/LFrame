package com.mymkf.lframe.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.mymkf.lframe.R;

/**
 * 水平RGB选择器.
 * Created by mymkf on 2016/12/28.
 */
public class HorizontalColorView extends View {

	private static final String TAG = "HorizontalColorView";

	/** 默认游标外环半径 */
	private static final int DEFAULT_THUMB_RADIUS = 30;
	/** 默认游标内环半径 */
	private static final int DEFAULT_THUMB_INNER_RADIUS = 14;
	/** 默认色带高度 */
	private static final int DEFAULT_COLOR_BAR_HEIGHT = 2;

	/** 基色带含色值. */
	private int[] mColorField = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFE10207 };
	private float mPositions[] = null;

	/** 色带线性渐变器. */
	private LinearGradient mGridient;

	/** 色带起始Y轴位置. */
	private int mBaseColorStartY;

	private int mThumbRadius;
	private int mThumbInnerRadius;
	private int mColorBarHeight;

	private int mThumbX;
	private int mThumbY;

	private Paint mColorBarPaint;

	/** 绘制游标 */
	private Paint mThumbPaint;

	private boolean isShowMark;
	/** 当前色值提示标志 */
	private Drawable mMarkDrawable;

	private int mViewWidth;
	private int mViewHeight;

	private int mDefaultSelectColor = Color.RED;
	private int[] mLocation;

	private OnColorChangedListener mColorListener;

	public HorizontalColorView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HorizontalColorView);
		mThumbRadius = ta.getDimensionPixelOffset(R.styleable.HorizontalColorView_thumbRadius, DEFAULT_THUMB_RADIUS);
		mThumbInnerRadius = ta.getDimensionPixelOffset(R.styleable.HorizontalColorView_thumbInnerRadius, DEFAULT_THUMB_INNER_RADIUS);
		mColorBarHeight = ta.getDimensionPixelOffset(R.styleable.HorizontalColorView_colorBarHeight, DEFAULT_COLOR_BAR_HEIGHT);
		mMarkDrawable = ta.getDrawable(R.styleable.HorizontalColorView_colorMark);
		ta.recycle();
		Log.d(TAG, "offset radius: " + mThumbRadius + ", inner radius: " + mThumbInnerRadius + ", bar height: " + mColorBarHeight);

		initData();
	}

	private void initData() {
		mThumbX = mThumbRadius;

		mColorBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorBarPaint.setStyle(Style.FILL_AND_STROKE);
		mColorBarPaint.setStrokeWidth(1f);

		mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mThumbPaint.setColor(Color.GRAY);
		mThumbPaint.setStyle(Style.STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mViewHeight = getHeight();
		mViewWidth = getWidth();
		mBaseColorStartY = mViewHeight / 2 - mColorBarHeight / 2;

		mThumbY = mViewHeight / 2;

		if (mLocation == null) {
			mLocation = new int[2];
			getLocationInWindow(mLocation);
		}

		if (mGridient == null) {
			mGridient = new LinearGradient(0, 0, getWidth(), getHeight(), mColorField, mPositions, TileMode.MIRROR);
		}
		mColorBarPaint.setShader(mGridient);
		canvas.drawRect(mThumbRadius - 1, mBaseColorStartY, mViewWidth - mThumbRadius, mBaseColorStartY + mColorBarHeight, mColorBarPaint);

		drawThumb(canvas);
	}

	/**
	 * 绘制游标.
	 */
	private void drawThumb(Canvas canvas) {
		canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);

		// 外环.
		int mTorusRadius = mThumbRadius - mThumbInnerRadius;
		mThumbPaint.setColor(Color.WHITE);
		mThumbPaint.setStrokeWidth(mTorusRadius);
		canvas.drawCircle(mThumbX, mThumbY, mThumbRadius - (mTorusRadius / 2 + 1), mThumbPaint);

		// 内环当前色值.
		mThumbPaint.setColor(Color.GRAY);
		mThumbPaint.setStrokeWidth(1);
		canvas.drawCircle(mThumbX, mThumbY, mThumbRadius - (mTorusRadius + 2), mThumbPaint);

		mThumbPaint.setColor(mDefaultSelectColor);
		mThumbPaint.setStyle(Style.FILL);
		canvas.drawCircle(mThumbX, mThumbY, mThumbRadius - (mTorusRadius + 2), mThumbPaint);

		// TODO 跟随手指绘制提示标识, [temporary 只测试了density=2的手机, 其他的没做测试].
		if (mMarkDrawable != null && isShowMark) {
			int drawableWidth = mMarkDrawable.getIntrinsicWidth();
			int drawableHeight = mMarkDrawable.getIntrinsicHeight();

			canvas.drawCircle(mThumbX, mThumbY - mThumbRadius - (drawableHeight + 10), drawableHeight - 20, mThumbPaint);
			mMarkDrawable.setBounds(mThumbX - drawableWidth, mThumbY - mThumbRadius - (drawableHeight * 2), mThumbX + drawableWidth, mThumbY -mThumbRadius);
			mMarkDrawable.draw(canvas);
		}

		// 回到下次绘制外层状态.
		mThumbPaint.setColor(Color.GRAY);
		mThumbPaint.setStyle(Style.STROKE);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);

		float clickX = event.getX();
		float rawX = event.getRawX();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			// 绘制提示标识.
			isShowMark = true;

			// 限定右边左右边界.
			mThumbX = (int) event.getX();
			if (mThumbX < mThumbRadius) {
				mThumbX = mThumbRadius;
			}
			if (mThumbX > (mViewWidth - mThumbRadius)) {
				mThumbX = mViewWidth - mThumbRadius;
			}

			int sh = mViewWidth - mThumbRadius - mThumbRadius + 1;
			float unit = (clickX - mThumbRadius) / sh;
			mDefaultSelectColor = interpColor(mColorField, unit);
			invalidate();

			if (mColorListener != null) {
				if (mLocation != null && mLocation.length > 0) {
					if (rawX < mLocation[0] + mThumbRadius) {
						rawX = mLocation[0] + mThumbRadius;
					}
					if (rawX > mLocation[0] + mViewWidth - mThumbRadius) {
						rawX = mLocation[0] + mViewWidth - mThumbRadius;
					}
					mColorListener.onColorChanged(mDefaultSelectColor, rawX);
				}
			}
			return true;

		case MotionEvent.ACTION_UP:
			// 停止绘制提示标识.
			isShowMark = false;

			mThumbX = (int) event.getX();
			if (mThumbX < mThumbRadius) {
				mThumbX = mThumbRadius;
			}
			if (mThumbX > (mViewWidth - mThumbRadius)) {
				mThumbX = mViewWidth - mThumbRadius;
			}
			invalidate();

			if (mColorListener != null) {
				mColorListener.onColorSelected(mDefaultSelectColor);
			}
			return true;

		default:
			break;
		}
		return true;
	}


	/**
	 * 截取颜色.
	 */
	private int interpColor(int colors[], float unit) {
		if (unit <= 0) {
			return colors[0];
		}
		if (unit >= 1) {
			return colors[colors.length - 1];
		}

		float p = 0;
		int i = 0;
		if (mPositions == null) {
			p = unit * (colors.length - 1);
			i = (int) p;
			p -= i;
		} else {
			for (int j = 1; j < mPositions.length; j++) {
				if (unit < mPositions[j]) {
					i = j - 1;
					p = (unit - mPositions[i]) / (mPositions[j] - mPositions[i]);
					break;
				}
			}
		}

		// now p is just the fractional part [0...1) and i is the index
		int c0 = colors[i];
		int c1 = colors[i+1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		return Color.argb(a, r, g, b);
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		invalidate();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (hasWindowFocus) {
			invalidate();
		}
		super.onWindowFocusChanged(hasWindowFocus);
	}

	/**
	 * 设置颜色变化、选中监听.
	 */
	public void setOnColorChangedListener(OnColorChangedListener l) {
		mColorListener = l;
	}

	/**
	 * 设置颜色值, 通过色值取出游标位置并显示.
	 */
	public void setColor(final int color) {

		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				mViewWidth = getMeasuredWidth();
		    	mDefaultSelectColor = color;
		    	int real = mViewWidth - mThumbRadius * 2 + 1;

				float[] hsv = new float[3];
				Color.colorToHSV(color, hsv);

				mThumbX = (int) (hsv[0] / 360 * real + mThumbRadius);
				if (mThumbX < mThumbRadius) {
		    		mThumbX = mThumbRadius;
		    	}
		    	if (mThumbX > (mViewWidth - mThumbRadius)) {
		    		mThumbX = mViewWidth - mThumbRadius;
		    	}
		    	Log.d(TAG, "setColor -> mThumbX >> " + mThumbX);

		    	postInvalidate();
				getViewTreeObserver().removeOnPreDrawListener(this);
				return false;
			}
		});
	}

	/**
	 * 颜色变化、选中的监听接口.
	 * @author mymkf 16-04-06
	 *
	 */
	public interface OnColorChangedListener {

		/**
		 * 颜色改变时的回调.
		 * @param color 被选中的颜色.
		 * @param x 手指在屏幕内x坐标.
		 */
		void onColorChanged(int color, float x);

		/**
		 * 手指离开选色游标时回调.
		 * @param color 被选中的颜色.
		 */
		void onColorSelected(int color);
	}
}
