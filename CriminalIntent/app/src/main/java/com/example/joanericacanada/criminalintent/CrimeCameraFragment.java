package com.example.joanericacanada.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by joanericacanada on 10/8/15.
 */
public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME =
            "com.example.joanericacanada.criminalintent.photo_filename";

    public static final String EXTRA_PHOTO_ORIENTATION =
            "com.example.joanericacanada.criminalintent.photo_orientation";

    private Camera camera;
    private SurfaceView surfaceView;
    private View progressContainer;
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            progressContainer.setVisibility(View.VISIBLE);
        }
    };
    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            String filename = UUID.randomUUID().toString() + ".jpg";
            FileOutputStream os = null;
            boolean success = true;

            try {
                os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                os.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + filename, e);
                success = false;
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file " + filename, e);
                    success = false;
                }
            }
            if (success) {
                Log.i(TAG, "JPEG saved at " + filename);

                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, filename);

                CrimeCameraActivity activity = (CrimeCameraActivity) getActivity();
                int orientation = activity.getOrientation();

                i.putExtra(EXTRA_PHOTO_ORIENTATION, orientation);
                getActivity().setResult(Activity.RESULT_OK, i);
            } else
                getActivity().setResult(Activity.RESULT_CANCELED);

            getActivity().finish();
        }
    };


    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);

        progressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        progressContainer.setVisibility(View.INVISIBLE);

        Button takePictureButton = (Button) v
                .findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (camera != null) 
                    camera.takePicture(shutterCallback, null, mJpegCallback);
            }
        });

        surfaceView = (SurfaceView) v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (camera != null) {
                        camera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null) {
                    camera.stopPreview();
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (camera == null) return;

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), w, h);
                parameters.setPreviewSize(s.width, s.height);
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(), w, h);
                parameters.setPictureSize(s.width, s.height);
                camera.setParameters(parameters);
                try {
                    camera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    camera.release();
                    camera = null;
                }
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            camera = Camera.open(0);
        } else {
            camera = Camera.open();
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }


}
