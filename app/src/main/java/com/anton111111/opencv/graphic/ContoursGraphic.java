
package com.anton111111.opencv.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay.Graphic;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.List;


public class ContoursGraphic extends Graphic {

    private static final int COLOR = Color.CYAN;
    private static final float POINT_WIDTH = 2.0f;

    private final Paint paint;
    private final List<MatOfPoint> contours;

    public ContoursGraphic(GraphicOverlay overlay, List<MatOfPoint> contours) {
        super(overlay);

        this.contours = contours;

        this.paint = new Paint();
        paint.setColor(COLOR);
        paint.setStrokeWidth(POINT_WIDTH);

        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }


    @Override
    public void draw(Canvas canvas) {

        if (contours == null || contours.size() <= 0) {
            Log.e("TAG", "Contours list is empty");
            return;
        }
        for (MatOfPoint contour : contours) {
            Point[] points = contour.toArray();
            for (Point point : points) {
                canvas.drawPoint(
                        translateX((float) point.x),
                        translateY((float) point.y),
                        paint);
            }
        }

    }
}
