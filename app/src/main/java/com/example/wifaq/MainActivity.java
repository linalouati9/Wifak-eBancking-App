package com.example.wifaq;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SEND_SMS = 1;
    private static final int CODE_VALIDITY_DURATION = 10 * 60 * 1000; // 10 minutes in milliseconds

    private Helper h;
    private AlertDialog dialog;
    private String generatedCode;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private String username = "";
    private String password = "";
    private long userId;
    private String nCompte = "";
    private TextView timerTextView;

    private User user;
    private BankAccount bankAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // use the entire surface of the screen,
        setContentView(R.layout.activity_main);

        h = new Helper(MainActivity.this);

        try {
            h.createDatabase();
            h.openDatabase();
        } catch (IOException e) {
            showToast("Database error: " + e.getMessage());
        }

        Button login = findViewById(R.id.login);
        login.setOnClickListener(view -> {
            username = ((EditText) findViewById(R.id.username)).getText().toString();
            password = ((EditText) findViewById(R.id.password)).getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Please fill in all fields");
            } else {
                Account account = h.getAccount(username, password);

                if (account != null) {
                    userId = account.getUserId();
                    nCompte = account.getNCompte();
                    showToast("User connected successfully ..");
                    grantAccess();
                } else {
                    showToast("User not found. Check your informations !");
                }
            }
        });

        TextView forgetPassword = findViewById(R.id.forgetPassword);
        forgetPassword.setOnClickListener(view -> {
            username = ((EditText) findViewById(R.id.username)).getText().toString();

            if (username.isEmpty()) {
                showToast("Please enter your username");
            } else {
                user = h.getUser(username, 0);
                bankAccount = h.getBankAccount(username, null);

                if (user != null && !user.getPhone().isEmpty()) {
                    userId = user.getCin();
                    nCompte = bankAccount.getNCompte();

                    generatedCode = generateRandomCode();
                    sendSms(user.getPhone(), "Your password reset code is: " + generatedCode + "\nThe code is valid for 10 minutes.");
                } else {
                    showToast("User with username : " +username+",not found. Or phone number missing !");
                }
            }
        });
    }

    private void sendSms(String phone, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            try {
                smsManager.sendTextMessage(phone, null, message, null, null);
                showToast("SMS sent. Check your phone");
                showCodeEntryDialog();
            } catch (Exception e) {
                showToast("Failed to send SMS: " + e.getMessage());
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms(user.getPhone(), "Your password reset code is: " + generatedCode + "\nThe code is valid for 10 minutes.");
            } else {
                showToast("SMS permission denied");
            }
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void startTimer() {
        timeLeftInMillis = CODE_VALIDITY_DURATION;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;

                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                timerTextView.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                generatedCode = null;
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                timerTextView.setText("");
                showToast("Time expired. Please try again");
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void showCodeEntryDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.forgetpassworddialog_layout, null);

        EditText[] digitInputs = new EditText[]{
                dialogView.findViewById(R.id.digit1),
                dialogView.findViewById(R.id.digit2),
                dialogView.findViewById(R.id.digit3),
                dialogView.findViewById(R.id.digit4),
                dialogView.findViewById(R.id.digit5),
                dialogView.findViewById(R.id.digit6)
        };
        timerTextView = dialogView.findViewById(R.id.timer_text_view);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Verification Code");
        builder.setView(dialogView);
        builder.setPositiveButton("Verify", null);
        builder.setNegativeButton("Cancel", null);

        startTimer();

        dialog = builder.create();
        dialog.show();

        setupDigitInputs(digitInputs);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String enteredCode = getEnteredCode(digitInputs);

            if (generatedCode != null && generatedCode.equals(enteredCode)) {
                dialog.dismiss();
                stopTimer();
                grantAccess();
            } else {
                showToast("Invalid code. Please try again");
                clearDigitInputs(digitInputs);
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            stopTimer();
            dialog.dismiss();
        });
    }

    private void setupDigitInputs(EditText[] digitInputs) {
        for (int i = 0; i < digitInputs.length; i++) {
            EditText currentEditText = digitInputs[i];
            final int nextIndex = i + 1;

            currentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0 && nextIndex < digitInputs.length) {
                        digitInputs[nextIndex].requestFocus();
                    }
                }
            });
        }
    }

    private String getEnteredCode(EditText[] digitInputs) {
        StringBuilder codeBuilder = new StringBuilder();
        for (EditText input : digitInputs) {
            codeBuilder.append(input.getText().toString());
        }
        return codeBuilder.toString();
    }

    private void clearDigitInputs(EditText[] digitInputs) {
        for (EditText input : digitInputs) {
            input.setText("");
        }
    }

    private void grantAccess() {
        Intent intent = new Intent(MainActivity.this, CardsActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("nCompte", nCompte);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
