package com.budzynska.fiszang;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.budzynska.fiszang.basedata.Dictionary;
import com.budzynska.fiszang.listview.DictionaryList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {

    public static final String DICTIONARY_ID = "dicionaryid";
    private TextView textViewDictionary;
    private ListView listViewDictionaries;
    private Button buttonAddDictionary;
    private String userId;

    private List<Dictionary> dictionaries;
    private DatabaseReference databaseDictionaries;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictionary_activity);

        Intent intent = getIntent();
        userId = intent.getStringExtra(MainMenuActivity.USER_ID);
        databaseDictionaries = FirebaseDatabase.getInstance().getReference(MainMenuActivity.DICTIONARY_PATH).child(userId);

        dictionaries = new ArrayList<>();

        textViewDictionary = findViewById(R.id.txvDictionary);
        listViewDictionaries = findViewById(R.id.lisViewDictionaries);
        buttonAddDictionary = findViewById(R.id.buttonAddDictionary);
        
        buttonAddDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDictionaryDialog();
            }
        });

        listViewDictionaries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Dictionary dictionary = dictionaries.get(position);

                Intent intent = new Intent(getApplicationContext(), DictionaryElementActivity.class);
                intent.putExtra(DICTIONARY_ID, dictionary.getDictionaryId());

                startActivity(intent);
            }
        });

        listViewDictionaries.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Dictionary dictionary = dictionaries.get(position);
                showOptionDialog(dictionary.getDictionaryId(), dictionary.getDictionaryName());
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseDictionaries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dictionaries.clear();

                for(DataSnapshot snapshotDictionary: dataSnapshot.getChildren()){

                    Dictionary dictionary = snapshotDictionary.getValue(Dictionary.class);
                    dictionaries.add(dictionary);
                }

                DictionaryList dictionaryListAdapter = new DictionaryList(DictionaryActivity.this, dictionaries);
                listViewDictionaries.setAdapter(dictionaryListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showAddDictionaryDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.add_dialog,null);
        dialogBuilder.setView(dialogView);

        final EditText editTextDictionaryName = dialogView.findViewById(R.id.editTextAddDictionaryName);
        final Button buttonSave = dialogView.findViewById(R.id.buttonSaveDictionary);

        dialogBuilder.setTitle("Add Dictionary");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextDictionaryName.getText().toString().trim();

                if(TextUtils.isEmpty(name)){
                    editTextDictionaryName.setError("Name required");
                    return;
                }
                addDictionary(name);
                alertDialog.dismiss();
            }
        });
    }

    private void addDictionary(String name) {

        String id = databaseDictionaries.push().getKey();
        Dictionary dictionary = new Dictionary(name, id);

        databaseDictionaries.child(id).setValue(dictionary);
        Toast.makeText(getApplicationContext(), "Dictionary added successfully", Toast.LENGTH_SHORT).show();
    }

    private void showOptionDialog(final String dictionaryId, String dictionaryName){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.option_dictionary_dialog,null);
        dialogBuilder.setView(dialogView);

        final EditText editTextDictionaryUpdateName = dialogView.findViewById(R.id.editTextUpdateDictionary);
        final Button buttonSave = dialogView.findViewById(R.id.buttonSaveUpdateDictionary);
        final Button buttonDelete = dialogView.findViewById(R.id.deleteDictionary);

        editTextDictionaryUpdateName.setText(dictionaryName);
        dialogBuilder.setTitle("Update or delete dictionary");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = editTextDictionaryUpdateName.getText().toString().trim();

                if(TextUtils.isEmpty(name)){
                    editTextDictionaryUpdateName.setError("Name required");
                    return;
                }

                updateDictionary(dictionaryId, name);
                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDictionary(dictionaryId);
                alertDialog.dismiss();
            }
        });
    }

    private void deleteDictionary(String id){
        DatabaseReference databaseDictionary = FirebaseDatabase.getInstance().getReference(MainMenuActivity.DICTIONARY_PATH).child(userId).child(id);
        DatabaseReference databaseWords = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(id);

        databaseDictionary.removeValue();
        databaseWords.removeValue();

        Toast.makeText(getApplicationContext(), "Dictionary deleted", Toast.LENGTH_SHORT).show();
    }

    private void updateDictionary(String id, String name){

        DatabaseReference databaseDictionary = FirebaseDatabase.getInstance().getReference(MainMenuActivity.DICTIONARY_PATH).child(userId).child(id);
        Dictionary dictionary = new Dictionary(name, id);
        databaseDictionary.setValue(dictionary);

        Toast.makeText(getApplicationContext(), "Dictionary updated successfully", Toast.LENGTH_SHORT).show();
    }
}
