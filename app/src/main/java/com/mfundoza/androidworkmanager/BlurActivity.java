package com.mfundoza.androidworkmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.WorkInfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.mfundoza.androidworkmanager.databinding.ActivityBlurBinding;

public class BlurActivity extends AppCompatActivity {
    private BlurViewModel mViewModel;
    private ActivityBlurBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlurBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the ViewModel
        mViewModel = new BlurViewModel(getApplication());

        // Setup blur image file button
        binding.goButton.setOnClickListener(view -> mViewModel.applyBlur(getBlurLevel()));

        mViewModel.getOutputWorkInfo().observe(this, listOfWorkInfo -> {

            // If there are no matching work info, do nothing
            if (listOfWorkInfo == null || listOfWorkInfo.isEmpty()) {
                return;
            }

            // We only care about the first output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            WorkInfo workInfo = listOfWorkInfo.get(0);

            boolean finished = workInfo.getState().isFinished();
            if (!finished) {
                showWorkInProgress();
            } else {
                showWorkFinished();
                Data outputData = workInfo.getOutputData();

                String outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI);

                // If there is an output file show "See File" button
                if (!TextUtils.isEmpty(outputImageUri)) {
                    mViewModel.setOutputUri(outputImageUri);
                    binding.seeFileButton.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.seeFileButton.setOnClickListener(view -> {
            Uri currentUri = mViewModel.getOutputUri();
            if (currentUri != null) {
                Intent actionView = new Intent(Intent.ACTION_VIEW, currentUri);
                if (actionView.resolveActivity(getPackageManager()) != null) {
                    startActivity(actionView);
                }
            }
        });

        binding.cancelButton.setOnClickListener(view -> mViewModel.cancelWork());
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private void showWorkInProgress() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cancelButton.setVisibility(View.VISIBLE);
        binding.goButton.setVisibility(View.GONE);
        binding.seeFileButton.setVisibility(View.GONE);
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private void showWorkFinished() {
        binding.progressBar.setVisibility(View.GONE);
        binding.cancelButton.setVisibility(View.GONE);
        binding.goButton.setVisibility(View.VISIBLE);
    }

    /**
     * Get the blur level from the radio button as an integer
     * @return Integer representing the amount of times to blur the image
     */
    private int getBlurLevel() {
        switch(binding.radioBlurGroup.getCheckedRadioButtonId()) {
            case R.id.radio_blur_lv_1:
                return 1;
            case R.id.radio_blur_lv_2:
                return 2;
            case R.id.radio_blur_lv_3:
                return 3;
        }

        return 1;
    }
}