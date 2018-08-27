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
import org.opencv.core.MatOfPoint;


import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

public class ContoursDetector implements Closeable {

    private static final String TAG = "ContoursDetector";
    private FindContoursTask findContoursTask;
    private ContoursTask task;


    public Task<List<MatOfPoint>> findContours(ByteBuffer data,
                                     final FrameMetadata frameMetadata) {
        task = new ContoursTask();
        findContoursTask = new FindContoursTask(data, frameMetadata, task);
        findContoursTask.execute();
        return task;
    }

    @Override
    public void close() throws IOException {
        if (findContoursTask != null) {
            findContoursTask.cancel(true);
        }
    }

    private static class FindContoursTask extends AsyncTask<Void, Void, List<MatOfPoint>> {

        private final ByteBuffer data;
        private final FrameMetadata frameMetadata;
        private final ContoursTask task;
        private boolean isCanceled = false;
        private boolean isCompleted = false;
        private List<MatOfPoint> contours = null;

        public boolean isCanceled() {
            return isCanceled;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public List<MatOfPoint> getResult() {
            return contours;
        }


        public FindContoursTask(ByteBuffer data,
                                final FrameMetadata frameMetadata, ContoursTask task) {
            this.data = data;
            this.frameMetadata = frameMetadata;
            this.task = task;
        }

        @Override
        protected List<MatOfPoint> doInBackground(Void... voids) {
            Mat mat = OpenCVHelper.matFromByteBuffer(data,
                    frameMetadata.getWidth(),
                    frameMetadata.getHeight(),
                    android.graphics.ImageFormat.NV21,
                    frameMetadata.getRotation());
                    //0);
            return OpenCVHelper.findContours(mat);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isCanceled = true;
        }

        @Override
        protected void onPostExecute(List<MatOfPoint> contours) {
            super.onPostExecute(contours);
            this.contours = contours;
            isCompleted = true;
            if (task != null) {
                task.getOnSuccessListener().onSuccess(contours);
                task.getOnCompleteListener().onComplete(task);
            }
        }

    }

    private class ContoursTask extends Task<List<MatOfPoint>> {

        private OnCompleteListener<List<MatOfPoint>> onCompleteListener;
        private OnSuccessListener<? super List<MatOfPoint>> onSuccessListener;

        public OnCompleteListener<List<MatOfPoint>> getOnCompleteListener() {
            return onCompleteListener;
        }

        public OnSuccessListener<? super List<MatOfPoint>> getOnSuccessListener() {
            return onSuccessListener;
        }

        @Override
        public boolean isComplete() {
            if (findContoursTask != null) {
                return findContoursTask.isCompleted();
            }
            return false;
        }

        @Override
        public boolean isSuccessful() {
            if (findContoursTask != null) {
                return findContoursTask.isCompleted();
            }
            return false;
        }

        @Override
        public boolean isCanceled() {
            if (findContoursTask != null) {
                return findContoursTask.isCanceled();
            }
            return false;
        }

        @Override
        public List<MatOfPoint> getResult() {
            if (findContoursTask != null && findContoursTask.isCompleted()) {
                return findContoursTask.getResult();
            }
            return null;
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnCompleteListener(@NonNull OnCompleteListener<List<MatOfPoint>> onCompleteListener) {
            this.onCompleteListener = onCompleteListener;
            return this;
        }

        @Override
        public <X extends Throwable> List<MatOfPoint> getResult(@NonNull Class<X> aClass) throws X {
            throw new UnsupportedOperationException("Unsupported operation getResult");
        }

        @Nullable
        @Override
        public Exception getException() {
            throw new UnsupportedOperationException("Unsupported operation getException");
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnSuccessListener(@NonNull OnSuccessListener<? super List<MatOfPoint>> onSuccessListener) {
            this.onSuccessListener = onSuccessListener;
            return this;
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super List<MatOfPoint>> onSuccessListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnSuccessListener with executor");
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super List<MatOfPoint>> onSuccessListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnSuccessListener with Activity");
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }

        @NonNull
        @Override
        public Task<List<MatOfPoint>> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
            throw new UnsupportedOperationException("Unsupported operation addOnFailureListener");
        }
    }


}
