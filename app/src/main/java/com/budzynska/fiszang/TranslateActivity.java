package com.budzynska.fiszang;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Arrays;

public class TranslateActivity extends AppCompatActivity {

    private Button btnTranslate;
    private TextView txvTranslated, txvToTranslate;
    private static final String API_KEY = "AIzaSyDsKajjSP6Dnk6wYc9S_EvzIEqEw98WQfc";

    Translate translate;
    TranslateOptions options;
    Translation translation;
    Handler handler = new Handler();
    String  translateText;
    private Vision vision;

    private String selectedText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_translator_second_activity);
        btnTranslate = findViewById(R.id.buttonTranslate);
        txvToTranslate = findViewById(R.id.txvToTranslate);
        txvTranslated = findViewById(R.id.txvTranslated);

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer(API_KEY));

        vision = visionBuilder.build();
        textDetection();


        //txvToTranslate.setText(newText);

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //translate();
                // wybieranie kawałka tekstu
                int startSelection=txvToTranslate.getSelectionStart();
                int endSelection=txvToTranslate.getSelectionEnd();
                selectedText = txvToTranslate.getText().subSequence(startSelection, endSelection).toString();
                translate();

            }
        });
        //words = getIntent().getStringExtra("image");
        //txvToTranslate.setText(words);
    }

    private void textDetection() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    InputStream inputStream = getResources().openRawResource(R.raw.superthumb);
                    byte[] photoData = IOUtils.toByteArray(inputStream);

                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("TEXT_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                    final TextAnnotation text = batchResponse.getResponses()
                            .get(0).getFullTextAnnotation();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txvToTranslate.setText(text.getText());
                        }
                    });

                } catch (Exception e) {
                    Log.d("ERROR", e.getMessage());
                }
            }
        });

    }


    public void translate() {
        //translateText = txvToTranslate.getText().toString(); // words to translate

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                options = TranslateOptions.newBuilder().setApiKey(API_KEY).build();
                translate = options.getService();
                translation = translate.translate(selectedText, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("pl"));
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
}

