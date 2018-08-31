package com.anton111111.opencv.detector;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anton111111.opencv.OpenCVHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

public class ROIDetector implements Closeable {

    private static final String TAG = "ROIDetector";
    private FindROITask findROITask;
    private ROITask task;


    public Task<List<Point>> findROI(ByteBuffer data,
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

    private static class FindROITask extends AsyncTask<Void, Void, List<Point>> {

        private final ByteBuffer data;
        private final FrameMetadata frameMetadata;
        private final ROITask task;
        private boolean isCanceled = false;
        private boolean isCompleted = false;
        private List<Point> roi = null;

        public boolean isCanceled() {
            return isCanceled;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public List<Point> getResult() {
            return roi;
        }


        public FindROITask(ByteBuffer data,
                           final FrameMetadata frameMetadata, ROITask task) {
            this.data = data;
            this.frameMetadata = frameMetadata;
            this.task = task;
        }

        @Override
        protected List<Point> doInBackground(Void... voids) {
            Mat mat = OpenCVHelper.matFromByteBuffer(data,
                    frameMetadata.getWidth(),
                    frameMetadata.getHeight(),
                    android.graphics.ImageFormat.NV21,
                    frameMetadata.getRotation());

            List<Point> roi = OpenCVHelper.findROI(mat);

            return roi;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isCanceled = true;
        }

        @Override
        protected void onPostExecute(List<Point> roi) {
            super.onPostExecute(roi);
            this.roi = roi;
            isCompleted = true;
            if (task != null) {
                task.getOnSuccessListener().onSuccess(this.roi);
                task.getOnCompleteListener().onComplete(task);
            }
        }

    }

    private class ROITask extends Task<List<Point>> {

        private OnCompleteListener<List<Point>> onCompleteListener;
        private OnSuccessListener<? super List<Point>> onSuccessListener;

        public OnCompleteListener<List<Point>> getOnCompleteListener() {
            return onCompleteListener;
        }

        public OnSuccessListener<? super List<Point>> getOnSuccessListener() {
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
        public List<Point> getResult() {
            if (findROITask != null && findROITask.isCompleted()) {
                return findROITask.getResult();
            }
            return null;
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnCompleteListener(@NonNull OnCompleteListener<List<Point>> onCompleteListener) {
            this.onCompleteListener = onCompleteListener;
            return this;
        }

        @Override
        public <X extends Throwable> List<Point> getResult(@NonNull Class<X> aClass) throws X {
            throw new UnsupportedOperationException("Unsupported operation getResult");
        }

        @Nullable
        @Override
        public Exception getException() {
            throw new UnsupportedOperationException("Unsupported operation getException");
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnSuccessListener(@NonNull OnSuccessListener<? super List<Point>> onSuccessListener) {
            this.onSuccessListener = onSuccessListener;
            return this;
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super List<Point>> onSuccessListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnSuccessListener with executor");
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super List<Point>> onSuccessListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnSuccessListener with Activity");
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }

        @NonNull
        @Override
        public Task<List<Point>> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }
    }

    ;
}
