package com.example.task101d.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.R;
import com.example.task101d.database.AccountDBHelper;

public class ShareActivity extends AppCompatActivity {
    // Declare views
    TextView tvName, tvTotalQuestionsValue, tvCorrectQuestionsValue, tvIncorrectQuestionsValue;
    Button btnShare;

    String username;
    int totalQuestions, correctQuestions, incorrectQuestions;
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_share);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get username from Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Using username, query additional data from DB
        getUserData();

        // Initialise views
        tvName = findViewById(R.id.tvName);
        tvTotalQuestionsValue = findViewById(R.id.tvTotalQuestionsValue);
        tvCorrectQuestionsValue = findViewById(R.id.tvCorrectQuestionsValue);
        tvIncorrectQuestionsValue = findViewById(R.id.tvIncorrectQuestionsValue);
        btnShare = findViewById(R.id.btnShare);

        // Set views to show user-specific data
        tvName.setText(username);
        tvTotalQuestionsValue.setText(String.valueOf(totalQuestions));
        tvCorrectQuestionsValue.setText(String.valueOf(correctQuestions));
        tvIncorrectQuestionsValue.setText(String.valueOf(incorrectQuestions));

        // On click listener for share button
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String title = username + "'s Quiz Results";
            String body = "Total Questions: " + totalQuestions +
                          "\nCorrectly Answered: " + correctQuestions +
                          "\nIncorrect Answers: " + incorrectQuestions;
            shareIntent.putExtra(Intent.EXTRA_TITLE, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(shareIntent, "Share using"));
        });
    }

    // Method to get the user's question counts
    private void getUserData() {
        // Get the data from a readable instance of the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                AccountDBHelper.COLUMN_TOTAL_QUESTIONS,
                AccountDBHelper.COLUMN_CORRECT_QUESTIONS,
                AccountDBHelper.COLUMN_INCORRECT_QUESTIONS,
        };
        String selection = AccountDBHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(AccountDBHelper.TABLE_ACCOUNTS, projection, selection, selectionArgs, null, null, null);

        // Check if the cursor is not null and move to the first row if possible
        if (cursor != null && cursor.moveToFirst()) {
            // Get the question counts for total, correct and incorrect
            try {
                totalQuestions = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_TOTAL_QUESTIONS));
                correctQuestions = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_CORRECT_QUESTIONS));
                incorrectQuestions = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_INCORRECT_QUESTIONS));
            } catch (Exception ignored)
            {
                // No previous questions were answered
            }

            cursor.close(); // close the cursor when done
            db.close();     // close the DB
        } else {
            // No previously selected interests
            db.close();
        }
    }
}