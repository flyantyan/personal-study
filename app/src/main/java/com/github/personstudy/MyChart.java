package com.github.personstudy;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanfei on 17/9/6.
 */

public class MyChart extends ViewGroup {
    /**
     * Draw text to the left of the pie chart
     */
    public static final int TEXTPOS_LEFT = 0;

    private Paint mTextPaint;
    private Paint mPiePaint;
    private Paint mShadowPaint;


    private PieView mPieView;
    private PointerView mPointerView;


    private ObjectAnimator mAutoCenterAnimator;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    /***gesture dector to handle onTouch messages*/
    private GestureDetector mDetector;

    private int mCurrentItemAngle;
    // index of the current item
    private int mCurrentItem = 0;


    private List<Item> mData = new ArrayList<Item>();
    private float mTotal = 0.0f;

    private RectF mPieBounds = new RectF() ;

    private float mPointerX;
    private float mPointerY;
    private float mTextX = 0.0f;
    /***********************自定义参数start*******************************/
    /**
     * 是否显示label tips
     **/
    private boolean mShowText;
    /* label 位置（圆的左边/右边） */
    private int mTextPos;
    /**
     * label的Y坐标
     **/
    private float mTextY;
    /***label的宽度***/
    private float mTextWidth;
    /****lable的高度****/
    private float mTextHeight;
    /**
     * 控件的文字颜色
     **/
    private int mTextColor;
    private float mHighlightStrength = 1.15f;
    /***初始角度***/
    private int mPieRotation;
    /****指示点半径***/
    private float mPointerRadius;
    /***指示点自动居中（位于扇形内容中间）***/
    private boolean mAutoCenterInSlice;
    /***********************自定义参数 end*******************************/


    // 事件
    private OnCurrentItemChangedListener mCurrentItemChangedListener = null;

    public MyChart(Context context) {
        super(context);
        init();
    }

