package com.animatinator.rotarykeyboard.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import com.animatinator.rotarykeyboard.util.Coordinates;

public class RotaryKeyboardView extends View implements View.OnTouchListener {
    private WordEntryCallback wordEntryCallback = null;
    private Paint backgroundPaint;
    private Paint linePaint;
    private Paint circlePaint;

    private boolean isDragging = false;
    private Coordinates rootPos = new Coordinates(0, 0);
    private Coordinates currentPos = new Coordinates(0, 0);

    public RotaryKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initPaints();
    }

    private void initPaints() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setAlpha(0);
        backgroundPaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint();
        circlePaint.setColor(Color.LTGRAY);
        circlePaint.setAlpha(200);

        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setAlpha(150);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(100.0f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setWordEntryCallback(WordEntryCallback wordEntryCallback) {
        this.wordEntryCallback = wordEntryCallback;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d("test", "onDraw");
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int diameter = Math.min(width, height);
        int centreX = width / 2;
        int centerY = height / 2;
        canvas.drawPaint(backgroundPaint);
        canvas.drawCircle(centreX, centerY, diameter / 2, circlePaint);

        if (isDragging) {
            Log.d("test", "draw the line");
            canvas.drawLine(rootPos.x(), rootPos.y(), currentPos.x(), currentPos.y(), linePaint);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;

        switch(event.getAction()) {
            case MotionEvent.ACTION_UP:
                isDragging = false;
                wordEntryCallback.onWordEntered("Test text");
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                isDragging = true;
                rootPos = new Coordinates(event.getX(), event.getY());
                currentPos = new Coordinates(event.getX(), event.getY());
                handled = true;
                break;
            case MotionEvent.ACTION_MOVE:
                currentPos = new Coordinates(event.getX(), event.getY());
                invalidate();
                handled = true;
                break;
            default:
                // Nothing to do.
                break;
        }

        if (handled) invalidate();

        return handled;
    }

    public interface WordEntryCallback {
        void onWordEntered(String word);
    }
}
