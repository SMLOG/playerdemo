package com.usbtv.demo.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class AudioWaveView extends TextView {
    private Paint paint;
    private RectF rectF1;
    private RectF rectF2;
    private RectF rectF3;
    private RectF rectF4;
    private RectF rectF5;
    private int viewWidth;
    private int viewHeight;
    /** 每个条的宽度 */
    private int rectWidth;
    /** 条数 */
    private int columnCount = 5;
    /** 条间距 */
    private final int space = 6;
    /** 条随机高度 */
    private int randomHeight;
    private Random random;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };
    private int width;

    public AudioWaveView(Context context) {
        super(context);
        init();
    }

    public AudioWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        viewWidth=150;
        rectWidth = (viewWidth - space * (columnCount - 1)) / columnCount;
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        random = new Random();

        initRect();
    }

    private void initRect() {
        rectF1 = new RectF();
        rectF2 = new RectF();
        rectF3 = new RectF();
        rectF4 = new RectF();
        rectF5 = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int left = rectWidth + space;
        int start=(width - viewWidth)/2;
        int rectHeight=50;
        //画每个条之前高度都重新随机生成
        randomHeight = random.nextInt(rectHeight);
        rectF1.set(start+left * 0, viewHeight-randomHeight, start+left * 0 + rectWidth, viewHeight);
        randomHeight = random.nextInt(rectHeight);
        rectF2.set(start+left * 1, viewHeight-randomHeight, start+left * 1 + rectWidth, viewHeight);
        randomHeight = random.nextInt(rectHeight);
        rectF3.set(start+left * 2, viewHeight-randomHeight, start+left * 2 + rectWidth, viewHeight);
        randomHeight = random.nextInt(rectHeight);
        rectF4.set(start+left * 3, viewHeight-randomHeight, start+left * 3 + rectWidth, viewHeight);
        randomHeight = random.nextInt(rectHeight);
        rectF5.set(start+left * 4, viewHeight-randomHeight, start+left * 4 + rectWidth, viewHeight);

        canvas.drawRect(rectF1, paint);
        canvas.drawRect(rectF2, paint);
        canvas.drawRect(rectF3, paint);
        canvas.drawRect(rectF4, paint);
        canvas.drawRect(rectF5, paint);

        handler.sendEmptyMessageDelayed(0, 200); //每间隔200毫秒发送消息刷新
    }

}