    public MyChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttribute(context, attrs);
        init();
    }

    public MyChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnCurrentItemChangedListener(OnCurrentItemChangedListener listener) {
        mCurrentItemChangedListener = listener;
    }

    private void getAttribute(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MyChart, 0, 0);


        try {
            mShowText = a.getBoolean(R.styleable.MyChart_showText, false);
            mTextY = a.getDimension(R.styleable.MyChart_labelY, 0.0f);
            mTextWidth = a.getDimension(R.styleable.MyChart_labelWidth, 0.0f);
            mTextHeight = a.getDimension(R.styleable.MyChart_labelHeight, 0.0f);
            mTextPos = a.getInteger(R.styleable.MyChart_labelPosition, 0);
            mTextColor = a.getColor(R.styleable.MyChart_labelColor, 0xff000000);
            mHighlightStrength = a.getFloat(R.styleable.MyChart_highlightStrength, 1.0f);
            mPieRotation = a.getInt(R.styleable.MyChart_pieRotation, 0);
            mPointerRadius = a.getDimension(R.styleable.MyChart_pointerRadius, 2.0f);
            mAutoCenterInSlice = a.getBoolean(R.styleable.MyChart_autoCenterPointerInSlice, false);
        } finally {
            a.recycle();
        }

    }

    public float getTextHeight() {
        return mTextHeight;
    }

    public void setTextHeight(float textHeight) {
        mTextHeight = textHeight;
        invalidate();
    }

    public float getPointerRadius() {
        return mPointerRadius;
    }

    public void setPointerRadius(float pointerRadius) {
        mPointerRadius = pointerRadius;
        invalidate();
    }

    public int addItem(String label , float value , int color){
        Item item = new Item();
        item.mLabel = label ;
        item.mColor = color ;
        item.mValue = value ;

        // Calculate the hightlight color. Saturate at 0xff to make sure that high value
        // don't result in aliasing
        item.mHighlight = Color.argb(
                0xff ,
                Math.min((int)mHighlightStrength * Color.red(color) , 0xff) ,
                Math.min((int) mHighlightStrength * Color.green(color) , 0xff) ,
                Math.min((int)mHighlightStrength * Color.blue(color) , 0xff)
        );

        mTotal += value ;
        mData.add(item);
        onDateChanged();
        return mData.size()-1 ;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the label text
        if(mShowText){
            canvas.drawText(mData.get(mCurrentItem).mLabel , mTextX , mTextY , mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean resule = mDetector.onTouchEvent(event);

//        if (!resule) {
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                // User is done scrolling, it's now safe to do things like autocenter
//                resule = true;
//            }
//        }
        Log.i("flyyan" , resule+"");
        return resule;
    }


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        boolean resule = mDetector.onTouchEvent(ev);
//
////        if (!resule) {
////            if (event.getAction() == MotionEvent.ACTION_UP) {
////                // User is done scrolling, it's now safe to do things like autocenter
////                resule = true;
////            }
////        }
//        return resule;
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() ;

        int w = Math.max(minw , MeasureSpec.getSize(widthMeasureSpec));

        // Whatever the width ends up being, ask for a height that would let the pie get
        // as big as it can
        int minH = (int) ((w - mTextWidth) + getPaddingBottom() + getPaddingTop());
        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec) , minH);

        setMeasuredDimension(w , h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Account for padding
        float xpad = getPaddingLeft() + getPaddingRight() ;
        float ypad = getPaddingTop() + getPaddingBottom() ;

        // Account for the label
        if(mShowText){
            xpad += mTextWidth ;
        }

        float ww = w - xpad ;
        float hh = h - ypad ;
        // Account for the radius for piecircle
        float diameter = Math.min(ww , hh);
        // init rect for circle
        mPieBounds = new RectF(
                0.0f,
                0,
                diameter,
                diameter
            );

        // adjust the rect
        mPieBounds.offsetTo(getPaddingLeft() , getPaddingTop());

        // Account for lable circle
        mPointerY = mTextY - (mTextHeight/2);
        float pointerOffset = mPieBounds.centerY() - mPointerY ;

        // Make adjustments based on text position
        if(mTextPos == TEXTPOS_LEFT){
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            if(mShowText){
                mPieBounds.offsetTo(mTextWidth , 0);
            }
            mTextX = mPieBounds.left ;

            if(pointerOffset < 0){
                pointerOffset = -pointerOffset ;
                mCurrentItemAngle = 225;
            }else{
                mCurrentItemAngle = 135 ;
            }
            mPointerX = mPieBounds.centerX() - pointerOffset ;
        }else{
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextX = mPieBounds.right;

            if(pointerOffset < 0){
                pointerOffset = -pointerOffset ;
                mCurrentItemAngle = 315 ;
            } else {
                mCurrentItemAngle = 45 ;
            }
            mPointerX = mPieBounds.centerX() + pointerOffset ;
        }

        // Lay out the child view that actually draws the pie
        mPieView.layout((int)mPieBounds.left ,
                (int)mPieBounds.top ,
                (int)mPieBounds.right ,
                (int)mPieBounds.bottom);
        mPieView.setPivot(mPieBounds.width()/2 , mPieBounds.height()/2);

        mPointerView.layout(0 , 0 , w , h);
        onDateChanged();
    }

    /**
     * Do all of the recalcalations needed when data array changes
     */
    private void onDateChanged(){
        // When data changes , we have to recalculate all of the angels
        int currentAngle = 0 ;
        for(Item it : mData){
            it.mStartAngle = currentAngle ;
            it.mEndAngle = (int) (currentAngle + it.mValue * 360.0f/mTotal);
            currentAngle = it.mEndAngle ;

            // Recalculate the gradient shaders. There are three values in this gradient,
            // even though only two are necessary , in order to work around a
            it.mShader = new SweepGradient(
                    mPieBounds.width() / 2.0f ,
                    mPieBounds.height() /2.0f ,
                    new int[]{
                            it.mHighlight ,
                            it.mHighlight ,
                            it.mColor ,
                            it.mColor ,
                    },
                    new float[]{
                            0,
                            (360 - it.mEndAngle)/360 ,
                            (360 - it.mStartAngle) / 360 ,
                            1.0f
                    }
            );
        }
        calcCurrentItem();
        onScrollFinished();
    }

    /**
     * Called when the user finishes a scroll action.
     */
    private void onScrollFinished() {
        if (mAutoCenterInSlice) {
            centerOnCurrentItem();
        } else {
            mPieView.decelerate();
        }
    }
    private void centerOnCurrentItem(){
        Item current = mData.get(mCurrentItem) ;
        int targetAngle = current.mStartAngle + (current.mEndAngle - current.mStartAngle) /2 ;
        targetAngle -= mCurrentItemAngle ;

        // Fancy animated version
        mAutoCenterAnimator.setIntValues(targetAngle);
        mAutoCenterAnimator.setDuration(250).start();
    }

    private void calcCurrentItem(){
        int pointerAngle = (mCurrentItemAngle + 360 + mPieRotation) % 360 ;
        for(int i = 0 ; i < mData.size() ; i++){
            Item it = mData.get(i);
            if(it.mStartAngle <= pointerAngle && pointerAngle <= it.mEndAngle){
                if(i != mCurrentItem){
                    setCurrentItem(i, false);
                }
                break;
            }
        }
    }

    private void setCurrentItem(int currentItem, boolean scrollIntoView) {
        mCurrentItem = currentItem;
        if (mCurrentItemChangedListener != null) {
            mCurrentItemChangedListener.OnCurrentItemChanged(this, currentItem);
        }
        if (scrollIntoView) {
            centerOnCurrentItem();
        }
        invalidate();
    }
    private void init() {
        //开启硬件加速，不然 blur 过滤不起作用
        setLayerToSW(this);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        if (mTextHeight == 0) {
            mTextHeight = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextHeight);
        }

        //设置圆盘画笔
        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);
        mPiePaint.setTextSize(mTextHeight);

        mShadowPaint = new Paint(0);
        mShadowPaint.setColor(0xff101010);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        //set up child view
        mPieView = new PieView(getContext());
        addView(mPieView);
        //TODO
