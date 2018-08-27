
package com.anton111111.opencv.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay.Graphic;

import org.opencv.core.Point;
import org.opencv.core.RotatedRect;


public class ROIGraphic extends Graphic {

    private static final int COLOR = Color.YELLOW;
    private static final float STROKE_WIDTH = 2.0f;
    private static final float POINT_RADIUS = 5.0f;

    private final RotatedRect rotatedRect;
    private final Paint paint;

    public ROIGraphic(GraphicOverlay overlay, RotatedRect rotatedRect) {
        super(overlay);

        this.rotatedRect = rotatedRect;

        this.paint = new Paint();
        paint.setColor(COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);


        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }


    @Override
    public void draw(Canvas canvas) {


        if (rotatedRect != null) {
            Point pts[] = new Point[4];
            rotatedRect.points(pts);

            for (Point pt : pts) {
                canvas.drawCircle((float) pt.x, (float) pt.y, POINT_RADIUS, paint);
            }

        }
    }
}
