package com.budzynska.fiszang;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import java.io.File;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ImageTranslatorActivity extends AppCompatActivity {

    private String detectedText;
    private Button takePictureButton, buttonTranslate;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private Bitmap bitmap;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_translator_activity);

        takePictureButton = findViewById(R.id.buttonPicture);
        buttonTranslate = findViewById(R.id.buttonTranslate2);
        imageView = findViewById(R.id.texture);



        buttonTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), TranslateActivity.class);
                i.putExtra("text", detectedText);
                startActivity(i);
            }
        });
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Photo file can't be created, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.budzynska.fiszang.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                int rotate = 0;
                File file = new File(currentPhotoPath);
                try {
                    ExifInterface exif = new ExifInterface(
                            file.getAbsolutePath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    bitmap = MediaStore.Images.Media
                            .getBitmap(getApplicationContext().getContentResolver(), Uri.fromFile(file));

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                    if (bitmap != null) {
                        Bitmap rotate_bitmap = rotateImage(bitmap, rotate);
                        imageView.setImageBitmap(rotate_bitmap);
                        textRecognizer(rotate_bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void textRecognizer(Bitmap bitmap2) {

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {

        } else {

            Frame frame = new Frame.Builder().setBitmap(bitmap2).build();
            final SparseArray<TextBlock> items = textRecognizer.detect(frame);
            if (items.size() != 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock item = items.valueAt(i);
                    stringBuilder.append(item.getValue());
                    stringBuilder.append(" ");
                }
                detectedText = stringBuilder.toString();
            }
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }

}