//        mPieView.

        mPointerView = new PointerView(getContext());
        addView(mPointerView);


        // Set up an animator to animate the PieRotation property. This is used to correct the
        // pie's orientation after the user lets go of it
        mAutoCenterAnimator = ObjectAnimator.ofInt(this, "PieRotation", 0);

        // Add a listener to hook the onAnimationEnd event so that we can do
        // some cleanup when pie stops moving
        mAutoCenterAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPieView.decelerate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mScroller = new Scroller(getContext(), null, true);

        // The scoller doesn't have any build-in animation functions--it just supplies
        // value when we ask it to . So we have to have a way ti call it every frame
        // until the flingedns . This code uses a ValueAnimator object to generate
        // a callback on every animation frame
        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tickScrollAnimation();
            }
        });
        // create a gesture dector to handle onTouch Message
        mDetector = new GestureDetector(getContext(), new GestureListener());

        // Turn off long press--this control doesn't use it, and if long press is enabled,
        // you can't scroll for a bit , pause,then scroll some more(the pause is interpreted
        // as a long press,apprently
        mDetector.setIsLongpressEnabled(false);


    }
    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setPieRotation(mScroller.getCurrY());
        } else {
            mScrollAnimator.cancel();
            onScrollFinished();
        }
    }

    public void setPieRotation(int rotation) {
        rotation = (rotation % 360 + 360) % 360;
        mPieRotation = rotation;
        mPieView.setRotation(rotation);
        calcCurrentItem();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    private void setLayerToSW(View v) {
        if (!v.isInEditMode()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void setLayerToHW(View v) {
        if (!v.isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(MyChart source, int currentItem);
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

    private class PieView extends View {
        private PointF mPivot = new PointF();

        public PieView(Context context) {
            super(context);
        }


        public  void rotateTo(float pieRotation){
            MyChart.this.setRotation(pieRotation);
        }

        /**
         * Enable hardware acceleration (consumes memory)
         */
        public void accelerate(){
            setLayerToHW(this);
        }

        /**
         * Diasble hardware acceleration (release memory)
         */
        public void decelerate(){
            setLayerToSW(this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for (Item it : mData){
                mPiePaint.setShader(it.mShader);
                canvas.drawArc(mBounds , 360 - it.mEndAngle,
                        it.mEndAngle - it.mStartAngle ,
                        true ,
                        mPiePaint
                        );
            }
        }

        public void setPivot(float x, float y) {
            mPivot.x = x;
            mPivot.y = y;
            setPivotX(x);
            setPivotY(y);
        }
        RectF mBounds;

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBounds = new RectF(0 , 0 , w , h);
        }
    }

    private class PointerView extends View {

        public PointerView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawLine(mTextX, mPointerY, mPointerX, mPointerY, mTextPaint);
            canvas.drawCircle(mPointerX, mPointerY, mPointerRadius, mTextPaint);
        }
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            float scrollTheta = vectorToScalarScroll(
                    distanceX ,
                    distanceY ,
                    e2.getX() - mPieBounds.centerX(),
                    e2.getY() - mPieBounds.centerY()
            );
            Log.i("flyyan" , mPieRotation+"\t"+scrollTheta+"\t" + (mPieRotation- scrollTheta));
            mPieView.setRotation(mPieRotation - scrollTheta/4);
            return true ;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Set up the Scroller for a fling
            float scrollTheta = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - mPieBounds.centerX(),
                    e2.getY() - mPieBounds.centerY());
            mScroller.fling(
                    0,
                    mPieRotation,
                    0,
                    (int) scrollTheta/4,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);

            // Start the animator and tell it to animate for the expected duration of the fling.
            mScrollAnimator.setDuration(mScroller.getDuration());
            mScrollAnimator.start();
            return true;
        }


        @Override
        public boolean onDown(MotionEvent e) {
            // The user is interacting with the pie, so we want to turn on acceleration
            // so that the interaction is smooth.
            mPieView.accelerate();
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
}
