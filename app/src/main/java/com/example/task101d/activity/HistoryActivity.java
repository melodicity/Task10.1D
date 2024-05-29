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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.R;
import com.example.task101d.adapter.ResultsAdapter;
import com.example.task101d.database.AccountDBHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    // Declare views
    RecyclerView rvHistory;

    ResultsAdapter adapter;

    String[] selectedAnswers = new String[0];
    String[] correctAnswers = new String[0];
    boolean[] answerStates = new boolean[0];

    String username;
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
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
        rvHistory = findViewById(R.id.rvHistory);

        // Setup rvHistory with adapter (uses same ResultsAdapter from quiz answers)
        rvHistory.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
        adapter = new ResultsAdapter(answerStates, selectedAnswers, correctAnswers, HistoryActivity.this);
        rvHistory.setAdapter(adapter);
    }

    // Get the user's history
    private void getUserData() {
        // Get the data from a readable instance of the History DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                AccountDBHelper.COLUMN_USER_ANSWER,
                AccountDBHelper.COLUMN_CORRECT_ANSWER
        };
        String selection = AccountDBHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(AccountDBHelper.TABLE_HISTORY, projection, selection, selectionArgs, null, null, null);

        // Iterate through the database, fetching all history belonging to the current user
        List<String> userAnswers = new ArrayList<>(), actualAnswers = new ArrayList<>();
        while (cursor.moveToNext()) {
            String userAnswer = cursor.getString(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_USER_ANSWER));
            String correctAnswer = cursor.getString(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_CORRECT_ANSWER));

            userAnswers.add(userAnswer);
            actualAnswers.add(correctAnswer);
        }

        cursor.close(); // close the cursor when done
        db.close();     // close the DB

        // Initialise the array fields of these lists, to be used by the results adapter
        selectedAnswers = userAnswers.toArray(new String[0]);
        correctAnswers = actualAnswers.toArray(new String[0]);

        int size = correctAnswers.length;
        answerStates = new boolean[size];

        // Populate answer states array
        for (int i = 0; i < size; i++) {
            if (selectedAnswers[i] != null && correctAnswers[i] != null) {
                answerStates[i] = selectedAnswers[i].equals(correctAnswers[i]);
            } else {
                answerStates[i] = false; // Consider it incorrect if either selected or correct answer is null
            }
        }
    }
}