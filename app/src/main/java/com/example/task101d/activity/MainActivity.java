package com.example.task101d.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.task101d.database.AccountDBHelper;
import com.example.task101d.widget.ClickableTextView;
import com.example.task101d.R;

public class MainActivity extends AppCompatActivity {
    // Declare views
    TextView tvWelcome;
    EditText etUsername, etPassword;
    Button btnLogin;
    ClickableTextView ctvNeedAccount;

    // DB Helper for reading account details from memory (login check)
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise views
        tvWelcome = findViewById(R.id.tvWelcome);
        ctvNeedAccount = findViewById(R.id.ctvNeedAccount);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Create a SpannableString to apply different font sizes
        SpannableString spannableString = new SpannableString("Welcome,\nStudent!\nLet's Start Learning!");
        spannableString.setSpan(new AbsoluteSizeSpan(42, true), 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(64, true), 9, 17, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(28, true), 18, 39, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        // Set the formatted text to the TextView
        tvWelcome.setText(spannableString);

        // On click listener for login button
        btnLogin.setOnClickListener(v -> {
            // Get input values
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // Check the login details
            login(username, password);
        });

        // On click listener for "need an account" text view
        ctvNeedAccount.setOnClickListener(v -> {
            // Get input values
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // Navigate to the Account Creation activity
            Intent intent = new Intent(MainActivity.this, AccountCreationActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("PASSWORD", password);
            startActivity(intent);
        });
    }

    // Helper method to validate login against DB, and go to the HomepageActivity if there is a match
    private void login(String username, String password) {
        // Validate inputs exist
        if (username.isEmpty() || password.isEmpty()) {
            // Show a toast indicating that both fields are required
            Toast.makeText(MainActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get a readable instance of the database to check login details
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define a projection to get the needed columns - username, password (and interests for later)
        String[] projection = {
                AccountDBHelper.COLUMN_USERNAME,
                AccountDBHelper.COLUMN_PASSWORD,
                AccountDBHelper.COLUMN_INTERESTS  // Will be passed with the intent for use later
        };

        // Define the selection criteria
        String selection = AccountDBHelper.COLUMN_USERNAME + " = ? AND " +
                           AccountDBHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        // Query the database for a match based on the selection criteria and args
        Cursor cursor = db.query(AccountDBHelper.TABLE_ACCOUNTS, projection, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // Login was successful!
            // Get the interests - will be a joined CSV
            String joinedInterests = cursor.getString(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_INTERESTS));

            // Navigate to the homepage activity
            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("INTERESTS", joinedInterests);
            startActivity(intent);
            cursor.close(); // Close the cursor when done

            etUsername.setText("");
            etPassword.setText("");
        } else {
            // If login failed, show a toast indicating the login details were invalid
            Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }

        // Close the DB connection
        db.close();
    }
}