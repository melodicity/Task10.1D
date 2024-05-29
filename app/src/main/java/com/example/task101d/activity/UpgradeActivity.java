package com.example.task101d.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.task101d.BuildConfig;
import com.example.task101d.R;
import com.example.task101d.database.AccountDBHelper;
import com.example.task101d.network.StripeApiClient;
import com.example.task101d.network.StripePaymentIntentResponse;
import com.example.task101d.network.StripeApiService;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpgradeActivity extends AppCompatActivity {
    // Declare views
    Button btnPurchase;

    String username;
    int isPremium;
    AccountDBHelper dbHelper = new AccountDBHelper(this);

    // Declare Stripe Payment API vars
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private static final String PUBLISHABLE_KEY = BuildConfig.STRIPE_PUBLISHABLE_KEY;
    private static final int PREMIUM_COST = 99; // 99 cents
    private static final String CURRENCY = "aud";
    private static final String PAYMENT_METHOD = "card";
    public static final String MERCHANT_DISPLAY_NAME = "Example, Inc.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upgrade);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise Stripe config and payment sheet
        PaymentConfiguration.init(getApplicationContext(), PUBLISHABLE_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Get username from Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Using username, query isPremium from DB
        getUserData();

        // Initialise "purchase" button and disable it initially (until payment API is ready)
        btnPurchase = findViewById(R.id.btnPurchase);
        btnPurchase.setEnabled(false);
        btnPurchase.setOnClickListener(v -> presentPaymentSheet());
        if (isPremium == 1) {
            // If user is already premium, change button text to reflect that
            btnPurchase.setText(R.string.premium_edition_already_bought);
            btnPurchase.setBackgroundResource(R.drawable.button_accent);
            btnPurchase.setTextColor(getColor(R.color.accent10));
        } else {
            // Premium has not been bought yet

            // Asynchronously fetch payment intent from Stripe API
            // Will enable payment button when client-server connection is made
            fetchPaymentIntent();
        }
    }

    private void fetchPaymentIntent() {
        StripeApiService service = StripeApiClient.getStripeApiService();
        Call<StripePaymentIntentResponse> call = service.createPaymentIntent(PREMIUM_COST, CURRENCY, PAYMENT_METHOD);

        call.enqueue(new Callback<StripePaymentIntentResponse>() {
            @Override
            public void onResponse(@NonNull Call<StripePaymentIntentResponse> call, @NonNull Response<StripePaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paymentIntentClientSecret = response.body().getClientSecret();

                    // Enable the pay button, as a connection is established with the API
                    btnPurchase.setEnabled(true);
                } else {
                    Toast.makeText(UpgradeActivity.this, "Failed to create payment intent", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StripePaymentIntentResponse> call, @NonNull Throwable t) {
                Toast.makeText(UpgradeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PaymentIntent", "Error: " + t.getMessage(), t);
            }
        });
    }

    // Shows the payment sheet (overlay over this activity) - the user can then input bank details
    private void presentPaymentSheet() {
        if (paymentIntentClientSecret != null) {
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder(MERCHANT_DISPLAY_NAME).build();
            paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);

            /*
             * NOTE: Stripe TEST MODE is active
             * Use test card number '4242 4242 4242 4242' with any future expiry date and any CVC
             */

        } else {
            Toast.makeText(this, "Payment Intent not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // Callback for when a payment is submitted
    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Payment was successful, call method to handle updating account data
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            premiumPurchased();
        } else {
            // Payment was not successful
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Get the user's account premium status
    private void getUserData() {
        // Get the data from a readable instance of the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {AccountDBHelper.COLUMN_IS_PREMIUM};
        String selection = AccountDBHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(AccountDBHelper.TABLE_ACCOUNTS, projection, selection, selectionArgs, null, null, null);

        // Check if the cursor is not null and move to the first row if possible
        if (cursor != null && cursor.moveToFirst()) {
            // Get the isPremium value
            try {
                isPremium = cursor.getInt(cursor.getColumnIndexOrThrow(AccountDBHelper.COLUMN_IS_PREMIUM));
            } catch (Exception ignored) {
                // Unknown payment model, default to lite edition
                isPremium = 0;
            }

            cursor.close(); // close the cursor when done
            db.close();     // close the DB
        } else {
            // No previous payment data
            isPremium = 0;
            db.close();
        }
    }

    // Handles the logic for when a premium account purchase is successful
    // Changes the account data in the DB, and the payment button
    private void premiumPurchased() {
        isPremium = 1;

        btnPurchase.setText(R.string.you_have_premium);
        btnPurchase.setBackgroundResource(R.drawable.button_accent);
        btnPurchase.setTextColor(getColor(R.color.accent10));
        btnPurchase.setEnabled(false);

        // Update account details in the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AccountDBHelper.COLUMN_IS_PREMIUM, isPremium);
        int rows = db.update(AccountDBHelper.TABLE_ACCOUNTS, values, AccountDBHelper.COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();

        // Check if the update was successful
        if (rows <= 0) {
            // Show a toast if there was an error saving to DB
            Toast.makeText(UpgradeActivity.this, "DATABASE ERROR", Toast.LENGTH_SHORT).show();
            Log.e("Error", "Premium was bought, but error saving to the DB. Initiate a refund!");
        }
    }
}