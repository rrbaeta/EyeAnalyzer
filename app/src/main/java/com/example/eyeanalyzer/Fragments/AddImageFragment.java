package com.example.eyeanalyzer.Fragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyeanalyzer.Model.RequestTask;
import com.example.eyeanalyzer.R;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.Activity.RESULT_OK;


public class AddImageFragment extends Fragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE = 2;

    private Button loadImageBtn;
    private ImageView targetImage;
    private TextView predictionTextView;

    public AddImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_add_image, container, false);

        loadImageBtn = rootView.findViewById(R.id.loadImageBtn);
        targetImage = rootView.findViewById(R.id.targetImage);
        predictionTextView = rootView.findViewById(R.id.predictionTextView);

        loadImageBtn.setOnClickListener(this);

        isStoragePermissionGranted();

        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.loadImageBtn:

                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE);

                //CropImage.startPickImageActivity(getContext(), this);

                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                String imagePath = getPath(getContext(), imageUri); // needs to be tested
                //image_name_tv.setText(imagePath);

                RequestTask requestTask = new RequestTask(this);
                requestTask.execute(imagePath);

                targetImage.setImageBitmap(selectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }

        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            targetImage.setImageBitmap(imageBitmap);
        }

        else {
            Toast.makeText(getContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public void displayPrediction(String response){
        predictionTextView.setText(response);
    }

//    private void dispatchTakePictureIntent() { //TODO: probably to be removed
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        try {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        } catch (ActivityNotFoundException e) {
//            // display error state to the user
//        }
//    }





//-------------------------------------------------------------------------------------------------
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK)
//        {
//            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
//            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri))
//            {
//                uri = imageUri;
//                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
//            }
//            else
//            {
//                startCrop(imageUri);
//            }
//        }
//
//        // TODO: when taking a picture the app doesn't call the cropper
//        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            targetImage.setImageBitmap(imageBitmap);
//
////            uri = data.getData();
////            startCrop(uri);
//
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
//        {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK)
//            {
//                targetImage.setImageURI(result.getUri());
//                Toast.makeText(getContext(), "Image Update Successfull", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }
//
//    private void startCrop(Uri imageUri) {
//        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true).setAspectRatio(4,4).start(getContext(), this);
//    }
}