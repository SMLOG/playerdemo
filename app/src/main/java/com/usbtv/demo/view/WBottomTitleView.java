package com.usbtv.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.usbtv.demo.R;


/**
 * 基本功能：带有底部标题的ImageView
 * 创建：王杰
 * 创建时间：2017-02-18
 */

public class WBottomTitleView extends ImageView {
    private String mTextString;
    private int mTextColor = Color.RED;
    private int mAlpha = 150;
    private float mTextDimension = 0;
    private Drawable mTextDrawable;
    private Paint p;
    private TextPaint mTextPaint;
    private float mTextHeight;
    private int mTextBgColor = Color.DKGRAY;
    private Paint.FontMetrics mTextPaintfontMetrics;

    public WBottomTitleView(Context context) {
        super(context);
        init(null, 0);
    }

    public WBottomTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WBottomTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        //加载 attributes
        final TypedArray typedArray = getContext().obtainStyledAttributes(
                attrs, R.styleable.WBottomTitleView, defStyle, 0);

        mTextString = typedArray.getString(
                R.styleable.WBottomTitleView_textString);
        mTextColor = typedArray.getColor(
                R.styleable.WBottomTitleView_textColor,
                mTextColor);
        mTextDimension = typedArray.getDimension(
                R.styleable.WBottomTitleView_textDimension,
                mTextDimension);
        mAlpha = typedArray.getInt(R.styleable.WBottomTitleView_mAlpha, mAlpha);
        mTextBgColor = typedArray.getInt(R.styleable.WBottomTitleView_mTextBgColor, mTextBgColor);
        if (typedArray.hasValue(R.styleable.WBottomTitleView_textDrawable)) {
            mTextDrawable = typedArray.getDrawable(
                    R.styleable.WBottomTitleView_textDrawable);
            mTextDrawable.setCallback(this);
        }
        typedArray.recycle();
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        p = new Paint();
        invalidateTextPaintAndMeasurements();
    }

    //根据attributes更新TextPaint
    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextDimension);
        mTextPaint.setColor(mTextColor);
        if (TextUtils.isEmpty(mTextString)) {
            mTextString = "";
        }
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;
        mTextPaintfontMetrics = mTextPaint.getFontMetrics();
        p.setColor(mTextBgColor);// 设置灰色
        p.setAlpha(mAlpha);
        p.setStyle(Paint.Style.FILL);//设置填满
        canvas.drawRect(paddingLeft, contentHeight - (mTextPaintfontMetrics.bottom - mTextPaintfontMetrics.top), contentWidth, contentHeight, p);// 矩形

        // Draw the text.
        canvas.drawText(mTextString,
                paddingLeft,
                paddingTop + (contentHeight - mTextHeight),
                mTextPaint);

        // Draw the text drawable on top of the text.
        if (mTextDrawable != null) {
            mTextDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mTextDrawable.draw(canvas);
        }

    }

    public String gettextString() {
        return mTextString;
    }

    public void setTextString(String textString) {
        mTextString = textString;
        invalidateTextPaintAndMeasurements();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidateTextPaintAndMeasurements();
    }

    public float getTextDimension() {
        return mTextDimension;
    }

    public void setTextDimension(float textDimension) {
        mTextDimension = textDimension;
        invalidateTextPaintAndMeasurements();
    }

    public Drawable getTextDrawable() {
        return mTextDrawable;
    }

    public void setTextDrawable(Drawable textDrawable) {
        mTextDrawable = textDrawable;
    }

    public int getmTextBgColor() {
        return mTextBgColor;
    }

    public void setmTextBgColor(int mTextBgColor) {
        this.mTextBgColor = mTextBgColor;
    }

    public int getmAlpha() {
        return mAlpha;
    }

    public void setmAlpha(int mAlpha) {
        this.mAlpha = mAlpha;
    }
}