package com.budzynska.fiszang;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogIn;
    private TextView textViewSignUp, textViewUserLogin;
    private EditText editTextPasswordLogin, editTextEmailLogin;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogIn = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewUserLogin = findViewById(R.id.textViewUserLoginName);
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            // go to MainMenuActivity
            finish();
            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
            startActivity(intent);
        }

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
    }

    private void logIn() {

        String email = editTextEmailLogin.getText().toString().trim();
        String password = editTextPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {

            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {

            Toast.makeText(this, "Password must have 6 signs at least.", Toast.LENGTH_SHORT).show();
            return;
        }

        // if validation are ok show progress dialog
        progressDialog.setMessage("Log in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if (task.isSuccessful()) {
                    // start MainMenuActivity
                    finish();
                    Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Incorrect email or password. Please try again.", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
