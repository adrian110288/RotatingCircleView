package com.adrian.customdrawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by adrian on 04/12/14.
 */
public class CustomImage extends View implements View.OnTouchListener{

    private static final String textSuffix = "";
    private int displayNumber = 0;

    private Paint paint;
    private TextPaint textPaint;
    private Path outerCirclePath;
    private Path innerCirclePath;
    private int OUTER_RADIUS;
    private int INNER_RADIUS;

    private RadialGradient shader;
    private RadialGradient onTouchShader;
    private RadialGradient currentShader;

    private Canvas canvas;
    private int centerX;
    private int centerY;

    private float degree = 0;

    public CustomImage(Context context, AttributeSet attrs) {
        super(context, attrs);

        createPaint();
        createTextPaint();
        setOnTouchListener(this);
    }

    private void createShaders() {
        shader = new RadialGradient(centerX, centerY, OUTER_RADIUS, new int[]{Color.parseColor("#ccff00"), Color.parseColor("#1d479e")}, new float[]{0, 1}, Shader.TileMode.CLAMP);
        onTouchShader = new RadialGradient(centerX, centerY, OUTER_RADIUS, new int[]{Color.parseColor("#ccff00"), Color.parseColor("#006400")}, new float[]{0, 1}, Shader.TileMode.CLAMP);

        currentShader = shader;
    }

    private void createPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
    }

    private void createTextPaint() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(60);
        textPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "beatles_light_light.ttf"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        calculateCenter();
        calculateRadius();

        if(shader ==null || onTouchShader == null) {
            createShaders();
        }

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(degree, centerX, centerY);

        drawOuterCircle();
        drawPoints();

        canvas.restore();

        drawInnerCircle();
        drawText();
        drawIndicator();

    }

    private void drawIndicator() {

        paint.setColor(Color.parseColor("#C81616"));

        int indicatorOriginX = centerX;
        int indicatorOriginY = centerY - INNER_RADIUS;

        Path indicatorPath = new Path();
        indicatorPath.moveTo(indicatorOriginX, indicatorOriginY);
        indicatorPath.lineTo(indicatorOriginX - 25, indicatorOriginY + 40);
        indicatorPath.lineTo(indicatorOriginX + 25, indicatorOriginY + 40);
        indicatorPath.lineTo(indicatorOriginX, indicatorOriginY);
        indicatorPath.close();

        canvas.drawPath(indicatorPath, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#260000"));
        canvas.drawPath(indicatorPath, paint);
    }

    private void calculateCenter() {
        centerX = canvas.getWidth() / 2;
        centerY = canvas.getHeight() / 2;
    }

    private void drawText() {

        String displayText = displayNumber + textSuffix;

        canvas.drawText(displayText, centerX - (textPaint.measureText(displayText) / 2), centerY - (textPaint.ascent() + textPaint.descent()) / 2, textPaint);
    }

    private void calculateRadius() {

        if(canvas.getHeight() > canvas.getWidth()) {
            OUTER_RADIUS = centerX;
            INNER_RADIUS = centerX - 110;
        } else {
            OUTER_RADIUS = centerY;
            INNER_RADIUS = (centerY) - 110;
        }
    }

    private void drawOuterCircle() {

        paint.setColor(Color.BLACK);
        paint.setShader(currentShader);
        paint.setStyle(Paint.Style.FILL);

        outerCirclePath = new Path();
        outerCirclePath.addCircle(centerX ,centerY, OUTER_RADIUS, Path.Direction.CW);

        canvas.drawPath(outerCirclePath, paint);

        paint.setShader(null);
    }

    private void drawInnerCircle() {

        paint.setColor(Color.WHITE);

        innerCirclePath = new Path();
        innerCirclePath.addCircle(centerX, centerY, INNER_RADIUS, Path.Direction.CW);

        canvas.drawPath(innerCirclePath, paint);
    }

    private void drawPoints() {

        paint.setColor(Color.WHITE);
        canvas.clipPath(outerCirclePath);

        for(int i=0;i<36; i++) {

            int noOfDraws = ((i*10)%90 == 0) ? 42 : 16;

            for(int j=0;j<noOfDraws; j++) {
                int dotX = (int) ((OUTER_RADIUS - j) * Math.cos(Math.toRadians(i * 10)) + centerX);
                int dotY = (int) ((OUTER_RADIUS - j) * Math.sin(Math.toRadians(i * 10)) + centerY);

                canvas.drawCircle(dotX, dotY, 2, paint);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            currentShader = onTouchShader;
            postInvalidate();
            updateRotation(event);

        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            currentShader = shader;
            postInvalidate();

        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            updateRotation(event);
        }

        return true;
    }

    private void updateRotation(MotionEvent event) {

        double touchedX = (centerX - event.getX());
        double touchedY = (centerY - event.getY());

        float degreeSigned = (float) Math.toDegrees(Math.atan2(touchedY, touchedX));
        degree = ((degreeSigned + 360) % 360);

        if(degree < 0) {
            degree = 360 - Math.abs(degree);
        }

        displayNumber = (int)degree;

        postInvalidate();
    }

}
