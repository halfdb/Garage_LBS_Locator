package ecnu.cs14.garagelocation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import ecnu.cs14.garagelocation.data.Map;

import java.util.HashSet;

/**
 * An experimental MapView.
 * Created by K on 2017/1/21.
 */

public class MapView extends View {
    private final static String TAG = MapView.class.getName();

    private Paint mShapePaint;
    private Paint mBackgroundPaint;
    private Paint mPositionDotPaint;
    private View mEmptyView;

    public MapView(Context context) {
        super(context);
        initPaint();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public void setEmptyView(@NonNull View v) {
        mEmptyView = v;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != VISIBLE) {
            mEmptyView.setVisibility(VISIBLE);
        } else {
            mEmptyView.setVisibility(GONE);
        }
    }

    private void initPaint() {
        mShapePaint = new Paint();
        mShapePaint.setColor(Color.DKGRAY);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.LTGRAY);
        mPositionDotPaint = new Paint();
        mPositionDotPaint.setColor(Color.BLUE);
    }

    private Rect mBackgroundRect = new Rect();
    private HashSet<Map.Shape.Rect> mRects = new HashSet<>();
    private HashSet<Map.Shape.Circle> mCircles = new HashSet<>();
    public void setMap(Map map) {
        mBackgroundRect = new Rect(
                0,
                0,
                map.width,
                map.height
        );
        for (Map.Shape shape: map.shapes) {
            switch (shape.type) {
                case RECT:
                {
                    mRects.add((Map.Shape.Rect) shape);
                    break;
                }
                case CIRCLE:
                {
                    mCircles.add((Map.Shape.Circle) shape);
                    break;
                }
            }
        }
        invalidate();
    }

    private HashSet<Pair<Integer, Integer>> mPositions = new HashSet<>();
    private static final float DOT_RADIUS = 5.0f;
    public void drawSampleDot(Pair<Integer, Integer> position) {
        mPositions.add(position);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.i(TAG, "onMeasure: wMode: " + wMode + " hMode: " + hMode + " wSize: " + wSize + " hSize: " + hSize);
        setMeasuredDimension(wSize, hSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sx = (float) getWidth() / (float) mBackgroundRect.right;
        float sy = (float) getHeight() / (float) mBackgroundRect.bottom;
        float s = sx < sy ? sx : sy;
        canvas.translate(
                (getWidth() / 2f - mBackgroundRect.centerX() * s),
                (getHeight() / 2f - mBackgroundRect.centerY() * s)
        );
        canvas.scale(s, s, 0, 0);
        canvas.clipRect(mBackgroundRect);
        canvas.drawRect(mBackgroundRect, mBackgroundPaint);
        for (Map.Shape.Rect rect :
                mRects) {
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, mShapePaint);
        }
        for (Map.Shape.Circle circle :
                mCircles) {
            canvas.drawCircle(circle.center_left, circle.center_top, circle.radius, mShapePaint);
        }
        for (Pair<Integer, Integer> p :
                mPositions) {
            canvas.drawCircle((float) p.first, (float) p.second, DOT_RADIUS, mPositionDotPaint);
        }
    }
}
