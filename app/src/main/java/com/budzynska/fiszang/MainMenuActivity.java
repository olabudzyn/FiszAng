package com.budzynska.fiszang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenuActivity extends AppCompatActivity {

    public static final String USER_ID = "userid";
    public static final String DICTIONARY_PATH  = "dictionaries";
    public static final String WORDS_PATH  = "words";

    private Button btnDictionary, btnLiveTranslator, btnImageTranslator, btnLogout;
    private TextView textViewWelcome;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_activity);

        btnDictionary = findViewById(R.id.buttonDictionary);
        btnImageTranslator = findViewById(R.id.buttonImageTranslation);
        btnLiveTranslator = findViewById(R.id.buttonLiveTranslation);
        btnLogout = findViewById(R.id.buttonLogout);
        textViewWelcome = findViewById(R.id.textViewWelcome);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            finish();
            Intent intent = new Intent(this, MainActivity.class);
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        textViewWelcome.setText("Welcome " + user.getEmail());

    }

    public void moveToLiveTranslation(View view) {

        Intent intent = new Intent(MainMenuActivity.this, LiveTranslatorAcitivity.class);
        startActivity(intent);
    }

    public void moveToImageTranslation(View view) {
        Intent intent = new Intent(MainMenuActivity.this, ImageTranslatorActivity.class);
        startActivity(intent);
    }

    public void moveToDictionary(View view) {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String id = user.getUid();
        Intent intent = new Intent(getApplicationContext(), DictionaryActivity.class);
        intent.putExtra(USER_ID, id);
        startActivity(intent);
    }

    public void logOut(View view) {
        firebaseAuth.signOut();
        finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
