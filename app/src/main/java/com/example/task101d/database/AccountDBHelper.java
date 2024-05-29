package com.example.task101d.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

// Helper class to manage the DB
public class AccountDBHelper extends SQLiteOpenHelper implements BaseColumns {
    public static final String DATABASE_NAME = "accounts.db";
    public static final int DATABASE_VERSION = 5;

    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_INTERESTS = "interests"; // all interests will be stored as CSV
    public static final String COLUMN_TOTAL_QUESTIONS = "total_questions";
    public static final String COLUMN_CORRECT_QUESTIONS = "correct_questions";
    public static final String COLUMN_INCORRECT_QUESTIONS = "incorrect_questions";
    public static final String COLUMN_IS_PREMIUM = "is_premium";

    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_USER_ANSWER = "user_answer";
    public static final String COLUMN_CORRECT_ANSWER = "correct_answer";

    public AccountDBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    // Create a database for storing data locally
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_EMAIL + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT NOT NULL, " +
                COLUMN_INTERESTS + " TEXT, " +
                COLUMN_TOTAL_QUESTIONS + " INTEGER, " +
                COLUMN_CORRECT_QUESTIONS + " INTEGER, " +
                COLUMN_INCORRECT_QUESTIONS + " INTEGER, " +
                COLUMN_IS_PREMIUM + " INTEGER DEFAULT 0" + // Default to non-premium
                ");"
        );

        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " + // Foreign key relation to users
                COLUMN_USER_ANSWER + " TEXT NOT NULL, " +
                COLUMN_CORRECT_ANSWER + " TEXT NOT NULL" +
                ");"
        );
    }

    // On version change, delete the old database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
