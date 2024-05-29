package com.example.task101d.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.database.AccountDBHelper;
import com.example.task101d.model.InterestItem;
import com.example.task101d.adapter.InterestsAdapter;
import com.example.task101d.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterestsActivity extends AppCompatActivity implements InterestsAdapter.ItemClickListener {
    // Declare views
    TextView tvTitle;
    RecyclerView rvInterests;
    Button btnNext;

    List<String> interestStrings = new ArrayList<>();
    List<InterestItem> interests = new ArrayList<>();
    InterestsAdapter adapter;

    List<String> selectedInterests;
    String username;

    // DB Helper for changing selected interests in saved account
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_interests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get username from Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Get previously selected interests, if any
        selectedInterests = getUsersInterests();

        // Initialise views
        tvTitle = findViewById(R.id.tvTitle);
        rvInterests = findViewById(R.id.rvInterests);
        btnNext = findViewById(R.id.btnNext);

        // Create a SpannableString to apply different font sizes
        SpannableString spannableString = new SpannableString("Your\nInterests");
        spannableString.setSpan(new AbsoluteSizeSpan(56, true), 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(36, true), 5, 14, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        // Set the formatted text to the TextView
        tvTitle.setText(spannableString);

        // Populate list of interests
        interestStrings = Arrays.asList(getResources().getStringArray(R.array.interests_array));
        for (int i = 0; i < interestStrings.size(); i++) {
            interests.add(new InterestItem(interestStrings.get(i)));
        }

        // Setup rvQuizzes with adapter
        rvInterests.setLayoutManager(new LinearLayoutManager(InterestsActivity.this));
        adapter = new InterestsAdapter(interests, InterestsActivity.this);
        adapter.setClickListener(this);

        // Iterate through the interests and mark them as selected if they exist in selectedInterests list
        for (InterestItem interest : interests) {
            if (selectedInterests.contains(interest.getTopic())) {
                interest.setSelected(true);
            }
        }
        rvInterests.setAdapter(adapter);

        // On click listener for next button
        btnNext.setOnClickListener(v -> {
            // Update account details
            String joinedInterests = updateAccount();

            // Start the HomepageActivity if the account was created successfully
            if (joinedInterests != null) {
                Intent intent1 = new Intent(InterestsActivity.this, HomepageActivity.class);
                intent1.putExtra("USERNAME", username);
                intent1.putExtra("INTERESTS", joinedInterests);
                startActivity(intent1);
                finish();
            }
        });
    }

    // Method to get the user's previous interests, if any
    private List<String> getUsersInterests() {
        // Get the interests of the current user from a readable instance of the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {AccountDBHelper.COLUMN_INTERESTS};
        String selection = AccountDBHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(AccountDBHelper.TABLE_ACCOUNTS, projection, selection, selectionArgs, null, null, null);

        // Check if the cursor is not null and move to the first row if possible
        if (cursor != null && cursor.moveToFirst()) {
            // Get the interests - will be a joined CSV
            String joinedInterests = cursor.getString(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_INTERESTS));
            cursor.close(); // close the cursor when done
            db.close();     // close the DB

            if (joinedInterests != null) { // Check if joinedInterests is not null
                // Split the joinedInterests CSV into a list of individual elements, and return it
                return new ArrayList<>(Arrays.asList(joinedInterests.split(",")));
            } else {
                return new ArrayList<>();
            }
        } else {
            // No previously selected interests
            db.close();
            return new ArrayList<>();
        }
    }

    // Updates the account in the DB with the selected interests
    // Returns the selected interests as a CSV String if successful, else null
    private String updateAccount() {
        // Validate selection is between 1 and 10 items
        if (selectedInterests.isEmpty()) {
            Toast.makeText(this, "You must select at least one topic", Toast.LENGTH_SHORT).show();
            return null;
        } else if (selectedInterests.size() > 10) {
            Toast.makeText(this, "You may only select up to 10 topics", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Concat interests into a CSV string
        String joinedInterests = TextUtils.join(",", selectedInterests);

        // Update account details in the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AccountDBHelper.COLUMN_INTERESTS, joinedInterests);
        int rows = db.update(AccountDBHelper.TABLE_ACCOUNTS, values, AccountDBHelper.COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();

        // Check if the update was successful
        if (rows <= 0) {
            // Show a toast if there was an error saving to DB
            Toast.makeText(InterestsActivity.this, "Error updating interests", Toast.LENGTH_SHORT).show();
            return null;
        }

        // The update was successful
        return joinedInterests;
    }

    // On click of an interest, toggle its selection and add/remove it from the list of interests
    @Override
    public void onItemClick(View v, int position) {
        InterestItem interest = adapter.getItem(position);
        Boolean selected = interest.toggleSelected();

        if (selected) {
            selectedInterests.add(interest.getTopic());
        } else {
            selectedInterests.remove(interest.getTopic());
        }
    }
}