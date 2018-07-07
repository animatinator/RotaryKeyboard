package com.animatinator.rotarykeyboard.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.animatinator.rotarykeyboard.util.CoordinateUtils;
import com.animatinator.rotarykeyboard.util.Coordinates;

import java.util.ArrayList;
import java.util.Optional;

public class RotaryKeyboardView extends View implements View.OnTouchListener {
    // The position along the circle's radius of the letters.
    // 0.0 = in the centre, 1.0 = on the edge.
    private static final float LETTER_RADIUS_RATIO = 0.8f;
    // Radius within which a motion event will count as hitting a letter.
    private static final float LETTER_HIT_RADIUS = 150f;
    // Radius of the circle drawn around a letter when it is selected.
    private static final float LETTER_HIGHLIGHT_RADIUS = 100.0f;

    private WordEntryCallback wordEntryCallback = null;
    private Paint backgroundPaint;
    private Paint linePaint;
    private Paint letterHighlightPaint;
    private Paint circlePaint;
    private Paint textPaint;

    private Coordinates bottomRight = null;

    private String[] letters = null;
    private Coordinates[] letterPositions = null;

    private boolean isDragging = false;
    private Coordinates rootPos = new Coordinates(0, 0);
    private Coordinates currentPos = new Coordinates(0, 0);
    private ArrayList<Integer> selectedLetters = new ArrayList<>();

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
        linePaint.setStrokeWidth(50.0f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        letterHighlightPaint = new Paint();
        letterHighlightPaint.setColor(Color.BLUE);
        letterHighlightPaint.setAlpha(150);
        letterHighlightPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(200.0f);
    }

    public void setLetters(String[] letters) {
        this.letters = letters;
        recomputeLetterPositions();
    }

    public void setWordEntryCallback(WordEntryCallback wordEntryCallback) {
        this.wordEntryCallback = wordEntryCallback;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Coordinates circleCentre = getCircleCentre();
        canvas.drawPaint(backgroundPaint);
        canvas.drawCircle(circleCentre.x(), circleCentre.y(), getCircleRadius(), circlePaint);

        if (isDragging) {
            drawSelectedLetterTrail(canvas);
        }

        drawLetters(canvas);
    }

    private void drawSelectedLetterTrail(Canvas canvas) {
        if (selectedLetters == null) {
            return;
        }

        // TODO we're assuming that the letters and positions can't be null here.

        Coordinates lastLetterPosition = null;

        for (Integer letterIndex : selectedLetters) {
            Coordinates letterPosition = letterPositions[letterIndex];

            if (lastLetterPosition != null) {
                canvas.drawLine(
                        lastLetterPosition.x(),
                        lastLetterPosition.y(),
                        letterPosition.x(),
                        letterPosition.y(),
                        linePaint);
            }

            canvas.drawCircle(
                    letterPosition.x(), letterPosition.y(), LETTER_HIGHLIGHT_RADIUS, letterHighlightPaint);
            lastLetterPosition = letterPosition;
        }

        if (lastLetterPosition != null) {
            canvas.drawLine(lastLetterPosition.x(), lastLetterPosition.y(), currentPos.x(), currentPos.y(), linePaint);
        }
    }

    private void drawLetters(Canvas canvas) {
        if (letters == null || letterPositions == null) {
            return;
        }

        for (int i = 0; i < letters.length; i++) {
            String letter = letters[i];
            Coordinates position = letterPositions[i];
            // Centre vertically.
            float yPos = position.y() - ((textPaint.ascent() + textPaint.descent()) / 2.0f);
            canvas.drawText(letter, position.x(), yPos, textPaint);
        }
    }

    private Coordinates getCircleCentre() {
        return new Coordinates(bottomRight.x() / 2, bottomRight.y() / 2);
    }

    private float getCircleRadius() {
        return Math.min(bottomRight.x(), bottomRight.y()) / 2;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            bottomRight = new Coordinates(right - left, bottom - top);
            recomputeLetterPositions();
        }
    }

    private void recomputeLetterPositions() {
        if (letters != null && bottomRight != null) {
            Coordinates circleCentre = getCircleCentre();
            float circleRadius = getCircleRadius();
            int numLetters = letters.length;

            letterPositions = new Coordinates[numLetters];
            Log.e("test", "Got this many letters: "+numLetters);

            float letterDistanceFromCentre = circleRadius * LETTER_RADIUS_RATIO;

            for (int i = 0; i < numLetters; i++) {
                Log.e("test", "Letter: '"+letters[i]+"'.");
                double angle = (Math.PI * 2 * i) / numLetters;

                float xFact = (float) Math.sin(angle);
                float yFact = (float) Math.cos(angle);
                float xPos = circleCentre.x() + (letterDistanceFromCentre * xFact);
                float yPos = circleCentre.y() - (letterDistanceFromCentre * yFact);

                letterPositions[i] = new Coordinates(xPos, yPos);
            }
        }
    }

    @SuppressLint("NewApi")
    private Optional<Integer> getAdjacentLetter(Coordinates position) {
        for (int i = 0; i < letterPositions.length; i++) {
            Coordinates letterPosition = letterPositions[i];
            if (CoordinateUtils.distance(letterPosition, position) < LETTER_HIT_RADIUS) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        bottomRight = new Coordinates(width, height);
        recomputeLetterPositions();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;

        Coordinates position = new Coordinates(event.getX(), event.getY());
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            isDragging = false;
            selectedLetters = new ArrayList<>();
            wordEntryCallback.onWordEntered("Test text");
            invalidate();

        } else if (action == MotionEvent.ACTION_DOWN) {
            Optional<Integer> selectedLetter = getAdjacentLetter(position);
            if (selectedLetter.isPresent()) {
                isDragging = true;
                rootPos = new Coordinates(event.getX(), event.getY());
                currentPos = new Coordinates(event.getX(), event.getY());
                selectedLetters = new ArrayList<>();
                selectedLetters.add(selectedLetter.get());
                handled = true;
            }

        } else if (action == MotionEvent.ACTION_MOVE) {
            Optional<Integer> selectedLetter = getAdjacentLetter(position);
            if (selectedLetter.isPresent()) {
                // TODO handle case where already selected.
                selectedLetters.add(selectedLetter.get());
            }
            currentPos = position;
            invalidate();
            handled = true;

        }

        if (handled) invalidate();

        return handled;
    }

    public interface WordEntryCallback {
        void onWordEntered(String word);
    }
}
