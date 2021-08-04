package com.example.roadtripapp_fbu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
/** Creates new Parse Account with the user's inputted profile information*/
public class CreateAccountActivity extends AppCompatActivity {
    Button btnLogn;
    Button btnCreateAccount;
    EditText etNewUsername;
    EditText etNewpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnLogn = findViewById(R.id.btnLogIn);
        etNewpassword = findViewById(R.id.etNewPassword);
        etNewUsername = findViewById(R.id.etNewUsername);

        //set button to create a new account using parse
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the ParseUser
                ParseUser user = new ParseUser();
                // Set core properties
                user.setUsername(etNewUsername.getText().toString());
                user.setPassword(etNewpassword.getText().toString());
                // Invoke signUpInBackground
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Hooray! Let them use the app now.
                            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            Toast.makeText(CreateAccountActivity.this, "Unable to Register Account", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //set button to go back to log in page
        btnLogn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}