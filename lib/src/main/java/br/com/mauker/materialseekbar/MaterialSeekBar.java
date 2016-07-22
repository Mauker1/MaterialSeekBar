package br.com.mauker.materialseekbar;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mauker on 27/05/16.
 *
 */
public class MaterialSeekBar extends View {

    // ----- Class properties ----- //

    // --- CONSTANTS --- //

    /**
     * The freaking log tag. Used for logs, duh.
     */
    private static final String LOG_TAG = MaterialSeekBar.class.getSimpleName();

    private static final int DEFAULT_WIDTH = 500;

    private static final int DEFAULT_HEIGHT = 100;

    /**
     * The Context that this view appears in.
     */
    private Context mContext;

    /**
     * The maximum value this Seek Bar can provide. effectively, this is your range (0 -> mMaxValue)
     */
    private int mMaxValue;

    /**
     * The current value of the Seek Bar. Can be anywhere between 0 and mMaxValue.
     */
    private int mCurrentValue;

    private int[] mColors = new int[] {0xFFD32F2F, 0xFFFFEB3B, 0xFF4CAF50, 0xFF448AFF};

    private int mRed, mGreen, mBlue;

    /**
     * The real left positioning from the View. Taking padding into account.
     */
    private int mRealLeft;

    /**
     * The real right positioning from the View. Taking padding into account.
     */
    private int mRealRight;

    /**
     * The real top positioning from the View. Taking padding into account.
     */
    private int mRealTop;

    /**
     * The real bottom positioning from the View. Taking padding into account.
     */
    private int mRealBottom;

    private int mBarWidth;

    private int mBarHeight;

    private int mThumbRadius;

    private int mThumbHeight;

    private int mBackgroundColor = 0xffffffff;

    /**
     * Additional padding required for the thumb and pin.
     */
    private int mPaddingSize;

    /**
     * The width of this Seek Bar, in pixels.
     */
    private int mViewWidth;

    /**
     * The height of this Seek Bar, in pixels.
     */
    private int mViewHeight;

    private Rect mColorRect;

    private Rect mBgRect;

    /**
     * Whether the Seek Bar is pressed or not.
     */
    private boolean mIsPressed;

    /**
     * Whether the Seek Bar is moving or not.
     */
    private boolean mIsMoving;

    /**
     * Whether the Seek Bar is in gradient background mode or not.
     */
    private boolean mIsGradient;

    /**
     * Whether the Seek Bar's pin is always visible or not.
     */
    private boolean mIsPinTemporary;

    // ----- Paints ----- //

    private Paint colorPaint;

    private Paint mColorRectPaint;

    private Paint mBgPaint;

    private Bitmap mTransparentBitmap;

    // ----- UI Elements ----- //

    /**
     * The pin that shows up when the user selects the seek bar. It's used to inform the value of the current position.
     */
    private PinView mPin;


    // ----- Constructors ----- //

    public MaterialSeekBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public MaterialSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public MaterialSeekBar(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mContext = context;
        //get attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorSeekBar, defStyleAttr, defStyleRes);
        int colorsId = a.getResourceId(R.styleable.ColorSeekBar_colors, 0);
        mMaxValue = a.getInteger(R.styleable.ColorSeekBar_maxValue,100);
        mCurrentValue = a.getInteger(R.styleable.ColorSeekBar_colorBarValue,0);
        mBackgroundColor = a.getColor(R.styleable.ColorSeekBar_bgColor, Color.TRANSPARENT);
        mBarHeight = (int)a.getDimension(R.styleable.ColorSeekBar_barHeight,(float)dp2px(3));
        mThumbHeight = (int)a.getDimension(R.styleable.ColorSeekBar_thumbHeight,(float)dp2px(20));
        a.recycle();

        mIsPinTemporary = true;
        mIsPressed = false;

        if(colorsId != 0) mColors = getColorsById(colorsId);

        setBackgroundColor(mBackgroundColor);

        mColorRectPaint = new Paint();
        colorPaint = new Paint();
        mBgPaint = new Paint();

        init();
    }

