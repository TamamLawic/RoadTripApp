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
    ImageView ivIconLogin;
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

        ivIconLogin = findViewById(R.id.ivIconLogin);
        //ivIconLogin.setImageResource(R.mipmap.icon);
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
                // Create the ParseUser
                ParseUser user = new ParseUser();
                // Set core properties
                user.setUsername(etUsername.getText().toString());
                user.setPassword(etPassword.getText().toString());
                // Invoke signUpInBackground
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Hooray! Let them use the app now.
                            goToMainActivity();
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            Toast.makeText(LoginActivity.this, "Unable to Register Account", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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