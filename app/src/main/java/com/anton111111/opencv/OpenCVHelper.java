package com.anton111111.opencv;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Potekhin (Anton.Potekhin@gmail.com) on 21.08.18.
 */
public class OpenCVHelper {

    private static final String TAG = "OpenCVHelper";


    /**
     * Convert ByteBuffer to Mat
     *
     * @param frame
     * @param width
     * @param height
     * @return
     */
    public static Mat matFromByteBuffer(ByteBuffer frame, int width, int height, int format) {
        return matFromByteBuffer(frame, width, height, format, 0);
    }

    /**
     * Convert ByteBuffer to Mat
     *
     * @param frame
     * @param width
     * @param height
     * @param rotate result angle = rotate*90
     * @return
     */
    public static Mat matFromByteBuffer(ByteBuffer frame, int width, int height, int format, int rotate) {
        byte[] data = new byte[frame.capacity()];
        ((ByteBuffer) frame.duplicate().clear()).get(data);
Log.e("Yo", "!!!M: "+width+"x"+height);
        switch (format) {
            case android.graphics.ImageFormat.NV21:
                Mat originalMat = new Mat(new Size(width, height + height / 2), CvType.CV_8UC1);
                originalMat.put(0, 0, data);
                Mat mat = new Mat(new Size(width, height), CvType.CV_8UC3);
                Imgproc.cvtColor(originalMat, mat, Imgproc.COLOR_YUV2RGBA_NV21);

                if (rotate > 0) {
                    Mat rMat = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
                    Core.flip(mat, rMat, rotate);
                    int rotateCode;
                    switch (rotate) {
                        case 1:
                            rotateCode = Core.ROTATE_90_CLOCKWISE;
                            break;
                        case 2:
                            rotateCode = Core.ROTATE_180;
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported rotation: " + rotate);
                    }
                    Core.rotate(mat, rMat, rotateCode);
                    return rMat;
                }
                return mat;
            default:
                throw new UnsupportedOperationException("Not supported format:" + format);
        }
    }

    public static RotatedRect getBestRectByArea(List<RotatedRect> boundingRects) {
        RotatedRect bestRect = null;

        if (boundingRects.size() >= 1) {
            RotatedRect boundingRect;
            Point[] vertices = new Point[4];
            Rect rect;
            double maxArea;
            int ixMaxArea = 0;

            // find best rect by area
            boundingRect = boundingRects.get(ixMaxArea);
            boundingRect.points(vertices);
            rect = Imgproc.boundingRect(new MatOfPoint(vertices));
            maxArea = rect.area();

            for (int ix = 1; ix < boundingRects.size(); ix++) {
                boundingRect = boundingRects.get(ix);
                boundingRect.points(vertices);
                rect = Imgproc.boundingRect(new MatOfPoint(vertices));

                if (rect.area() > maxArea) {
                    maxArea = rect.area();
                    ixMaxArea = ix;
                }
            }

            bestRect = boundingRects.get(ixMaxArea);
        }

        return bestRect;
    }

    public static RotatedRect findROI(Mat sourceMat) {
        final Mat mat = new Mat();
        sourceMat.copyTo(mat);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat, mat, 146, 250, Imgproc.THRESH_BINARY);

        // find contours
        List<MatOfPoint> contours = new ArrayList<>();
        List<RotatedRect> boundingRects = new ArrayList<>();
        Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // find appropriate bounding rectangles
        for (MatOfPoint contour : contours) {
            MatOfPoint2f areaPoints = new MatOfPoint2f(contour.toArray());
            RotatedRect boundingRect = Imgproc.minAreaRect(areaPoints);
            boundingRects.add(boundingRect);
        }

        return getBestRectByArea(boundingRects);

    }

    public static List<MatOfPoint> findContours(Mat sourceMat) {
        final Mat mat = new Mat();
        sourceMat.copyTo(mat);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat, mat, 146, 250, Imgproc.THRESH_BINARY);

        // find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

}
