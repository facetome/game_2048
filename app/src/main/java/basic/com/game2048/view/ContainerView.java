package basic.com.game2048.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import java.util.Random;

/**
 * 2048主窗体控件.
 */
public class ContainerView extends ViewGroup implements OnTouchListener {

    private static final int DEFAULT_CELL_COLUMN_COUNT = 4;
    private static final float DEFAULT_ITEM_PADDING = 10.F;
    private int mColumnCount = DEFAULT_CELL_COLUMN_COUNT; //单元列
    private int mCellCount = mColumnCount * mColumnCount;
    private GestureDetector mGestureDetector;
    private float mPadding;
    private float mItemWidth; // 每个cell的大小.
    private boolean mFirst = true;
    //存储当前已在棋盘中的格子，每移动一个位置，需要记录位置信息
    private SparseArray<Cell> mCellSparseArray = new SparseArray<>();

    public ContainerView(Context context) {
        this(context, null);
    }

    public ContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mGestureDetector = new GestureDetector(context, new MyGestureListener());
        setOnTouchListener(this);
        setLongClickable(true); //必须加才能响应fling.
        //取最小padding作为整个view的padding，同时也作为每个item的padding.
        mPadding = Math.min(Math.min(getPaddingLeft(), getPaddingRight()), Math.min
                (getPaddingBottom(), getPaddingTop()));
        mPadding = mPadding > 0 ? mPadding : DEFAULT_ITEM_PADDING;
//        setBackgroundColor(Color.parseColor("#5AF9E6BC"));
        setBackgroundColor(Color.RED);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        //随机将view进行位置的放置.
        for (int index = 0; index < childCount; index++) {
            Cell cell = (Cell) getChildAt(index);
            int cellIndex = mCellSparseArray.indexOfValue(cell);
            if (cellIndex > -1) { //能够在array中找到该cell，即之前已经添加数据.
                // TODO 这里是否还需要将之前的cell再次layout
                Rect location = calculateLocationByIndex(cell.getIndex());
//                cell.layout(location.left, location.top, location.right, location.bottom);
            } else {
                int randomIndex = calculateCellIndex();
                cell.setIndex(randomIndex);
                //根据random计算当前cell的位置.
                Rect location = calculateLocationByIndex(randomIndex);
                cell.layout(location.left, location.top, location.right, location.bottom);
                mCellSparseArray.put(randomIndex, cell);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewWidth = Math.min(getMeasuredHeight(), getMeasuredWidth()); //计算view的大小
        setMeasuredDimension(viewWidth, viewWidth);
        mItemWidth = (getMeasuredWidth() - (mColumnCount + 1) * mPadding) / mColumnCount;
        if (mFirst) {
            mFirst = false;
            //添加两个item。
            addCell(2, true);
        }
    }

    //通过index计算应该在棋盘格中的位置.
    // TODO 位置需要重新计算
    private Rect calculateLocationByIndex(int index) {
        Rect rect = new Rect();
        int columnIndex = index % mColumnCount;
        //做1个像素的偏移量，由于计算格子的大小的时候，可能存在丢失精度的问题
        rect.left = (int) (columnIndex * mItemWidth + (columnIndex + 1) * mPadding) + 1;
        rect.right = (int) ((columnIndex + 1) * (mItemWidth + mPadding));
        int romIndex = index / mColumnCount;
        rect.top = (int) (romIndex * mItemWidth + (romIndex + 1) * mPadding) + 1;
        rect.bottom = (int) ((romIndex + 1) * (mItemWidth + mPadding));
        return rect;
    }

    //计算当前棋盘中未被占据的位置.
    private int calculateCellIndex() {
        int index;
        do {
            //能够遍历到这个位置的元素
            index = randomIndex(mCellCount);
            Cell cell = mCellSparseArray.get(index);
            if (cell == null) {
                break;
            }

        } while (true);
        return index;
    }

    private int randomIndex(int max) {
        Random random = new Random();
        return random.nextInt(max);  //计算一个随机索引
    }

    public void setColumnCount(int columnCount) {
        mColumnCount = columnCount;
        mCellCount = (int) Math.pow(mColumnCount, 2);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private void addCell(int cellCount, boolean needToClear) {
        if (needToClear) {
            removeAllViews();
        }
        for (int count = 0; count < cellCount; count++) {
            Cell cell = new Cell(getContext(), 2, 0);
            LayoutParams params = new LayoutParams((int) mItemWidth, (int) mItemWidth);
            int measureWidth = MeasureSpec.makeMeasureSpec((int) mItemWidth, MeasureSpec.EXACTLY);
            cell.measure(measureWidth, measureWidth);
            addView(cell, params);
            cell.startScaleAnimation();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制水平分割线
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#C17D38"));
        paint.setAntiAlias(false);
        paint.setStrokeWidth(mPadding);
        for (int row = 0; row < mColumnCount + 1; row++) {
            float startY = row * (mPadding + mItemWidth) + mPadding / 2;
            //绘制宽线条时候，是以线条的中心点向两边扩展宽度.
            if (row == 0) {
                startY = mPadding / 2;
            }
            float stopY = startY;
            float startX = 0;
            float stopX = getMeasuredWidth();
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }

        for (int column = 0; column < mColumnCount + 1; column++) {
            float startX = column * (mPadding + mItemWidth) + mPadding / 2;
            if (column == 0) {
                startX = mPadding / 2;
            }
            float stopX = startX;
            float startY = 0;
            float stopY = getMeasuredHeight();
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }

    private void moveAndMergeBlock(final Direction direction) {
        //进行block的移动，只能使用属性动画，不适用帧动画。
        int childCount = mCellSparseArray.size();
        switch (direction.getValue()) {
            case Direction.LEFT_DIRECTION:
            case Direction.TOP_DIRECTION:
                for (int index = 0; index < childCount; index++) {  // TODO 修改为遍历mcount * mcount
                     updateBlock(index, direction);
                }
                break;
            case Direction.BOTTOM_DIRECTION:
            case Direction.RIGHT_DIRECTION:
                for (int index = childCount - 1; index >= 0; index--) {
                    updateBlock(index, direction);
                }
                break;
            default:
                break;
        }
        addCell(1, false);
    }

    //根据在array中的位置索引进行cell的检索
    private boolean updateBlock(int arrayIndex, Direction direction) {
        Cell cell = mCellSparseArray.get(mCellSparseArray.keyAt(arrayIndex));
        if (checkCellCanMove(cell, direction)) { //图块能够进行移动
            //检测当前cell可以移动的情况下，立马将该cell进行移动并更新他的位置信息.
            //计算这个cell可以移动多少位置
            int moveBlockNum = moveBlockNum(cell, direction);
            Block rect = calculateMoveDistance(moveBlockNum);
            cell.moveToPosition(rect.width(), rect.height(), direction);
            mergeBlock(cell, direction, moveBlockNum);
            return true;
        }
        return false;
    }

    // TODO 如果有合并，需要将合并前的元素delete ，同时remove掉该view。
    // TODO 需要进行图块的合并以及集合的更新
    private void mergeBlock(Cell cell, Direction direction, int moveNum) {
        int newIndex = calculateBlockIndex(cell, direction, moveNum);
        final Cell mergeBlock = mCellSparseArray.get(newIndex);
        //执行更新当前cell的元素的索引
        mCellSparseArray.remove(cell.getIndex());
        cell.setIndex(newIndex);
        mCellSparseArray.put(newIndex, cell);
        if (mergeBlock != null) {
            cell.setValue(cell.getValue() * 2);
            removeView(mergeBlock); //从棋盘格中将该view删掉.
        }
    }

    //计算当前cell可以移动多少个位置.
    //只考虑一次碰撞
    private int moveBlockNum(Cell cell, Direction direction) {
        int blockIndex = cell.getIndex();
        int moveMum = 0;
        int maxMoveNum;
        switch (direction.getValue()) {
            case Direction.LEFT_DIRECTION:
                maxMoveNum = blockIndex % mColumnCount;
                for (int index = 1; index <= maxMoveNum; index++) {
                    Cell mayExitsCell = mCellSparseArray.get(blockIndex - index);
                    if (mayExitsCell == null || mayExitsCell.getValue() == cell.getValue()) {
                        moveMum++;
                    }
                }
                break;
            case Direction.RIGHT_DIRECTION:
                maxMoveNum = mColumnCount - (blockIndex % mColumnCount + 1);
                for (int index = 1; index <= maxMoveNum; index++) {
                    Cell mayExitsCell = mCellSparseArray.get(blockIndex + index);
                    if (mayExitsCell == null || mayExitsCell.getValue() == cell.getValue()) {
                        moveMum++;
                    }
                }
                break;
            case Direction.BOTTOM_DIRECTION:
                maxMoveNum = mColumnCount - (blockIndex / mColumnCount + 1);
                for (int index = 1; index <= maxMoveNum; index++) {
                    Cell mayExitsCell = mCellSparseArray.get(blockIndex + index * mColumnCount);
                    if (mayExitsCell == null || mayExitsCell.getValue() == cell.getValue()) {
                        moveMum++;
                    }
                }
                break;
            case Direction.TOP_DIRECTION:
                maxMoveNum = blockIndex / mColumnCount;
                for (int index = 1; index <= maxMoveNum; index++) {
                    Cell mayExitsCell = mCellSparseArray.get(blockIndex - mColumnCount * index);
                    if (mayExitsCell == null || mayExitsCell.getValue() == cell.getValue()) {
                        moveMum++;
                    }
                }
                break;
            default:
                break;
        }
        return moveMum;
    }


    //计算当前cell新的index
    private int calculateBlockIndex(Cell cell, Direction direction, int moveNum) {
        int originIndex = cell.getIndex();
        switch (direction.getValue()) {
            case Direction.LEFT_DIRECTION:
                return originIndex - moveNum;
            case Direction.RIGHT_DIRECTION:
                return originIndex + moveNum;
            case Direction.TOP_DIRECTION:
                return originIndex - moveNum * mColumnCount;
            case Direction.BOTTOM_DIRECTION:
                return originIndex + moveNum * mColumnCount;
            default:
                break;
        }
        return 0;
    }

    private Block calculateMoveDistance(int blockNum) {
        int width = (int) (blockNum * (mPadding + mItemWidth));
        int height = (int) (blockNum * (mPadding + mItemWidth));
        return new Block(width, height);
    }

    private boolean checkCellCanMove(Cell cell, Direction direction) {
        if (cell == null) {
            return false;
        }
        int blockIndex = cell.getIndex();  //当前cell在棋盘中的格子索引
        if (direction == Direction.LEFT) {
            if (blockIndex % mColumnCount != 0) {
                Cell leftCell = mCellSparseArray.get(blockIndex - 1);
                if (leftCell == null || leftCell.getValue() == cell.getValue()) {
                    //上一个图块不存在或者两个的值一样，则可以进行叠加
                    return true;
                }
            }
        } else if (direction == Direction.TOP) {
            if (blockIndex / mColumnCount != 0) {
                Cell topCell = mCellSparseArray.get(blockIndex - mColumnCount);
                if (topCell == null || topCell.getValue() == cell.getValue()) {
                    //上一个图块不存在或者两个的值一样，则可以进行叠加
                    return true;
                }
            }

        } else if (direction == Direction.BOTTOM) {
            if (blockIndex / mColumnCount != mColumnCount - 1) { //不是最后一行
                Cell bottomCell = mCellSparseArray.get(blockIndex + mColumnCount);
                if (bottomCell == null || bottomCell.getValue() == cell.getValue()) {
                    return true;
                }
            }

        } else { //向右
            if (blockIndex % mColumnCount != mColumnCount - 1) { //不是最后一列
                Cell rightCell = mCellSparseArray.get(blockIndex + 1);
                if (rightCell == null || rightCell.getValue() == cell.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // true 消耗事件.
            Direction direction;
            float valueX = e1.getX() - e2.getX();
            float valueY = e1.getY() - e2.getY();
            //如果横向方向移动的值比竖向方向大，那么久定义为横向
            if (Math.abs(valueX) >= Math.abs(valueY)) {
                if (valueX > 0) { // 向左
                    direction = Direction.LEFT;
                } else {
                    direction = Direction.RIGHT;
                }
            } else {
                if (valueY > 0) { //向上
                    direction = Direction.TOP;
                } else {
                    direction = Direction.BOTTOM;
                }
            }
            moveAndMergeBlock(direction);
            return true;
        }
    }


    public enum Direction {

        /**
         * 向左.
         */
        LEFT(0),

        /**
         * 向上.
         */
        TOP(2),

        /**
         * 向下.
         */
        BOTTOM(3),

        /**
         * 向右.
         */
        RIGHT(1);

        public static final int LEFT_DIRECTION = 0;
        public static final int RIGHT_DIRECTION = 1;
        public static final int TOP_DIRECTION = 2;
        public static final int BOTTOM_DIRECTION = 3;
        private int mValue;

        Direction(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    /**
     * 宽度和长度的结构体.
     */
    private static class Block {
        private int width;
        private int height;

        Block(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }
    }
}
