package com.kai_jan_57.laztris.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;

public class PlayingFieldView extends android.support.v7.widget.AppCompatImageView {

    public PlayingFieldView(Context context) {
        super(context);
    }

    public PlayingFieldView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public interface TickEvent {
        boolean onTick(PlayingFieldView caller, long time);
    }

    private TickEvent tickEventHandler;
    public void registerTickEvent(TickEvent tickEvent) {
        tickEventHandler = tickEvent;
    }

    private long nextTick;
    public void setNextTick(long nextTick) {
        this.nextTick = nextTick;
        invalidate();
    }

    public long getNextTick() {
        return nextTick;
    }

    public static long getTime() {
        return SystemClock.elapsedRealtime();
    }

    public Canvas canvas;

    @Override
    public void onDraw(Canvas canvas) {
        long time = getTime();
        if (nextTick <= time) {
            if (tickEventHandler != null) {
                this.canvas = canvas;
                tickEventHandler.onTick(this, time);
            }
        }
        else {
            invalidate();
        }
        super.onDraw(canvas);
    }
}
