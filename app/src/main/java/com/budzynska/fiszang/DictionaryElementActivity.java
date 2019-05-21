package com.budzynska.fiszang;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budzynska.fiszang.basedata.Dictionary;
import com.budzynska.fiszang.basedata.DictionaryElement;
import com.budzynska.fiszang.listview.DictionaryElementList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DictionaryElementActivity extends AppCompatActivity {

    private Button buttonAddNewElement, buttonFlash;
    private ListView listViewWords;
    private TextView textViewWords;
    private List<DictionaryElement> dictionaryElements;
    private String dictionaryId;

    private DatabaseReference databaseWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_element);

        buttonAddNewElement = findViewById(R.id.buttonAddElement);
        listViewWords = findViewById(R.id.listViewElements);
        textViewWords = findViewById(R.id.txvDictionaryElement);
        buttonFlash = findViewById(R.id.buttonFlashcards);

        Intent intent = getIntent();
        dictionaryId = intent.getStringExtra(DictionaryActivity.DICTIONARY_ID);

        databaseWords = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId);

        dictionaryElements = new ArrayList<>();

        buttonAddNewElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddWordsDialog();
            }
        });

        listViewWords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DictionaryElement dictionaryElement = dictionaryElements.get(position);

                showOptionDialog(dictionaryElement.getElementId(), dictionaryElement.getEnglishWord(), dictionaryElement.getPolishWord());

            }
        });

        buttonFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FlashcardActivity.class);
                i.putExtra(DictionaryActivity.DICTIONARY_ID, dictionaryId);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseWords.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dictionaryElements.clear();

                for (DataSnapshot snapshotDictionary : dataSnapshot.getChildren()) {

                    DictionaryElement dictionaryElement = snapshotDictionary.getValue(DictionaryElement.class);
                    dictionaryElements.add(dictionaryElement);
                }

                DictionaryElementList dictionaryListAdapter = new DictionaryElementList(DictionaryElementActivity.this, dictionaryElements);
                listViewWords.setAdapter(dictionaryListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showAddWordsDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.add_words_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextAddEnglishWord = dialogView.findViewById(R.id.editTextAddEnglishWord);
        final EditText editTextAddPolishWord = dialogView.findViewById(R.id.editTextAddPolishWord);
        final Button buttonSave = dialogView.findViewById(R.id.buttonSaveWords);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String englishWord = editTextAddEnglishWord.getText().toString().trim();
                String polishWord = editTextAddPolishWord.getText().toString().trim();


                if (TextUtils.isEmpty(englishWord)) {
                    editTextAddEnglishWord.setError("English word required");
                    return;
                }

                if (TextUtils.isEmpty(polishWord)) {
                    editTextAddEnglishWord.setError("Polish word required");
                    return;
                }
                addNewElement(englishWord, polishWord);
                alertDialog.dismiss();
            }
        });
    }

    private void addNewElement(String englishWord, String polishWord) {

        String id = databaseWords.push().getKey();
        DictionaryElement dictionaryElement = new DictionaryElement(id, englishWord.toLowerCase(), polishWord.toLowerCase());

        databaseWords.child(id).setValue(dictionaryElement);
        Toast.makeText(getApplicationContext(), "Word added successfully", Toast.LENGTH_SHORT).show();
    }


    private void showOptionDialog(final String elementId, String english, String polish) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.option_dictionary_element_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextUpdateEnglishWord = dialogView.findViewById(R.id.editTextUpdateEnglishWord);
        final EditText editTextUpdatePolishWord = dialogView.findViewById(R.id.editTextUpdatePolishWord);
        final Button buttonSave = dialogView.findViewById(R.id.buttonSaveUpdateWord);
        final Button buttonDelete = dialogView.findViewById(R.id.deleteElement);

        editTextUpdateEnglishWord.setText(english);
        editTextUpdatePolishWord.setText(polish);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String englishWord = editTextUpdateEnglishWord.getText().toString().trim();
                String polishWord = editTextUpdatePolishWord.getText().toString().trim();


                if (TextUtils.isEmpty(englishWord)) {
                    editTextUpdateEnglishWord.setError("English word required");
                    return;
                }

                if (TextUtils.isEmpty(polishWord)) {
                    editTextUpdatePolishWord.setError("Polish word required");
                    return;
                }
                updateElement(elementId, englishWord, polishWord);
                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteElement(elementId);
                alertDialog.dismiss();
            }
        });

    }

    private void updateElement(String id, String englishWord, String polishWord) {

        DatabaseReference databaseWord = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId).child(id);
        DictionaryElement dictionaryElement = new DictionaryElement(id, englishWord, polishWord);
        databaseWord.setValue(dictionaryElement);

        Toast.makeText(getApplicationContext(), "Word updated succesfully", Toast.LENGTH_SHORT).show();

    }

    private void deleteElement(String id) {

        DatabaseReference databaseWord = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId).child(id);
        databaseWord.removeValue();

        Toast.makeText(getApplicationContext(), "Word is deleted", Toast.LENGTH_SHORT).show();
    }
}
