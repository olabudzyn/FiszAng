package com.budzynska.fiszang;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budzynska.fiszang.basedata.Dictionary;
import com.budzynska.fiszang.basedata.DictionaryElement;
import com.budzynska.fiszang.listview.DictionaryList;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TranslateActivity extends AppCompatActivity {

    private Button btnTranslate, btnAddToDictionary;
    private TextView txvTranslated, txvToTranslate;
    private static final String API_KEY = "AIzaSyDsKajjSP6Dnk6wYc9S_EvzIEqEw98WQfc";
    private List<Dictionary> dictionaries;
    private String dictionaryId;

    private Translate translate;
    private TranslateOptions options;
    private Translation translation;
    private Handler handler = new Handler();
    private String translatedText, selectedText;
    private Vision vision;

    private DatabaseReference databaseDictionaries;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_translator_second_activity);
        btnTranslate = findViewById(R.id.buttonTranslate);
        txvToTranslate = findViewById(R.id.txvToTranslate);
        txvTranslated = findViewById(R.id.txvTranslated);
        btnAddToDictionary = findViewById(R.id.buttonAddToDictionary);
        dictionaries = new ArrayList<>();

        databaseDictionaries = FirebaseDatabase.getInstance().getReference(MainMenuActivity.DICTIONARY_PATH).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        buildList();

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
                int startSelection = txvToTranslate.getSelectionStart();
                int endSelection = txvToTranslate.getSelectionEnd();
                selectedText = txvToTranslate.getText().subSequence(startSelection, endSelection).toString();
                translate();

            }
        });

        btnAddToDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDictionary();
            }
        });
        //words = getIntent().getStringExtra("image");
        //txvToTranslate.setText(words);
    }

    private void addToDictionary() {
        showDictionaryDialog();
        DatabaseReference databaseWords = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId);

        String wordsId = databaseWords.push().getKey();
        DictionaryElement words = new DictionaryElement(wordsId, selectedText, translatedText);
        databaseWords.child(wordsId).setValue(words).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Word added successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectDictionary(String id) {
        dictionaryId = id;
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
                            translatedText = translation.getTranslatedText();
                        }
                    }
                });
                return null;
            }
        }.execute();

    }

    private void showDictionaryDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.list_dictionaries_dialog, null);

        ListView lv = convertView.findViewById(R.id.listViewAddWord);

        lv.setAdapter(new DictionaryList(this, dictionaries));

        alertDialog.setView(convertView);
        alertDialog.setTitle("Choose dictionary");

        AlertDialog dialog = alertDialog.create();
        dialog.show();

       /* AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.list_dictionaries_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Choose dictionary");

        final TextView textViewAvaliable = dialogView.findViewById(R.id.textViewAvaliableDictionaries);
        final ListView listViewDictionaries = dialogView.findViewById(R.id.listViewAddWord);
        buildList(listViewDictionaries);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show(); */

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dictionary dictionary = dictionaries.get(position);
                selectDictionary(dictionary.getDictionaryId());
                dialog.dismiss();
            }
        });

    }

    private void buildList() {
        databaseDictionaries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshotDictionary : dataSnapshot.getChildren()) {

                    Dictionary dictionary = snapshotDictionary.getValue(Dictionary.class);
                    dictionaries.add(dictionary);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

