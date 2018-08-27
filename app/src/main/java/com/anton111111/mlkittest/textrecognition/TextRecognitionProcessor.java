package com.anton111111.mlkittest.textrecognition;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.textrecognition.TextGraphic;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Anton Potekhin (Anton.Potekhin@gmail.com) on 16.08.18.
 */
public class TextRecognitionProcessor extends com.google.firebase.samples.apps.mlkit.textrecognition.TextRecognitionProcessor {


    private static final String TAG = "TRProcessor";

    @Override
    protected void onSuccess(
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    FirebaseVisionText.Element element = elements.get(k);
                    if (!element.getText().equals("29.10.1981") && !element.getText().equals("3.")) {
                        continue;
                    }
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, element);
                    graphicOverlay.add(textGraphic);
                }
            }
        }
    }

}
