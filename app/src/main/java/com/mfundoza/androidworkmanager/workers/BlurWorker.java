package com.mfundoza.androidworkmanager.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mfundoza.androidworkmanager.R;

import static com.mfundoza.androidworkmanager.Constants.KEY_IMAGE_URI;

public class BlurWorker extends Worker {
    private static final String TAG = BlurWorker.class.getSimpleName();

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param context   the application {@link Context}
     * @param parameters the set of {@link WorkerParameters}
     */
    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();

        WorkerUtils.makeStatusNotification("Blurring image", appContext);
        WorkerUtils.sleep();
        String resourceUri = getInputData().getString(KEY_IMAGE_URI);


        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver resolver = appContext.getContentResolver();

            // Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));

            // Blur the bitmap
            Bitmap output = WorkerUtils.blurBitmap(picture,appContext);

            // Write bitmap to a temp file
            Uri outputUri = WorkerUtils.writeBitmapToFile(appContext, output);

            Data outputData = new Data.Builder()
                    .putString(KEY_IMAGE_URI, outputUri.toString())
                    .build();

            // If there were no errors, return SUCCESS
            return Result.success(outputData);
        } catch (Throwable throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
