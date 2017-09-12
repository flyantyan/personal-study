package com.github.personstudy;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanfei on 17/9/8.
 */

public class MyPieView extends View {


    private Paint mPiePaint ;
    private Paint mTextPaint ;

    private RectF rectF ;


    private GestureDetector mDetector ;
    private float mTotal ;

    private Scroller mScroller ;
    private List<Item> mData = new ArrayList<>();
    public MyPieView(Context context) {
        super(context);
        init();
    }

    public MyPieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyPieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void addItem(String label , float value , int color){
        Item item = new Item();
        item.mLabel = label ;
        item.mValue = value ;
        item.mColor = color ;
        mData.add(item);
        mTotal += value ;
        onDataChange();
        invalidate();
    }

    private void onDataChange(){
        int startAngle = 0 ;
        for(int i = 0 ; i < mData.size() ; i++){
            Item item = mData.get(i);

            item.mStartAngle = startAngle ;
            if(i == mData.size()-1){
                item.mEndAngle = 360 ;
            }else{
                item.mEndAngle = (int) (startAngle + item.mValue*1.0/mTotal * 360);
//                mAnimator.start();
            }
            startAngle = item.mEndAngle ;
            Log.i("asd" ,"start"+startAngle);

            item.mHighlight = Color.argb(
                    0xff ,
                    Math.min((int)1.2 * Color.red(item.mColor) , 0xff) ,
                    Math.min((int) 1.2 * Color.green(item.mColor) , 0xff) ,
                    Math.min((int)1.2 * Color.blue(item.mColor) , 0xff)
            );
            item.mShader = new SweepGradient(
                    rectF.width()/2,
                    rectF.height() /2 ,
                    new int[]{
                            item.mHighlight ,
                            item.mHighlight ,
                            item.mColor ,
                            item.mColor ,
                    },
                    new float[]{
                            0,
                            (360 - item.mEndAngle)/360 ,
                            (360 - item.mStartAngle) / 360 ,
                            1.0f
                    }
            );
        }
    }

    private ObjectAnimator mAnimator ;
    private ValueAnimator mScrollerAnimator ;
    private void init(){
        //设置圆盘画笔
        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);
        mPiePaint.setTextSize(20);

        rectF = new RectF(0 , 0 , 800 , 800);

        mAnimator = ObjectAnimator.ofInt(this , "rotation" , 0 , 360);
        mAnimator.setDuration(3000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mDetector = new GestureDetector(getContext() ,new GestrueDectorListener());

        mScroller = new Scroller(getContext() , null , true);

        mScrollerAnimator = ValueAnimator.ofFloat(0,1);
        mScrollerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!mScroller.isFinished()) {
                    mScroller.computeScrollOffset();
                    mDegree = (mScroller.getCurrY());
                    invalidate();
                }
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private int mDegree ;
    public void setRotation(int degree){
        this.mDegree = degree ;
        invalidate();
    }

    /**
     * Maintains the state for a data item.
     */
    private class Item {
        public String mLabel;
        public float mValue;
        public int mColor;

        // computed values
        public int mStartAngle;
        public int mEndAngle;

        public int mHighlight;
        public Shader mShader;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int j = mDegree ;
        mDegree = (mDegree % 360 + 360) % 360;
        Log.d("asdasd",j+"============Draw"+mDegree);

        for(int i = 0 ; i < mData.size() ; i++){
            Item item = mData.get(i);
//            mPiePaint.setColor(item.mColor);
            mPiePaint.setShader(item.mShader);
            canvas.drawArc(rectF , item.mStartAngle+mDegree , item.mEndAngle-item.mStartAngle , true , mPiePaint);
        }

    }


    class GestrueDectorListener extends  GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            float scrollTheta = vectorToScalarScroll(
                    distanceX ,
                    distanceY ,
                    e2.getX() - rectF.centerX(),
                    e2.getY() - rectF.centerY()
            );
            Log.d("asdasd","scrollTheta"+scrollTheta+"\t"+rectF.centerX()+"\t"+rectF.centerY());
            Log.i("ad" , scrollTheta+"");
            mDegree -= scrollTheta/2;
            invalidate();
            return true ;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Set up the Scroller for a fling
            float scrollTheta = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - rectF.centerX(),
                    e2.getY() - rectF.centerY());
            mScroller.fling(
                    0,
                    mDegree,
                    0,
                    (int) scrollTheta/4,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);

            // Start the animator and tell it to animate for the expected duration of the fling.
            mScrollerAnimator.setDuration(mScroller.getDuration());
            mScrollerAnimator.start();
            return true;
        }


        @Override
        public boolean onDown(MotionEvent e) {
            // The user is interacting with the pie, so we want to turn on acceleration
            // so that the interaction is smooth.
//            if (isAnimationRunning()) {
//                stopScrolling();
//            }
            return true;
        }

    }

    private static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        // get the length of the vector
        float l = (float) Math.sqrt(dx * dx + dy * dy);
        // decide if the scalar should be negative or positive by finding
        // the dot product of the vector perpendicular to (x,y).
        float crossX = -y;
        float crossY = x;
        float t = (float) Math.sqrt(x * x + y * y);

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);
        return l * sign;
    }

    private static float vectorToScalarScroll2(float dx, float dy, float x, float y) {
        // get the length of the vector
        Log.i("vectorToScalarScroll2" , dx +"\t"+dy+"\t"+x+"\t"+y);
        float l = (float) Math.sqrt(dx * dx + dy * dy);
        // decide if the scalar should be negative or positive by finding
        // the dot product of the vector perpendicular to (x,y).
        float crossX = -y;
        float crossY = x;
        float t = (float) Math.sqrt(x * x + y * y);

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);
        Log.i("flyyan" , dot + "\t" + l +"\t" + t + "\t" + x + "\t"+y +"\t"+Math.acos(dot/(l*t))+"\t"+dot/(l*t));
        return (float) Math.toDegrees(Math.acos(-dot/(l*t)));
    }
}
