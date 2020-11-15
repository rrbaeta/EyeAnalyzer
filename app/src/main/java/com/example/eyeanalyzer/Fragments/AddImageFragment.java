package com.example.eyeanalyzer.Fragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eyeanalyzer.Activity.MainActivity;
import com.example.eyeanalyzer.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;


public class AddImageFragment extends Fragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE = 101;

    private Button loadImageBtn;
    private ImageView targetImage;
    private Button takePictureBtn;
    private Uri uri;

    public AddImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_add_image, container, false);

        loadImageBtn = rootView.findViewById(R.id.loadImageBtn);
        targetImage = rootView.findViewById(R.id.targetImage);
        takePictureBtn = rootView.findViewById(R.id.takePictureBtn);

        loadImageBtn.setOnClickListener(this);
        takePictureBtn.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.loadImageBtn:

                CropImage.startPickImageActivity(getContext(), this);

                break;

            case R.id.takePictureBtn:

                dispatchTakePictureIntent();

                break;
        }

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE) {
//            try {
//                final Uri imageUri = data.getData();
//                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                targetImage.setImageBitmap(selectedImage);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
//            }
//        }
//
//        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            targetImage.setImageBitmap(imageBitmap);
//        }
//
//        else {
//            Toast.makeText(getContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
//        }
//    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK)
        {
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri))
            {
                uri = imageUri;
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            else
            {
                startCrop(imageUri);
            }
        }

        // TODO: when taking a picture the app doesn't call the cropper
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            targetImage.setImageBitmap(imageBitmap);

//            uri = data.getData();
//            startCrop(uri);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                targetImage.setImageURI(result.getUri());
                Toast.makeText(getContext(), "Image Update Successfull", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true).setAspectRatio(4,4).start(getContext(), this);
    }
}