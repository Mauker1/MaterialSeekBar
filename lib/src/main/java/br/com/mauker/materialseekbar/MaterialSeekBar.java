package br.com.mauker.materialseekbar;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
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

    private static final int DEFAULT_HEIGHT = 150;

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
    private boolean mIsPinPermanent;

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
        // applyStyle(context, attrs, defStyleAttr, defStyleRes);
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

        Log.d(LOG_TAG,"onMeasure w: " + mViewWidth + " height: " + mViewHeight);
//        Log.d(LOG_TAG,"onMeasure new h: " + (mThumbHeight + mBarHeight));
        setMeasuredDimension(mViewWidth, mViewHeight + 80);
    }

    // ----- Touch events ----- //

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mIsPressed = true;
                if(isOnBar(mColorRect, x, y)){
                    mIsMoving = true;
                }
                pressPin(mPin);
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if(mIsMoving){
                    // TODO - This is the actual value from within the range.
                    float value = (x - mRealLeft) / mBarWidth * mMaxValue;
                    Log.d(LOG_TAG,"Value: " + value);
                    mCurrentValue = (int) value;
                    Log.d(LOG_TAG,"Value (int): " + mCurrentValue);

                    if (mCurrentValue < 0) mCurrentValue = 0;
                    if (mCurrentValue > mMaxValue) mCurrentValue = mMaxValue;

                    // Change the pin value to the current position.
                    mPin.setXValue(String.valueOf(mCurrentValue));

                    onActionMove(x);

                }

                // TODO - Create listener.
//                if(mOnColorChangeLister != null && mIsMoving)
//                    mOnColorChangeLister.onColorChangeListener(mCurrentValue, mAlphaBarValue,getColor());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mIsPressed = false;
                mIsMoving = false;
                releasePin(mPin);
                break;
        }
        return true;
    }

    // TODO - x = colorPosition + mRealLeft
    private void onActionMove(float x) {
        float colorPosition = (float) mCurrentValue / mMaxValue * mBarWidth;
        mPin.setColor(pickColor(colorPosition));
        movePin(mPin, x);
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
        if (!mIsPinPermanent) {
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
//
//        final float nearestTickX = mBar.getNearestTickCoordinate(thumb);
//        thumb.setX(nearestTickX);
//        int tickIndex = mBar.getNearestTickIndex(thumb);
//        thumb.setXValue(getPinValue(tickIndex));

        if (!mIsPinPermanent) {
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
}
