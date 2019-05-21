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

public class RegisterActivity extends AppCompatActivity {

    private Button buttonRegister;
    private TextView textViewSignIn, textViewRegisterUser;
    private EditText editTextPasswordRegister, editTextEmaiRegister;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            // start MainMenuAcivity
            finish();
            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
            startActivity(intent);
        }

        progressDialog = new ProgressDialog(this);
        editTextEmaiRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        textViewRegisterUser = findViewById(R.id.textViewUserRegister);
        textViewSignIn = findViewById(R.id.textViewSignIn);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start MainActivity
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void registerUser() {
        String email = editTextEmaiRegister.getText().toString().trim();
        String password = editTextPasswordRegister.getText().toString().trim();

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
        progressDialog.setMessage("Register..");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if (task.isSuccessful()) {
                    // user is successfully registered and logged in we will start the MainMenuActivity here
                    finish();
                    Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(intent);
                } else {

                    Toast.makeText(getApplicationContext(), "Colud not register. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
