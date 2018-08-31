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


    public static List<MatOfPoint> findContours(Mat sourceMat) {
        final Mat mat = new Mat();
        sourceMat.copyTo(mat);

        //convert the image to black and white
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

        //convert the image to black and white does (8 bit)
        Imgproc.Canny(mat, mat, 50, 50);

        //apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 5);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    public static List<Point> findROI(Mat sourceMat) {

        //find the contours
        List<MatOfPoint> contours = findContours(sourceMat);

        if (contours == null || contours.size() == 0) {
            return null;
        }

        double maxArea = -1;
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.05, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    approxCurve = approxCurve_temp;
                }
            }
        }


        double[] temp_double;
        temp_double = approxCurve.get(0, 0);
        Point p1 = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(1, 0);
        Point p2 = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(2, 0);
        Point p3 = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(3, 0);
        Point p4 = new Point(temp_double[0], temp_double[1]);
        List<Point> source = new ArrayList<Point>();
        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);
        return source;
    }

}
