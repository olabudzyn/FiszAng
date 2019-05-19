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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.budzynska.fiszang.basedata.Dictionary;
import com.budzynska.fiszang.listview.DictionaryList;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {

    private static final String DICTIONARY_ID = "dicionaryid";
    private static final String DICTIONARY_NAME = "dicionaryname";
    private TextView textViewDictionary;
    private ListView listViewDictionaries;
    private Button buttonAddDictionary;

    private List<Dictionary> dictionaries;
    private DatabaseReference databaseDictionaries;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictionary_activity);

        Intent intent = getIntent();
        String userId = intent.getStringExtra(MainMenuActivity.USER_ID);
        Log.d("DATABASE", "User ID: " + userId);
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
                addArtist(name);
                alertDialog.dismiss();
            }
        });
    }

    private void addArtist(String name) {

        String id = databaseDictionaries.push().getKey();
        Dictionary dictionary = new Dictionary(name, id);

        databaseDictionaries.child(id).setValue(dictionary);
        Toast.makeText(getApplicationContext(), "Artist added", Toast.LENGTH_SHORT).show();


    }
}
