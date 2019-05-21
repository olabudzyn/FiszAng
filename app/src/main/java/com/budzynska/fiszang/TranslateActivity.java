package com.budzynska.fiszang;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TranslateActivity extends AppCompatActivity {

    private Button btnTranslate, btnAddToDictionary;
    private TextView txvTranslated, txvToTranslate;
   // private static final String API_KEY = "AIzaSyDsKajjSP6Dnk6wYc9S_EvzIEqEw98WQfc";
    private List<Dictionary> dictionaries;
    private String dictionaryId;

    private Translate translate;
    private TranslateOptions options;
    private Translation translation;
    private Handler handler = new Handler();
    private String translatedText, selectedText, allText;
    private Vision vision;

    private DatabaseReference databaseDictionaries;
    private DatabaseReference databaseWords;


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

        Intent i = getIntent();
        allText = i.getStringExtra("text");
        txvToTranslate.setText(allText);

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer(getString(R.string.api_key)));

        vision = visionBuilder.build();

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                if(translatedText != null) {
                    showDictionaryDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot add word", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }

    private void addToDictionary() {

        databaseWords = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId);
        String wordsId = databaseWords.push().getKey();
        DictionaryElement words = new DictionaryElement(wordsId, selectedText.toLowerCase(), translatedText.toLowerCase());
        databaseWords.child(wordsId).setValue(words).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                txvTranslated.setText("");
                Toast.makeText(getApplicationContext(), "Word added successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void translate() {

        if(selectedText != null){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                options = TranslateOptions.newBuilder().setApiKey(getString(R.string.api_key)).build();
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
    }

    private void showDictionaryDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TranslateActivity.this, R.style.MyDialogTheme);
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
                addToDictionary();
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

