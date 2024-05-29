package com.example.task101d.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.task101d.database.AccountDBHelper;
import com.example.task101d.R;

public class AccountCreationActivity extends AppCompatActivity {
    // Declare views
    TextView tvTitle;
    ImageView ivAvatar;
    EditText etUsername, etEmail, etConfirmEmail, etPassword, etConfirmPassword, etPhoneNumber;
    Button btnCreateAccount;

    // DB Helper for writing a new account to memory
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise views
        tvTitle = findViewById(R.id.tvTitle);
        ivAvatar = findViewById(R.id.ivAvatar);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etConfirmEmail = findViewById(R.id.etConfirmEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Create a SpannableString to apply different font sizes
        SpannableString spannableString = new SpannableString("Let's get you\nSetup!");
        spannableString.setSpan(new AbsoluteSizeSpan(32, true), 0, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(48, true), 13, 20, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        // Set the formatted text to the TextView
        tvTitle.setText(spannableString);

        // On click listener for create account button
        btnCreateAccount.setOnClickListener(v -> {
            // Get input values
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String confirmEmail = etConfirmEmail.getText().toString();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();
            String phoneNumber = etPhoneNumber.getText().toString();

            // Create the account
            boolean success = createAccount(username, email, confirmEmail, password, confirmPassword, phoneNumber);

            // Start the InterestsActivity if the account was created successfully
            if (success) {
                Intent intent = new Intent(AccountCreationActivity.this, InterestsActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            }
        });

        // On click listener for profile picture
        ivAvatar.setOnClickListener(v -> {
            // Launch the image picker
            Toast.makeText(AccountCreationActivity.this, "Choose a profile picture", Toast.LENGTH_SHORT).show();
            mGetContentLauncher.launch("image/*");
        });
    }

    // Image picker for profile picture
    private final ActivityResultLauncher<String> mGetContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
        new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    // Set the selected image to the ImageView
                    ivAvatar.setImageURI(result);
                }
            }
        }
    );

    // Validates inputs, inserts the account into the DB, and returns whether the creation was successful
    private boolean createAccount(String username, String email, String confirmEmail, String password, String confirmPassword, String phoneNumber) {
        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || confirmEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
            // Show a toast indicating that all fields are required
            Toast.makeText(AccountCreationActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Check email fields match
        if (!email.equals(confirmEmail)) {
            // Show a toast indicating emails must match
            Toast.makeText(AccountCreationActivity.this, "Email addresses do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Check password fields match
        if (!password.equals(confirmPassword)) {
            // Show a toast indicating emails must match
            Toast.makeText(AccountCreationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Check password complexity
        if (isPasswordWeak(password)) {
            // Validation and toasts are handled by the function called
            return false;     // Return if the password was not complex enough
        }

        // All inputs are validated

        // Get readable and writable database instances
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();

        // Check if an account already exists with this username
        Cursor cursor = dbRead.rawQuery("SELECT * FROM " + AccountDBHelper.TABLE_ACCOUNTS + " WHERE " + AccountDBHelper.COLUMN_USERNAME + "=?", new String[]{username});
        if (cursor.getCount() > 0) {
            // Show a toast indicating that the username is already taken
            Toast.makeText(AccountCreationActivity.this, "An account already exists with this username", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Insert the new account into the database
        ContentValues values = new ContentValues();
        values.put(AccountDBHelper.COLUMN_USERNAME, username);
        values.put(AccountDBHelper.COLUMN_EMAIL, email);
        values.put(AccountDBHelper.COLUMN_PASSWORD, password);
        values.put(AccountDBHelper.COLUMN_PHONE, phoneNumber);
        long id = dbWrite.insert(AccountDBHelper.TABLE_ACCOUNTS, null, values);

        // Close DB
        dbRead.close();
        dbWrite.close();
        cursor.close();

        // Check if the account was inserted successfully
        if (id == -1) {
            // Show a toast if there was an error saving to DB
            Toast.makeText(AccountCreationActivity.this, "Error creating account", Toast.LENGTH_SHORT).show();
            return false;
        }

        // The insertion was successful
        return true;
    }

    // Method to check if password is complex enough
    private boolean isPasswordWeak(String password) {
        // Check if password is at least 8 characters long
        if (password.length() < 8) {
            // Show a toast indicating that the password is not long enough
            Toast.makeText(AccountCreationActivity.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Check if password contains at least one letter and one number / symbol
        boolean hasLetter = false;
        boolean hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                hasLetter = true;
            } else if (!Character.isWhitespace(c)) { // exclude whitespace characters
                hasSymbol = true;
            }
        }

        if (!hasLetter) {
            // Show a toast indicating that the password needs a letter
            Toast.makeText(AccountCreationActivity.this, "Password must contain at least one letter", Toast.LENGTH_SHORT).show();
            return true;
        } else if (!hasSymbol) {
            // Show a toast indicating that the password needs a symbol / number
            Toast.makeText(AccountCreationActivity.this, "Password must contain at least one symbol / number", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
}
