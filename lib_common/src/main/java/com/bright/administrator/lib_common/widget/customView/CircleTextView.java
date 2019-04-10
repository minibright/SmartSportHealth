package com.bright.administrator.lib_common.widget.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;

public class CircleTextView extends android.support.v7.widget.AppCompatTextView {
    public static final String TAG = "CircleTextView";
    private Paint mBgPaint = new Paint();
    private Context mContext;

    PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG| Paint.FILTER_BITMAP_FLAG);

    public CircleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleTextView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context){
        mContext = context;
        mBgPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int max = Math.max(measuredWidth, measuredHeight);
        setMeasuredDimension(max, max);
    }

    @Override
    public void setBackgroundColor(int color ) {
        mBgPaint.setColor(color);
    }

    public void setNotifiText(int text){
        //      if(text>99){
        //          String string = 99+"+";
        //          setText(string);
        //          return;
        //      }
        setText(text+"");
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.setDrawFilter(pfd);
        canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2, mBgPaint);
        super.draw(canvas);
    }
}
