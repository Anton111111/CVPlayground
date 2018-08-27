// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.anton111111.mlkittest;

import android.graphics.Bitmap;
import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Log;

import com.anton111111.opencv.OpenCVHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.VisionImageProcessor;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.concurrent.atomic.AtomicBoolean;


public abstract class VisionProcessorMulti implements VisionImageProcessor {

    private static final String TAG = "VisionProcessorMulti";

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);


    public VisionProcessorMulti() {
    }

    @Override
    public void process(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay
            graphicOverlay) {
        if (shouldThrottle.get()) {
            return;
        }

        detectInVisionImage(
                data, frameMetadata, graphicOverlay);
    }

    // Bitmap version
    @Override
    public void process(Bitmap bitmap, final GraphicOverlay
            graphicOverlay) {
        throw new UnsupportedOperationException("Unsupported process from bitmap");
//        if (shouldThrottle.get()) {
//            return;
//        }
//        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap), null, graphicOverlay);
    }

    /**
     * Detects feature from given media.Image
     *
     * @return created FirebaseVisionImage
     */
    @Override
    public void process(Image image, int rotation, final GraphicOverlay graphicOverlay) {
        throw new UnsupportedOperationException("Unsupported process from image");
//        if (shouldThrottle.get()) {
//            return;
//        }
//        // This is for overlay display's usage
//        FrameMetadata frameMetadata =
//                new FrameMetadata.Builder().setWidth(image.getWidth()).setHeight(image.getHeight
//                        ()).build();
//        FirebaseVisionImage fbVisionImage =
//                FirebaseVisionImage.fromMediaImage(image, rotation);
//        detectInVisionImage(fbVisionImage, frameMetadata, graphicOverlay);
    }


    private void detectInVisionImage(
            ByteBuffer data,
            final FrameMetadata frameMetadata,
            final GraphicOverlay graphicOverlay) {

        ProcessorImage processorImage = new ProcessorImage(frameMetadata, data);

        List<Task> tasks = detectInImage(processorImage);
        List<Object> resultObjects = new ArrayList<>();

        for (Task task : tasks) {

            task.addOnSuccessListener(
                    new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object results) {
                            resultObjects.add(results);
                        }
                    })
                    .addOnCompleteListener(
                            new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    for (Task taskTmp : tasks) {
                                        if (!taskTmp.isComplete()) {
                                            return;
                                        }
                                    }
                                    if (resultObjects != null && resultObjects.size() > 0) {
                                        VisionProcessorMulti.this.onSuccess(resultObjects, frameMetadata,
                                                graphicOverlay);
                                    } else {
                                        VisionProcessorMulti.this.onFailure(graphicOverlay);
                                    }
                                    shouldThrottle.set(false);
                                }
                            });
        }

        shouldThrottle.set(true);
    }

    @Override
    public void stop() {
    }

    protected abstract List<Task> detectInImage(ProcessorImage processorImage);

    protected abstract void onSuccess(
            @NonNull List<Object> results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull GraphicOverlay graphicOverlay);

    public class ProcessorImage {
        private FrameMetadata frameMetadata;
        private ByteBuffer data;
        private FirebaseVisionImage image;

        public ProcessorImage(FrameMetadata frameMetadata, ByteBuffer data) {
            this.frameMetadata = frameMetadata;
            this.data = data;
        }

        public FrameMetadata getFrameMetadata() {
            return frameMetadata;
        }

        public ByteBuffer getData() {
            return data;
        }

        public synchronized FirebaseVisionImage getImage() {
            if (image == null) {
                FirebaseVisionImageMetadata metadata =
                        new FirebaseVisionImageMetadata.Builder()
                                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                                .setWidth(frameMetadata.getWidth())
                                .setHeight(frameMetadata.getHeight())
                                .setRotation(frameMetadata.getRotation())
                                .build();

                image = FirebaseVisionImage.fromByteBuffer(data, metadata);
            }
            return image;
        }
    }
}
