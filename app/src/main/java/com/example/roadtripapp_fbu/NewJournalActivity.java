package com.example.roadtripapp_fbu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;

public class NewJournalActivity extends AppCompatActivity {
    public static final String TAG  = "NewJournalActivity";
    EditText etTitle;
    EditText etJournal;
    ImageButton btnPostJournal;
    Trip clicked_trip;

    /** Sets up on click listeners for buttons to take photo and post update **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journal);

        etJournal = findViewById(R.id.etJournal);
        etTitle = findViewById(R.id.etTitle);
        btnPostJournal = findViewById(R.id.btnPostJournal);

        //use Parcels to unwrap trip selected
        //unwrap post's data from the pass
        clicked_trip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Trip.class.getSimpleName()));

        //when post picture button is clicked, post to current trip
        btnPostJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etJournal.getText().toString();
                String title = etTitle.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(NewJournalActivity.this, "Journal can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.isEmpty()) {
                    Toast.makeText(NewJournalActivity.this, "Title can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                savePost(text, title);
            }
        });
    }

    /** Save journal entry in a background thread, sets the parameters to the current fields of the View.*/
    private void savePost(String text, String title) {
        JournalEntry journal = new JournalEntry();
        journal.setText(text);
        journal.setTripId(clicked_trip);
        journal.setTitle(title);
        journal.setUser(ParseUser.getCurrentUser());
        journal.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(NewJournalActivity.this, "Error while saving journal!", Toast.LENGTH_SHORT).show();
                }
                //if post saved successfully, remove input fields
                etTitle.setText("");
                etJournal.setText("");
                Intent intent = new Intent();
                intent.putExtra("journal", Parcels.wrap(journal));//set result code and bundle response
                setResult(RESULT_OK, intent);//return results okay, and intent
                finish();//end and return user back to timeline
            }
        });
    }
}