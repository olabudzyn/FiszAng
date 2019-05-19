package com.budzynska.fiszang;

import android.Manifest;
import android.content.pm.PackageManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;

public class LiveTranslatorAcitivity extends AppCompatActivity {

    private TextView txvTranslated, txvToTranslate;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private static final int requestPermissionID = 101;
    private static final String API_KEY = "AIzaSyDsKajjSP6Dnk6wYc9S_EvzIEqEw98WQfc";

    private Translate translate;
    private TranslateOptions options;
    private Translation translation;
    private Handler handler = new Handler();
    private String  translateText;
    private String currentText ="";

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_translator_activity);

        txvTranslated = findViewById(R.id.txvTranslated2);
        txvToTranslate = findViewById(R.id.txvToTranslate2);
        surfaceView = findViewById(R.id.surfaceView);

        startCameraSource();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != requestPermissionID){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        return;}

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            try {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void translate() {
        translateText = txvToTranslate.getText().toString(); // words to translate

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                options = TranslateOptions.newBuilder().setApiKey(API_KEY).build();
                translate = options.getService();
                translation = translate.translate(translateText, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("pl"));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (txvTranslated != null) {
                            txvTranslated.setText(translation.getTranslatedText());
                        }
                    }
                });
                return null;
            }
        }.execute();

    }

    private void startCameraSource() {

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()){

        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setAutoFocusEnabled(true)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

                            ActivityCompat.requestPermissions(LiveTranslatorAcitivity.this, new String[]{Manifest.permission.CAMERA}, requestPermissionID);
                            return;
                        }
                        cameraSource.start(surfaceView.getHolder());

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() !=0){
                        txvToTranslate.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i = 0 ; i < items.size() ; i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append(" ");
                                }
                                if(!stringBuilder.toString().equals(currentText))
                                {
                                    currentText = stringBuilder.toString();
                                }

                                    txvToTranslate.setText(currentText);
                                    translate();

                            }
                        });
                    }
                }
            });
        }
    }
}
