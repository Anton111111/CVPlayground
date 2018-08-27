package com.anton111111.opencv.detector;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anton111111.opencv.OpenCVHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class ROIDetector implements Closeable {

    private static final String TAG = "ROIDetector";
    private FindROITask findROITask;
    private ROITask task;


    public Task<RotatedRect> findROI(ByteBuffer data,
                                     final FrameMetadata frameMetadata) {
        task = new ROITask();
        findROITask = new FindROITask(data, frameMetadata, task);
        findROITask.execute();
        return task;
    }

    @Override
    public void close() throws IOException {
        if (findROITask != null) {
            findROITask.cancel(true);
        }
    }

    private static class FindROITask extends AsyncTask<Void, Void, RotatedRect> {

        private final ByteBuffer data;
        private final FrameMetadata frameMetadata;
        private final ROITask task;
        private boolean isCanceled = false;
        private boolean isCompleted = false;
        private RotatedRect rotatedRect = null;

        public boolean isCanceled() {
            return isCanceled;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public RotatedRect getResult() {
            return rotatedRect;
        }


        public FindROITask(ByteBuffer data,
                           final FrameMetadata frameMetadata, ROITask task) {
            this.data = data;
            this.frameMetadata = frameMetadata;
            this.task = task;
        }

        @Override
        protected RotatedRect doInBackground(Void... voids) {
            Mat mat = OpenCVHelper.matFromByteBuffer(data,
                    frameMetadata.getWidth(),
                    frameMetadata.getHeight(),
                    android.graphics.ImageFormat.NV21,
                    frameMetadata.getRotation());

            RotatedRect roi = OpenCVHelper.findROI(mat);

            return roi;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isCanceled = true;
        }

        @Override
        protected void onPostExecute(RotatedRect rotatedRect) {
            super.onPostExecute(rotatedRect);
            this.rotatedRect = rotatedRect;
            isCompleted = true;
            if (task != null) {
                task.getOnSuccessListener().onSuccess(rotatedRect);
                task.getOnCompleteListener().onComplete(task);
            }
        }

    }

    private class ROITask extends Task<RotatedRect> {

        private OnCompleteListener<RotatedRect> onCompleteListener;
        private OnSuccessListener<? super RotatedRect> onSuccessListener;

        public OnCompleteListener<RotatedRect> getOnCompleteListener() {
            return onCompleteListener;
        }

        public OnSuccessListener<? super RotatedRect> getOnSuccessListener() {
            return onSuccessListener;
        }

        @Override
        public boolean isComplete() {
            if (findROITask != null) {
                return findROITask.isCompleted();
            }
            return false;
        }

        @Override
        public boolean isSuccessful() {
            if (findROITask != null) {
                return findROITask.isCompleted();
            }
            return false;
        }

        @Override
        public boolean isCanceled() {
            if (findROITask != null) {
                return findROITask.isCanceled();
            }
            return false;
        }

        @Override
        public RotatedRect getResult() {
            if (findROITask != null && findROITask.isCompleted()) {
                return findROITask.getResult();
            }
            return null;
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnCompleteListener(@NonNull OnCompleteListener<RotatedRect> onCompleteListener) {
            this.onCompleteListener = onCompleteListener;
            return this;
        }

        @Override
        public <X extends Throwable> RotatedRect getResult(@NonNull Class<X> aClass) throws X {
            throw new UnsupportedOperationException("Unsupported operation getResult");
        }

        @Nullable
        @Override
        public Exception getException() {
            throw new UnsupportedOperationException("Unsupported operation getException");
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnSuccessListener(@NonNull OnSuccessListener<? super RotatedRect> onSuccessListener) {
            this.onSuccessListener = onSuccessListener;
            return this;
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super RotatedRect> onSuccessListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnSuccessListener with executor");
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super RotatedRect> onSuccessListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnSuccessListener with Activity");
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }

        @NonNull
        @Override
        public Task<RotatedRect> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }
    }

    ;
}
