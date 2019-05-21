package com.budzynska.fiszang;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.budzynska.fiszang.basedata.DictionaryElement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlashcardActivity extends AppCompatActivity {

    private Button buttonNext;
    private TextView textViewFlashcard;
    private TextView textViewName;
    private boolean flag = true;
    private DictionaryElement word;
    private DatabaseReference databaseWords;
    private List<DictionaryElement> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        buttonNext = findViewById(R.id.buttonNext);
        textViewFlashcard = findViewById(R.id.textViewCardWords);
        textViewName = findViewById(R.id.textViewCard);

        words = new ArrayList<>();

        Intent intent = getIntent();
        String dictionaryId = getIntent().getStringExtra(DictionaryActivity.DICTIONARY_ID);
        databaseWords = FirebaseDatabase.getInstance().getReference(MainMenuActivity.WORDS_PATH).child(dictionaryId);

        databaseWords.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.d("Error", databaseError.getMessage());
            }
        });


        textViewFlashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (flag == true) {
                    textViewFlashcard.setText(word.getPolishWord());
                    textViewFlashcard.setBackgroundResource(R.drawable.flashcardshape);
                    textViewFlashcard.setTextColor(getColor(R.color.white2));
                    flag = false;
                } else {
                    textViewFlashcard.setText(word.getEnglishWord());
                    textViewFlashcard.setBackgroundResource(R.drawable.flashcardshape2);
                    textViewFlashcard.setTextColor(getColor(R.color.dark));
                    flag = true;
                }
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word = getRandomWord(words);
                textViewFlashcard.setBackgroundResource(R.drawable.flashcardshape2);
                textViewFlashcard.setText(word.getEnglishWord());
                textViewFlashcard.setTextColor(getColor(R.color.dark));
                flag = true;
            }
        });
    }

    private DictionaryElement getRandomWord(List<DictionaryElement> w) {

        Random r = new Random();
        int s = w.size();
        int randomNumber = r.nextInt(s);
        DictionaryElement randomWord = w.get(randomNumber);

        return randomWord;
    }

    private void showData(DataSnapshot dataSnapshot) {

        for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
            DictionaryElement word = wordSnapshot.getValue(DictionaryElement.class);

            words.add(word);
        }

        if (words.size() != 0) {
            word = getRandomWord(words);
            textViewFlashcard.setText(word.getEnglishWord());
        }
    }

    private boolean changeColor(boolean f) {

        if (f == true) {
            textViewFlashcard.setText(word.getPolishWord());
            textViewFlashcard.setBackgroundColor(R.drawable.flashcardshape);
            f = false;
        } else {
            textViewFlashcard.setText(word.getEnglishWord());
            textViewFlashcard.setBackgroundColor(R.drawable.flashcardshape2);
            f = true;
        }

        return f;
    }

}