    private void init() {
        //init l r t b

        mThumbRadius = mThumbHeight / 2;
//        mPaddingSize = (int)mThumbRadius;

        mRealLeft = getPaddingLeft() + mPaddingSize;
        mRealRight = getWidth() - getPaddingRight() - mPaddingSize;
        // TODO - Aqui, você tem que dar o tamanho da budega. Adicionando o tamanho da barra + o da bola e o do pin.

//        mRealTop = getPaddingTop() + mPaddingSize + 80;
        mRealTop = getPaddingTop() + mPaddingSize;
        mRealBottom = getHeight() - getPaddingBottom() - mPaddingSize;

//        //init size
//        mThumbRadius = mThumbHeight / 2;
        mPaddingSize = mThumbRadius;
        mBarWidth = mRealRight - mRealLeft;

        //init rect
        mColorRect = new Rect(mRealLeft , mRealTop, mRealRight, mRealTop + mBarHeight);

        int bgTop = mRealTop + mColorRect.height()/4;

        mBgRect = new Rect(mRealLeft, bgTop, mRealRight, bgTop + (mBarHeight/2));

        //init paint
//        mColorGradient = new LinearGradient(0, 0, mColorRect.width(), 0, mColors, null, Shader.TileMode.MIRROR);
//        mColorRectPaint = new Paint();
        // TODO - Get back.
        //mColorRectPaint.setShader(mColorGradient);
        mColorRectPaint.setAntiAlias(true);

    }

    private void updateInit() {
        //init l r t b

        mThumbRadius = mThumbHeight / 2;

        mRealLeft = getPaddingLeft() + mPaddingSize;
        mRealRight = getWidth() - getPaddingRight() - mPaddingSize;

        mRealTop = getPaddingTop() + mPaddingSize;
        mRealBottom = getHeight() - getPaddingBottom() - mPaddingSize;

//        //init size
        mPaddingSize = mThumbRadius;
        mBarWidth = mRealRight - mRealLeft;

        //init rect
        mColorRect.left = mRealLeft;
        mColorRect.top = mRealTop;
        mColorRect.right = mRealRight;
        mColorRect.bottom = mRealTop + mBarHeight;

        int bgTop = mRealTop + mColorRect.height()/4;

        mBgRect.left = mRealLeft;
        mBgRect.top = bgTop;
        mBgRect.right = mRealRight;
        mBgRect.bottom = bgTop + (mBarHeight/2);

        mColorRectPaint.setAntiAlias(true);
    }

    // ----- Draw and measure methods ----- //

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = widthMeasureSpec;
        mViewHeight = heightMeasureSpec;

        int speMode = MeasureSpec.getMode(heightMeasureSpec);
        if(speMode == MeasureSpec.AT_MOST || speMode == MeasureSpec.UNSPECIFIED){
            if(mIsShowAlphaBar){
                setMeasuredDimension(mViewWidth,mThumbHeight * 2 +  mBarHeight * 2  + mBarMargin);
            }else{
                setMeasuredDimension(mViewWidth,mThumbHeight + mBarHeight + pin.getHeight());
            }
        }*/

        int width;
        int height;

        // Get measureSpec mode and size values.
        final int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);

        // The RangeBar width should be as large as possible.
        if (measureWidthMode == MeasureSpec.AT_MOST) {
            mViewWidth = measureWidth;
        } else if (measureWidthMode == MeasureSpec.EXACTLY) {
            mViewWidth = measureWidth;
        } else {
            mViewWidth = DEFAULT_WIDTH;
        }

        // The RangeBar height should be as small as possible.
        if (measureHeightMode == MeasureSpec.AT_MOST) {
            mViewHeight = Math.min(DEFAULT_HEIGHT, measureHeight);
        } else if (measureHeightMode == MeasureSpec.EXACTLY) {
            mViewHeight = measureHeight;
        } else {
            mViewHeight = DEFAULT_HEIGHT;
        }

//        Log.d(LOG_TAG,"onMeasure w: " + mViewWidth + " height: " + mViewHeight);
//        Log.d(LOG_TAG,"onMeasure new h: " + (mThumbHeight + mBarHeight));
//        setMeasuredDimension(mViewWidth, mViewHeight + 80);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateInit();
//        float thumbY = mColorRect.top + mColorRect.height() / 2;
//        float colorPosition = (float) mCurrentValue / mMaxValue * mBarWidth;
//        mPin = new PinView(getContext());
//        // TODO - Allow the user to change the sizes.
//        mPin.init(getContext(),thumbY,1000,pickColor(colorPosition),Color.WHITE,dp2px(6),pickColor(colorPosition),8,20, mIsPinTemporary);
//        mPin.setX(mRealLeft);
//        mPin.setXValue(String.valueOf(mCurrentValue));
        mTransparentBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_4444);
        mTransparentBitmap.eraseColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateInit();
        float colorPosition = (float) mCurrentValue / mMaxValue * mBarWidth;

