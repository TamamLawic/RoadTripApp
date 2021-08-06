package com.example.roadtripapp_fbu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


/**
 * Activity the allows the user to log into an existing account, or create a new one using Parse.
 */
public class LoginActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    Button btnLogin;
    Button btnSignUp;

    /** When loginActivity is created either take the user to login page or feed, based on if a user is already logged in
     * Uses Parse to sign a user up for an account if they dont already have one*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //If the current user is logged in, go to main activity
        if (ParseUser.getCurrentUser() != null){
            goToMainActivity();
        }

        etUsername = findViewById(R.id.etCaption);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when the button is clicked, get inputted strings, and attempt login
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

    }

    /** Use Parse verify a user log in attempt, and take them to their feed page if successful */
    private void loginUser(String username, String password) {
        //If log in successful, go to the main activity
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null){
                    return;
                }
                goToMainActivity();
            }
        });
    }

    /**Make and start intent to go to the main activity*/
    private void goToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}