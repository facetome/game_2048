package basic.com.game2048.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import basic.com.game2048.view.ContainerView.Direction;

/**
 * Created by basic on 2017/2/7.
 */
public class Cell extends View {

    private int mIndex; //index用于定义该view在棋盘格中的位置，比如 1*1位置为1，且会随着位置的改变而发生变化.
    private int mValue; //用于记录当前的格子的值.
    private boolean mCanMove = true;  //是否能够移动.比如在当前检索中，已经发生了一次位移，那么在此检索其的时候，将不能发生移动
    private boolean mIsShowing = true; //是否已经显示
    private boolean mRandomPosition = false;
    private int mLeft;

    public Cell(Context context) {
        super(context);
    }

    public Cell(Context context, int value, int index) {
        this(context);
        mValue = value;
        mIndex = index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setColor(Constants.getColorInValue(mValue));
        Rect rectF = new Rect(0, 0, width, height);
        canvas.drawRect(rectF, paint);
        //显示缩放动画
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setTextSize(getContext().getResources().getDisplayMetrics().density * 30);
        //获取文本的占用范围
        Rect textBound = new Rect();
        String text = String.valueOf(mValue);
        paint.getTextBounds(text, 0, text.length(), textBound);
        paint.setTextAlign(Align.LEFT);
        Rect textOriginRect = measureTextLocation(textBound);
        canvas.drawText(text, textOriginRect.left, textOriginRect.bottom, paint);
        mLeft = getLeft();
    }

    /**
     * 进行属性动画的移动.
     * 使用动画进行移动存在问题。
     *
     * @param tragetX   x方向移动距离
     * @param tragetY   y方向移动距离.
     * @param direction 移动方向.
     */
    public void moveToPosition(int tragetX, int tragetY, Direction direction) {
        ObjectAnimator animator;
        if (direction == Direction.LEFT) {
            animator = ObjectAnimator.ofFloat(this, "TranslationX", 0, -tragetX);
        } else if (direction == Direction.TOP) {
            animator = ObjectAnimator.ofFloat(this, "TranslationY", 0, -tragetY);
        } else if (direction == Direction.RIGHT) {
            animator = ObjectAnimator.ofFloat(this, "TranslationX", 0, tragetX);
        } else {
            animator = ObjectAnimator.ofFloat(this, "TranslationY", 0, tragetY);
        }
        animator.setRepeatCount(0);
        animator.setDuration(100);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            animator.setAutoCancel(true);
        }
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setTranslationX(0);   //由于执行layout的同时，如果之前有translate，则为在layout之后重新执行该位移
                setTranslationY(0);
                ContainerView parent = (ContainerView) getParent();
                if (parent != null) {
                    parent.refreshLocation(Cell.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    /**
     * @param tragetX
     * @param tragetY
     * @param direction
     * @deprecated 移动效果不好看，后面尝试新方法实现
     */
    @Deprecated
    public void movePosition(int tragetX, int tragetY, Direction direction) {
        TranslateAnimation animation = null;
        switch (direction.getValue()) {
            case Direction.LEFT_DIRECTION:
                animation = new TranslateAnimation(0, -tragetX, 0, 0);
                break;
            case Direction.TOP_DIRECTION:
                animation = new TranslateAnimation(0, 0, 0, -tragetY);
                break;
            case Direction.RIGHT_DIRECTION:
                animation = new TranslateAnimation(0, tragetX, 0, 0);
                break;
            case Direction.BOTTOM_DIRECTION:
                animation = new TranslateAnimation(0, 0, 0, tragetY);
                break;
            default:
                break;
        }
        if (animation != null) {
            animation.setDuration(100);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //由于属性动画执行后x，y等位置信息发生变化，但是getLeft等信息不回发生变化，再次执行属性动画的时候仍然是以该view
                    // 最初的位置为基础进行动画平移，所以这里我们更新view的实际位置信息.
                    ContainerView parent = (ContainerView) getParent();
                    parent.refreshLocation(Cell.this);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            startAnimation(animation);
        }
    }


    public void moveViewByAnimation(int tragetX, int tragetY, Direction direction) {
        ValueAnimator animator = null;
        final boolean[] isMoveByX = new boolean[1];
        isMoveByX[0] = false;
        switch (direction.getValue()) {
            case Direction.LEFT_DIRECTION:
                animator = ValueAnimator.ofInt(0, -tragetX);
                isMoveByX[0] = true;
                break;
            case Direction.RIGHT_DIRECTION:
                animator = ValueAnimator.ofInt(0, tragetX);
                isMoveByX[0] = true;
                break;
            case Direction.TOP_DIRECTION:
                animator = ValueAnimator.ofInt(0, -tragetY);
                isMoveByX[0] = false;
                break;
            case Direction.BOTTOM_DIRECTION:
                animator = ValueAnimator.ofInt(0, tragetY);
                isMoveByX[0] = false;
                break;
            default:
                break;
        }
        if (animator != null) {
            animator.setDuration(100);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (isMoveByX[0]) {
                        setTranslationX(value);
                    } else {
                        setTranslationY(value);
                    }
                }
            });
            animator.start();
        }


    }

    public void startScaleAnimation() {
        ScaleAnimation animation = new ScaleAnimation(0.01f, 1f,
                0.01f, 1f, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        animation.setDuration(300);
        animation.setFillAfter(true);
        animation.setInterpolator(new AnticipateOvershootInterpolator());
        animation.setRepeatCount(0);
        startAnimation(animation);
    }

    //绘制文本的时候是以左下角为基准点（未设置align的情况下，baseLine为字符的底部基准线）
    private Rect measureTextLocation(Rect textBound) {
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        Rect result = new Rect();
        result.left = (viewWidth - textBound.width()) / 2;
        result.bottom = (viewHeight + textBound.height()) / 2;
        return result;
    }

    //--------------------------------------------------------------------------------------------------------------------

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
        if (isShowing()) {
            invalidate();
        }
    }

    public boolean isCanMove() {
        return mCanMove;
    }

    public void setCanMove(boolean canMove) {
        mCanMove = canMove;
    }

    public boolean isRandomPosition() {
        return mRandomPosition;
    }

    public void setRandomPosition(boolean randomPosition) {
        mRandomPosition = randomPosition;
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void setShowing(boolean showing) {
        mIsShowing = showing;
    }
}
