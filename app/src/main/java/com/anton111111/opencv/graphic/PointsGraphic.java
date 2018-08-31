
package com.anton111111.opencv.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay.Graphic;

import org.opencv.core.Point;

import java.util.List;


public class PointsGraphic extends Graphic {

    private static final int COLOR = Color.YELLOW;
    private static final float STROKE_WIDTH = 2.0f;
    private static final float POINT_RADIUS = 5.0f;

    private final List<Point> points;
    private final Paint paint;

    public PointsGraphic(GraphicOverlay overlay, List<Point> points) {
        super(overlay);

        this.points = points;

        this.paint = new Paint();
        paint.setColor(COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);

        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }


    @Override
    public void draw(Canvas canvas) {
        if (points != null && points.size() > 0) {
            for (Point pt : points) {
                canvas.drawCircle(
                        translateX((float) pt.x),
                        translateY((float) pt.y),
                        POINT_RADIUS, paint);
            }

        }
    }
}
