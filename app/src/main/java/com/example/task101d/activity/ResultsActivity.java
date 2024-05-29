package com.example.task101d.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.R;
import com.example.task101d.adapter.ResultsAdapter;
import com.example.task101d.database.AccountDBHelper;


public class ResultsActivity extends AppCompatActivity {
    // Declare views
    RecyclerView rvAnswers;
    Button btnContinue;

    ResultsAdapter adapter;

    boolean[] answerStates;
    String[] selectedAnswers, correctAnswers;
    int correctAnswerCount, incorrectAnswerCount;

    String username;

    // DB Helper for updating account history and question counts
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise views
        rvAnswers = findViewById(R.id.rvAnswers);
        btnContinue = findViewById(R.id.btnContinue);

        // Get results from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        selectedAnswers = intent.getStringArrayExtra("SELECTED_ANSWERS");
        correctAnswers = intent.getStringArrayExtra("CORRECT_ANSWERS");
        assert correctAnswers != null;
        int size = correctAnswers.length;
        answerStates = new boolean[size];

        // Populate answer states array and count the number of correct/incorrect answers
        for (int i = 0; i < size; i++) {
            if (selectedAnswers[i] != null && correctAnswers[i] != null) {
                answerStates[i] = selectedAnswers[i].equals(correctAnswers[i]);
            } else {
                answerStates[i] = false; // Consider it incorrect if either selected or correct answer is null
            }

            if (answerStates[i]) {
                correctAnswerCount++;
            } else {
                incorrectAnswerCount++;
            }
        }

        // Setup rvQuizzes with adapter
        rvAnswers.setLayoutManager(new LinearLayoutManager(ResultsActivity.this));
        adapter = new ResultsAdapter(answerStates, selectedAnswers, correctAnswers, ResultsActivity.this);
        rvAnswers.setAdapter(adapter);

        // Call method to update the user's account with these results (history and question counts)
        updateAccount();

        // On click listener for continue button
        btnContinue.setOnClickListener(v -> finish()); // closing this activity will return to the homepage
    }

    // Updates the account in the DB with the new history and question counts
    private void updateAccount() {
        // Declare data vars to be updated
        int totalQuestions = 0, correctQuestions = 0, incorrectQuestions = 0;

        // Ensure username is not null
        if (username == null || username.isEmpty()) {
            Toast.makeText(ResultsActivity.this, "Username is null or empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get old data to update
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
        String[] projection = {
                AccountDBHelper.COLUMN_TOTAL_QUESTIONS,
                AccountDBHelper.COLUMN_CORRECT_QUESTIONS,
                AccountDBHelper.COLUMN_INCORRECT_QUESTIONS
        };
        String selection = AccountDBHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = dbRead.query(AccountDBHelper.TABLE_ACCOUNTS, projection, selection, selectionArgs, null, null, null);

        // Check if the cursor is not null and move to the first row if possible
        if (cursor != null && cursor.moveToFirst()) {
            // Get the old data
            try {
                // Get the question counts for total, correct and incorrect
                totalQuestions = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_TOTAL_QUESTIONS));
                correctQuestions = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_CORRECT_QUESTIONS));
                incorrectQuestions = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_INCORRECT_QUESTIONS));
            } catch (Exception ignored)
            {
                // No previous questions were answered
            }

            cursor.close(); // close the cursor when done
        }
        dbRead.close();     // close the read DB

        // Now write to the DB
        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AccountDBHelper.COLUMN_TOTAL_QUESTIONS, totalQuestions + correctAnswerCount + incorrectAnswerCount);
        values.put(AccountDBHelper.COLUMN_CORRECT_QUESTIONS, correctQuestions + correctAnswerCount);
        values.put(AccountDBHelper.COLUMN_INCORRECT_QUESTIONS, incorrectQuestions + incorrectAnswerCount);
        int rows = dbWrite.update(AccountDBHelper.TABLE_ACCOUNTS, values, AccountDBHelper.COLUMN_USERNAME + " = ?", new String[]{username});

        // Check if the update was successful
        if (rows <= 0) {
            // Show a toast if there was an error saving to DB
            Toast.makeText(ResultsActivity.this, "Error updating account data", Toast.LENGTH_SHORT).show();
        }


        // Also update the history data table by adding the newly answered questions
        for (int i = 0; i < selectedAnswers.length; i++) {
            if (selectedAnswers[i] != null && correctAnswers[i] != null) { // Ensure answers are not null
                ContentValues historyValues = new ContentValues();
                historyValues.put(AccountDBHelper.COLUMN_USERNAME, username);
                historyValues.put(AccountDBHelper.COLUMN_USER_ANSWER, selectedAnswers[i]);
                historyValues.put(AccountDBHelper.COLUMN_CORRECT_ANSWER, correctAnswers[i]);
                long historyRowId = dbWrite.insert(AccountDBHelper.TABLE_HISTORY, null, historyValues);

                // Check if the insert was successful
                if (historyRowId == -1) {
                    // Show a toast if there was an error saving to DB
                    Toast.makeText(ResultsActivity.this, "Error updating history data", Toast.LENGTH_SHORT).show();
                }
            }
        }

        dbWrite.close();     // close the write DB
    }
}