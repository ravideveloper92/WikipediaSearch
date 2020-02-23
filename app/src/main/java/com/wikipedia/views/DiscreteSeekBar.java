package com.wikipedia.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.wikipedia.R;

@SuppressLint("AppCompatCustomView")
public class DiscreteSeekBar extends SeekBar {
    private int min;
    @Nullable private Drawable tickDrawable;
    @Nullable private Drawable centerDrawable;
    private boolean isRtl;

    public DiscreteSeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public DiscreteSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DiscreteSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public int getValue() {
        return getProgress() + min;
    }

    public void setValue(int value) {
        setProgress(value - min);
    }

    private void init(@Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs,
                    R.styleable.DiscreteSeekBar, defStyleAttr, 0);
            min = array.getInteger(R.styleable.DiscreteSeekBar_min, 0);
            setMax(getMax() - min);
            int id = array.getResourceId(R.styleable.DiscreteSeekBar_tickDrawable, 0);
            if (id != 0) {
                tickDrawable = AppCompatResources.getDrawable(getContext(), id);
            }
            id = array.getResourceId(R.styleable.DiscreteSeekBar_centerDrawable, 0);
            if (id != 0) {
                centerDrawable = AppCompatResources.getDrawable(getContext(), id);
            }
            array.recycle();
        }
        isRtl = getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        int value = getValue();
        if (value >= 0) {
            drawTickMarks(canvas, true, false);
            super.onDraw(canvas);
            drawTickMarks(canvas, false, true);
        } else {
            super.onDraw(canvas);
            drawTickMarks(canvas, true, true);
        }
    }

    void drawTickMarks(@NonNull Canvas canvas, boolean drawCenter, boolean drawOther) {
        int max = getMax() + min;
        int value = getValue();
        if (tickDrawable != null) {
            int halfW = tickDrawable.getIntrinsicWidth() >= 0 ? tickDrawable.getIntrinsicWidth() / 2 : 1;
            int halfH = tickDrawable.getIntrinsicHeight() >= 0 ? tickDrawable.getIntrinsicHeight() / 2 : 1;
            tickDrawable.setBounds(-halfW, -halfH, halfW, halfH);
        }
        if (centerDrawable != null) {
            int halfW = centerDrawable.getIntrinsicWidth() >= 0 ? centerDrawable.getIntrinsicWidth() / 2 : 1;
            int halfH = centerDrawable.getIntrinsicHeight() >= 0 ? centerDrawable.getIntrinsicHeight() / 2 : 1;
            centerDrawable.setBounds(-halfW, -halfH, halfW, halfH);
        }
        float tickSpacing = (float) (getWidth() - getPaddingLeft() - getPaddingRight()) / (float) (max - min);
        canvas.save();
        if (isRtl) {
            canvas.scale(-1, 1, getWidth() / 2, getHeight() / 2);
        }
        canvas.translate((float) getPaddingLeft(), (float) (getHeight() / 2));
        for (int i = min; i <= max; ++i) {
            if (drawOther && tickDrawable != null && i > value) {
                tickDrawable.draw(canvas);
            }
            if (drawCenter && i == 0 && centerDrawable != null) {
                centerDrawable.draw(canvas);
            }
            canvas.translate(tickSpacing, 0.0f);
        }
        canvas.restore();
    }
}
