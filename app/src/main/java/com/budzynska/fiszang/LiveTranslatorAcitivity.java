package com.budzynska.fiszang;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budzynska.fiszang.basedata.Dictionary;
import com.budzynska.fiszang.basedata.DictionaryElement;
import com.budzynska.fiszang.listview.DictionaryList;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiveTranslatorAcitivity extends AppCompatActivity {

    private TextView txvTranslated, txvToTranslate;
    private Button buttonSave;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private static final int requestPermissionID = 101;
    private static final String API_KEY = "AIzaSyDsKajjSP6Dnk6wYc9S_EvzIEqEw98WQfc";

    private Translate translate;
    private TranslateOptions options;
    private Translation translation;
    private Handler handler = new Handler();
    private String  translateTextTranslation;
    private String currentText ="";

    private List<Dictionary> dictionaries;
    private String dictionaryId;

    private DatabaseReference databaseDictionaries;
    private DatabaseReference databaseWords;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_translator_activity);

        txvTranslated = findViewById(R.id.txvTranslated2);
        txvToTranslate = findViewById(R.id.txvToTranslate2);
        surfaceView = findViewById(R.id.surfaceView);
        buttonSave = findViewById(R.id.buttonSaveLiveTranslate);

        dictionaries = new ArrayList<>();

        databaseDictionaries = FirebaseDatabase.getInstance().getReference(MainMenuActivity.DICTIONARY_PATH).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        buildList();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDictionaryDialog();
            }
        });

        startCameraSource();

    }

    private void showDictionaryDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LiveTranslatorAcitivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.list_dictionaries_dialog, null);

        ListView lv = convertView.findViewById(R.id.listViewDialog);
        DictionaryList dictionaryListAdapter = new DictionaryList(getApplicationContext(), dictionaries);
        lv.setAdapter(dictionaryListAdapter);


        alertDialog.setView(convertView);

        AlertDialog dialog = alertDialog.create();
        dialog.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dictionary dictionary = dictionaries.get(position);
                dictionaryId = dictionary.getDictionaryId();
                dialog.dismiss();

                int startSelection = txvToTranslate.getSelectionStart();
                int endSelection = txvToTranslate.getSelectionEnd();
                String selectedText = txvToTranslate.getText().subSequence(startSelection, endSelection).toString();
                addToDictionary(selectedText);
            }
        });

    }

    private void addToDictionary(String selectedText) {

        databaseWords = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId);
        String wordsId = databaseWords.push().getKey();
        String translatedText = translate(selectedText);
        DictionaryElement words = new DictionaryElement(wordsId, selectedText.toLowerCase(), translatedText.toLowerCase());
        databaseWords.child(wordsId).setValue(words).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Word added successfully!", Toast.LENGTH_SHORT).show();
            }
        });
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

    public String translate(String translateText) {
        //translateText = txvToTranslate.getText().toString(); // words to translate

        String translatedText;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params){
                options = TranslateOptions.newBuilder().setApiKey(API_KEY).build();
                translate = options.getService();
                translation = translate.translate(translateText, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("pl"));
               handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (txvTranslated != null) {
                            translateTextTranslation = translation.getTranslatedText();
                        }
                    }
                });
                return null;
            }
        }.execute();

        translatedText = translateTextTranslation;
        return translatedText;
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
                                    txvToTranslate.setText(currentText);
                                    String translated = translate(currentText);
                                    txvTranslated.setText(translated);
                                }

                            }
                        });
                    }
                }
            });
        }
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
