package com.anton111111.mlkittest.drivingidrecognition;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.anton111111.mlkittest.VisionProcessorMulti;
import com.anton111111.opencv.detector.ContoursDetector;
import com.anton111111.opencv.detector.ROIDetector;
import com.anton111111.opencv.graphic.ContoursGraphic;
import com.anton111111.opencv.graphic.PointsGraphic;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.facedetection.FaceGraphic;
import com.google.firebase.samples.apps.mlkit.textrecognition.TextGraphic;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Potekhin (Anton.Potekhin@gmail.com) on 16.08.18.
 */
public class DrivingIdRecognitionProcessor extends VisionProcessorMulti {

    private static final String TAG = "DIRP";
    private final FirebaseVisionTextRecognizer textDetector;
    private final FirebaseVisionFaceDetector faceDetector;
    private final ROIDetector roiDetector;
    private final ContoursDetector contoursDetector;

    public DrivingIdRecognitionProcessor() {
        textDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setModeType(FirebaseVisionFaceDetectorOptions.FAST_MODE)
                        .setTrackingEnabled(true)
                        .build();

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        roiDetector = new ROIDetector();
        contoursDetector = new ContoursDetector();
    }

    @Override
    protected List<Task> detectInImage(ProcessorImage processorImage) {
        return new ArrayList<Task>() {{
//            add(textDetector.processImage(processorImage.getImage()));
//            add(faceDetector.detectInImage(processorImage.getImage()));
            add(roiDetector.findROI(processorImage.getData(), processorImage.getFrameMetadata()));
            add(contoursDetector.findContours(processorImage.getData(), processorImage.getFrameMetadata()));

        }};
    }

    @Override
    protected void onSuccess(@NonNull List<Object> resultsList, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        FirebaseVisionFace face = null;
        List<FirebaseVisionText.Element> elements = null;
        List<Point> roi = null;
        List<MatOfPoint> contours = null;

        for (Object result : resultsList) {
            if (result instanceof FirebaseVisionText) {
                elements = onSuccessText((FirebaseVisionText) result, frameMetadata, graphicOverlay);
            }
            if (result instanceof List &&
                    ((List) result).size() > 0 &&
                    ((List) result).get(0) instanceof FirebaseVisionFace) {
                face = onSuccessFace((List<FirebaseVisionFace>) result, frameMetadata, graphicOverlay);
            }
            if (result instanceof List &&
                    ((List) result).size() > 0 &&
                    ((List) result).get(0) instanceof Point) {
                roi = (List<Point>) result;
            }
            if (result instanceof List &&
                    ((List) result).size() > 0 &&
                    ((List) result).get(0) instanceof MatOfPoint) {
                contours = (List<MatOfPoint>) result;
            }

        }

        if (elements != null && elements.size() > 0) {
            for (FirebaseVisionText.Element element : elements) {
                GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, element);
                graphicOverlay.add(textGraphic);
            }
        }

        if (face != null) {
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
            graphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face, frameMetadata.getCameraFacing());
        }

        if (roi != null) {
            graphicOverlay.add(
                    new PointsGraphic(graphicOverlay, roi)
            );
        }
        if (contours != null) {
            graphicOverlay.add(
                    new ContoursGraphic(graphicOverlay, contours)
            );
        }

    }


    private List<FirebaseVisionText.Element> onSuccessText(@NonNull FirebaseVisionText
                                                                   results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
        boolean isDriver = false;
        boolean isLicense = false;
        boolean isHasDate = false;
        boolean isHasDOB = false;
        List<FirebaseVisionText.Element> result = new ArrayList<>();
        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    FirebaseVisionText.Element element = elements.get(k);
                    if (element.getText().toLowerCase().equals("driver")) {
                        isDriver = true;
                        result.add(element);
                    }
                    if (element.getText().toLowerCase().equals("license")) {
                        isLicense = true;
                        result.add(element);
                    }
                    if (element.getText().toLowerCase().indexOf("driver license") >= 0) {
                        isDriver = true;
                        isLicense = true;
                        result.add(element);
                    }
                    if (element.getText().toLowerCase().indexOf("driving license") >= 0) {
                        isDriver = true;
                        isLicense = true;
                        result.add(element);
                    }
//                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, element);
//                    graphicOverlay.add(textGraphic);
                }
            }
        }
        return result;
    }


    private FirebaseVisionFace onSuccessFace(
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        FirebaseVisionFace faceResult = null;
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            if (faceResult == null ||
                    compareRects(faceResult.getBoundingBox(), face.getBoundingBox()) < 0) {
                faceResult = face;
            }
        }
        return faceResult;
    }

    /**
     * Compare two Rects
     *
     * @param rect1
     * @param rect2
     * @return the value {@code 0} if {@code rect1 == rect2};
     * a value less than {@code 0} if {@code rect1 < rect2}; and
     * a value greater than {@code 0} if {@code rect1 > rect2}
     */
    private int compareRects(Rect rect1, Rect rect2) {
        int sq1 = (rect1.right - rect1.left) * (rect1.bottom - rect1.top);
        int sq2 = (rect2.right - rect2.left) * (rect2.bottom - rect2.top);
        return Integer.compare(sq1, sq2);
    }

    @Override
    protected void onFailure(GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
    }
}
