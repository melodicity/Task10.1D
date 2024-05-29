package com.example.task101d.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.adapter.HomepageQuizzesAdapter;
import com.example.task101d.database.AccountDBHelper;
import com.example.task101d.model.QuizItem;
import com.example.task101d.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {
    // Declare views
    TextView tvName, tvTotalQuestionsValue, tvCorrectQuestionsValue, tvIncorrectQuestionsValue;
    ImageView ivAvatar;
    RecyclerView rvQuizzes;
    Button btnHistory, btnShare, btnInterests, btnUpgrade;

    List<QuizItem> quizzes = new ArrayList<>();
    HomepageQuizzesAdapter adapter;

    String username;
    String joinedInterests;
    List<String> interests;
    int totalQuestions, correctQuestions, incorrectQuestions, isPremium;

    // DB Helper for getting account interests and question counts
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get username from Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        joinedInterests = intent.getStringExtra("INTERESTS");

        // Using username, query additional data from DB
        getUserData();

        // Initialise views
        tvName = findViewById(R.id.tvName);
        ivAvatar = findViewById(R.id.ivAvatar);
        rvQuizzes = findViewById(R.id.rvQuizzes);
        tvTotalQuestionsValue = findViewById(R.id.tvTotalQuestionsValue);
        tvCorrectQuestionsValue = findViewById(R.id.tvCorrectQuestionsValue);
        tvIncorrectQuestionsValue = findViewById(R.id.tvIncorrectQuestionsValue);
        btnHistory = findViewById(R.id.btnHistory);
        btnShare = findViewById(R.id.btnShare);
        btnInterests = findViewById(R.id.btnInterests);
        btnUpgrade = findViewById(R.id.btnUpgrade);

        // Set views to show user-specific data
        tvName.setText(username);
        tvTotalQuestionsValue.setText(String.valueOf(totalQuestions));
        tvCorrectQuestionsValue.setText(String.valueOf(correctQuestions));
        tvIncorrectQuestionsValue.setText(String.valueOf(incorrectQuestions));

        // Assign a list of quizzes based on the user's preferred topics
        if (joinedInterests != null) {
            interests = Arrays.asList(joinedInterests.split(","));
            Collections.shuffle(interests);
            for (int i = 0; i < interests.size(); i++) {
                quizzes.add(new QuizItem(i+1, interests.get(i)));
            }
        } else {
            // The user has no interest currently, take them to interests activity
            intent = new Intent(HomepageActivity.this, InterestsActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
        }

        // Setup rvQuizzes with adapter
        rvQuizzes.setLayoutManager(new LinearLayoutManager(HomepageActivity.this));
        adapter = new HomepageQuizzesAdapter(quizzes, HomepageActivity.this, username);
        rvQuizzes.setAdapter(adapter);

        // On click listener for each button (each starts its matching activity)
        btnHistory.setOnClickListener(v -> startActivityIfPremium(HistoryActivity.class));
        btnShare.setOnClickListener(v -> startActivityIfPremium(ShareActivity.class));
        btnInterests.setOnClickListener(v -> startActivity(InterestsActivity.class));
        btnUpgrade.setOnClickListener(v -> startActivity(UpgradeActivity.class));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        // Using username, query additional data from DB
        getUserData();

        // Set views to show user-specific data
        tvName.setText(username);
        tvTotalQuestionsValue.setText(String.valueOf(totalQuestions));
        tvCorrectQuestionsValue.setText(String.valueOf(correctQuestions));
        tvIncorrectQuestionsValue.setText(String.valueOf(incorrectQuestions));

        // Reload quizzes based on the updated interests
        if (joinedInterests != null) {
            interests = Arrays.asList(joinedInterests.split(","));
            Collections.shuffle(interests);
            quizzes.clear();
            for (int i = 0; i < interests.size(); i++) {
                quizzes.add(new QuizItem(i + 1, interests.get(i)));
            }
            adapter.notifyDataSetChanged();
        }
    }

    // Method to get the user's question counts
    private void getUserData() {
        // Get the data from a readable instance of the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                AccountDBHelper.COLUMN_TOTAL_QUESTIONS,
                AccountDBHelper.COLUMN_CORRECT_QUESTIONS,
                AccountDBHelper.COLUMN_INCORRECT_QUESTIONS,
                AccountDBHelper.COLUMN_IS_PREMIUM
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
                isPremium = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_IS_PREMIUM));
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

    // Helper method to start an activity
    public void startActivity(Class<?> newActivity) {
        // Create an intent to start the new activity
        Intent intent = new Intent(HomepageActivity.this, newActivity);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    // Helper method to impose premium/free account restrictions on button functionality
    public void startActivityIfPremium(Class<?> newActivity) {
        if (isPremium == 0) { // Not premium (default)
            Toast.makeText(HomepageActivity.this, "You must upgrade to premium to use this feature!", Toast.LENGTH_SHORT).show();
        } else {
            // Premium account is active
            startActivity(newActivity);
        }
    }
}