//        Paint colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setColor(pickColor(colorPosition));
//        int[] toAlpha = new int[]{Color.argb(255, mRed, mGreen, mBlue),Color.argb(0, mRed, mGreen, mBlue)};
        //clear
        canvas.drawBitmap(mTransparentBitmap,0,0,null);

        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(Color.LTGRAY);

        mColorRectPaint.setColor(colorPaint.getColor());
        //draw color bar
        canvas.drawRect(mBgRect, mBgPaint);

        //draw color bar thumb
        float thumbX = colorPosition + mRealLeft;

        canvas.drawRect(mColorRect.left,mColorRect.top,thumbX,mColorRect.bottom, mColorRectPaint);

        float thumbY = mColorRect.top + mColorRect.height() / 2;

//        canvas.drawCircle(thumbX,thumbY , mBarHeight / 2 + 5, colorPaint);


        // TODO - Fix this performance issue
        //draw color bar thumb radial gradient shader
//        RadialGradient thumbShader  = new RadialGradient(thumbX,  thumbY,  mThumbRadius, toAlpha, null, Shader.TileMode.MIRROR);
//        RadialGradient thumbShader2  = new RadialGradient(thumbX,  thumbY,  mThumbRadius, Color.argb(0, mRed, mGreen, mBlue), Color.argb(255, mRed, mGreen, mBlue), Shader.TileMode.MIRROR);
//        SweepGradient ts = new SweepGradient(thumbX, thumbY, toAlpha,null);
//        Paint thumbGradientPaint = new Paint();
//        Paint strokePaint = new Paint();
//        thumbGradientPaint.setAntiAlias(true);
//        thumbGradientPaint.setColor(pickColor(colorPosition));
//        thumbGradientPaint.setAlpha(127);
//        thumbGradientPaint.setStyle(Paint.Style.FILL);
//        strokePaint.setAntiAlias(true);
//        strokePaint.setColor(pickColor(colorPosition));
//        strokePaint.setStrokeWidth(8);
////        thumbGradientPaint.setShader(thumbShader2);
//        strokePaint.setStyle(Paint.Style.STROKE);
//        canvas.drawCircle(thumbX, thumbY, mThumbHeight / 3, thumbGradientPaint);
//        canvas.drawCircle(thumbX, thumbY, mThumbHeight / 3, strokePaint);

        // TODO - Re-enable
//        mPin.setY(thumbY);
//        mPin.draw(canvas);

        if (mIsPressed) {
            canvas.drawCircle(thumbX, thumbY, mThumbHeight / 2, colorPaint);
//            mPin.setY(thumbY);
//            mPin.draw(canvas);
        }
        else {
            canvas.drawCircle(thumbX, thumbY, mThumbHeight / 3, colorPaint);
        }

        super.onDraw(canvas);
    }


    // ----- Touch events ----- //

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

//        if (Build.VERSION.SDK_INT >= 19)
//            Log.d(LOG_TAG,"Touch event: " + MotionEvent.actionToString(event.getAction()));

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsPressed = true;
                if(isOnBar(mColorRect, x, y)){
                    Log.d(LOG_TAG,"On bar!");
                    mIsMoving = true;

                    float value = (x - mRealLeft) / mBarWidth * mMaxValue;
                    mCurrentValue = (int) value;

                    if (mCurrentValue < 0) mCurrentValue = 0;
                    if (mCurrentValue > mMaxValue) mCurrentValue = mMaxValue;

                    invalidate();
                }
                // TODO - Re-enable
//                pressPin(mPin);
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if(mIsMoving){
                    // TODO - This is the actual value from within the range.
                    float value = (x - mRealLeft) / mBarWidth * mMaxValue;
                    mCurrentValue = (int) value;

                    if (mCurrentValue < 0) mCurrentValue = 0;
                    if (mCurrentValue > mMaxValue) mCurrentValue = mMaxValue;

                    // Change the pin value to the current position.
//                    mPin.setXValue(String.valueOf(mCurrentValue));

//                    onActionMove();
                }

                // TODO - Create listener.
