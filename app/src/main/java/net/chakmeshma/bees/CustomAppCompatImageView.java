package net.chakmeshma.bees;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by chakmeshma on 14.05.2017.
 */

public final class CustomAppCompatImageView extends AppCompatImageView {
    private Runnable nextDrawCallback;
    private float prcWidth;
    private float prcHeight;
    private boolean prcDimensSet = false;

    public CustomAppCompatImageView(Context context) {
        super(context);
    }

    public CustomAppCompatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        readCustomAttributes(context, attrs, 0);
    }

    public CustomAppCompatImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        readCustomAttributes(context, attrs, defStyleAttr);
    }

    private void readCustomAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray customAttributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomAppCompatImageView, defStyleAttr, 0);

        try {
            this.prcWidth = customAttributes.getFloat(R.styleable.CustomAppCompatImageView_refPrcWidth, 0.0f);
            this.prcHeight = customAttributes.getFloat(R.styleable.CustomAppCompatImageView_refPrcHeight, 0.0f);

            prcDimensSet = true;
        } finally {
            customAttributes.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.prcDimensSet) {
            float scaleReference;

            if (!this.isInEditMode()) {
                scaleReference = GameActivity.getScaleReferenceNumber();
            } else {
                scaleReference = 768;
            }

            float fTargetWidth = (prcWidth / 100.0f) * scaleReference;
            float fTargetHeight = (prcHeight / 100.0f) * scaleReference;

            int targetWidth = Math.round(fTargetWidth);
            int targetHeight = Math.round(fTargetHeight);

            setMeasuredDimension(targetWidth, targetHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (nextDrawCallback != null) {
            nextDrawCallback.run();

            nextDrawCallback = null;
        }
    }

    void setOnNextDrawOnceCallback(Runnable runnable) {
        nextDrawCallback = runnable;
    }
}