//                if(mOnColorChangeLister != null && mIsMoving)
//                    mOnColorChangeLister.onColorChangeListener(mCurrentValue, mAlphaBarValue,getColor());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsPressed = false;
                mIsMoving = false;
//                releasePin(mPin);
                invalidate();
                break;
        }
        return true;
    }

    private void onActionMove() {
        float colorPosition = (float) mCurrentValue / mMaxValue * mBarWidth;
        mPin.setColor(pickColor(colorPosition));
        movePin(mPin, colorPosition + mRealLeft);
    }

    /**
     *
     * @param position
     * @return color
     */
    private int pickColor(float position) {
        float unit = position / mBarWidth;
        if (unit <= 0.0)
            return mColors[0];

        if (unit >= 1)
            return mColors[mColors.length - 1];

        float colorPosition = unit * (mColors.length - 1);
        int i = (int)colorPosition;
        colorPosition -= i;

        int c0 = mColors[i];
        int c1 = mColors[i+1];

        mRed = mix(Color.red(c0), Color.red(c1), colorPosition);
        mGreen = mix(Color.green(c0), Color.green(c1), colorPosition);
        mBlue = mix(Color.blue(c0), Color.blue(c1), colorPosition);
        return Color.rgb(mRed, mGreen, mBlue);
    }

    /**
     *
     * @param start
     * @param end
     * @param position　
     * @return
     */
    private int mix(int start, int end, float position) {
        return start + Math.round(position * (end - start));
    }

    /**
     *
     * @param r
     * @param x
     * @param y
     * @return whether MotionEvent is performing on bar or not
     */
    private boolean isOnBar(Rect r, float x, float y){
        return r.left - mThumbRadius < x && x < r.right + mThumbRadius && r.top - mThumbRadius < y
                && y < r.bottom + mThumbRadius;
    }

    // ----- Getters and Setters ----- //

    /**
     *
     * @param id
     * @return
     */
    private int[] getColorsById(int id){
        if(isInEditMode()){
            String[] s = mContext.getResources().getStringArray(id);
            int[] colors = new int[s.length];
            for (int j = 0; j < s.length; j++){
                colors[j] = Color.parseColor(s[j]);
            }
            return colors;
        } else {
            TypedArray typedArray = mContext.getResources().obtainTypedArray(id);
            int[] colors = new int[typedArray.length()];
            for (int j = 0; j < typedArray.length(); j++){
                colors[j] = typedArray.getColor(j,Color.BLACK);
            }
            typedArray.recycle();
            return colors;
        }
    }


    // ----- Pin methods ----- //

    /**
     * Moves the thumb to the given x-coordinate.
     *
     * @param thumb the thumb to move
     * @param x     the x-coordinate to move the thumb to
     */
    private void movePin(PinView thumb, float x) {
        if (thumb != null) {
            thumb.setX(x);
            invalidate();
        }
    }

    /**
     * Set the thumb to be in the pressed state and calls invalidate() to redraw
     * the canvas to reflect the updated state.
     *
     * @param thumb the thumb to press
     */
    private void pressPin(final PinView thumb) {
        if (mIsPinTemporary) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 36);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float mThumbRadiusDP = (float) (animation.getAnimatedValue());
                    thumb.setSize(mThumbRadiusDP, ((mThumbHeight / 4) + 36) * animation.getAnimatedFraction());
                    invalidate();
                }
            });
            animator.start();
        }

        thumb.press();
    }

    /**
     * Set the thumb to be in the normal/un-pressed state and calls invalidate()
     * to redraw the canvas to reflect the updated state.
     *
     * @param thumb the thumb to release
     */
    private void releasePin(final PinView thumb) {
        if (mIsPinTemporary) {
            ValueAnimator animator = ValueAnimator.ofFloat(36, 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float mThumbRadiusDP = (float) (animation.getAnimatedValue());
                    thumb.setSize(mThumbRadiusDP,
                            (mThumbHeight / 4) + 36 - (((mThumbHeight / 4) + 36) * animation.getAnimatedFraction()));
                    invalidate();
                }
            });
            animator.start();
        } else {
            invalidate();
        }

        thumb.release();
    }

    // Others

    public int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
