package com.example.wifaq;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TransferActivity extends AppCompatActivity {

    private TFLiteModel model = null;
    private Helper h;
    private String selectedCard;
    private long userId;
    private String nCompte;

    private BankAccount bankAccount;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.transfer_activity);

        model = new TFLiteModel(TransferActivity.this);
        h = new Helper(TransferActivity.this);

        userId = getIntent().getLongExtra("userId", -1);
        nCompte = getIntent().getStringExtra("nCompte");
        selectedCard = getIntent().getStringExtra("selectedCard"); // Impossible to be null

        Card card = h.getCreditCardById(selectedCard);
        bankAccount = h.getBankAccount(null, nCompte);

        EditText sourceCard = findViewById(R.id.sourceCreditCardNumber);
        sourceCard.setEnabled(false); // Disables user interaction
        sourceCard.setText(card.getCardNumber());

        String sourceCardText = sourceCard.getText().toString();

        Button confirmBtn = findViewById(R.id.confirm);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText destCard = findViewById(R.id.destinationCreditCardNumber);
                EditText amount = findViewById(R.id.amount);

                String destCardText = destCard.getText().toString();
                String amountText = amount.getText().toString();

                if(sourceCardText.isEmpty() || destCardText.isEmpty() || amountText.isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(), "All fields are required !", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    return;
                //} else if(Float.parseFloat(amountText) < 10){
                    //Toast toast = Toast.makeText(getApplicationContext(), "The minimum transaction amount must be 10 TND", Toast.LENGTH_LONG);
                    //toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
                    //toast.show();
                    //return;
                }else {
                    Card destinationCard = h.getCreditCardById(destCardText);
                    if(destinationCard == null){
                        Toast toast = Toast.makeText(getApplicationContext(), "Target credit card under number = " + destCardText  +" not found !", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                        return;
                    }

                    // Fraud detection
                    float step = getNumberBasedOnCurrentTime();
                    float amnt = Float.parseFloat(amountText);
                    float isFlaggedFraud = amnt > 200000 ? 1.0f : 0.0f;
                    float[][] input = {{step, 1.0f, amnt, isFlaggedFraud}};
                    float result = model.predict(input);

                    AlertDialog.Builder builder = new AlertDialog.Builder(TransferActivity.this);
                    LayoutInflater inflater = getLayoutInflater(); // Android class to convert layout XML files to View objects
                    View dialogView = inflater.inflate(R.layout.dialog_layout, null);

                    ImageView imageView = dialogView.findViewById(R.id.dialog_image);
                    TextView messageTextView = dialogView.findViewById(R.id.dialog_message);

                    // fraud transaction
                    if(result <= 0.5) {
                        imageView.setImageResource(R.drawable.ic_non_fraud);
                        messageTextView.setText(String.format("At %.2f%%\nThis transaction is not fraudulent",  result * 100));

                        builder.setTitle("TRANSACTION RESULT")
                                .setView(dialogView)
                                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Transaction transaction = new Transaction(
                                                sourceCardText, destCardText, LocalDate.now(ZoneId.of("GMT")),
                                                LocalTime.now(ZoneId.of("Europe/Paris")).minusHours(1), amnt, Transaction.Type.TRANSFER);

                                        // Test
                                        // The authorized amount is available to make the transaction, and the amount actually exists in the bank account
                                        if(checkTransactionValidity(transaction, card, bankAccount)){
                                            long transferFeedBack = h.applyTransferTransaction(transaction, bankAccount);
                                            if(transferFeedBack != -1) {
                                                Toast toast = Toast.makeText(getApplicationContext(), "Transaction applied successfully", Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
                                                toast.show();

                                                Intent intent = new Intent(TransferActivity.this, CardsActivity.class);
                                                intent.putExtra("userId", userId);
                                                intent.putExtra("nCompte", nCompte);
                                                intent.putExtra("selectedCard", selectedCard);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                Toast toast = Toast.makeText(getApplicationContext(), "A problem occurs while adding the transaction", Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
                                                toast.show();
                                                dialog.dismiss();
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        refreshActivity();
                                    }
                                });
                    } else {
                        imageView.setImageResource(R.drawable.ic_fraud);
                        messageTextView.setText(String.format("At %.2f%%\nThis transaction is fraudulent", result * 100));

                        builder.setTitle("TRANSACTION RESULT")
                                .setView(dialogView)
                                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        refreshActivity();
                                    }
                                });
                    }

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        setupBottomNavigation();
    }

    private static int getNumberBasedOnCurrentTime() {
        long hoursElapsed = 1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

            hoursElapsed = ChronoUnit.HOURS.between(startOfMonth, now);
        }

        return (int) (hoursElapsed + 1);
    }

    private boolean checkTransactionValidity(Transaction transaction, Card card, BankAccount bankAccount){

        if((card.getBalance() >= transaction.getAmount()) && (bankAccount.getBalance() >= transaction.getAmount())){
            return true;
        }
        return false;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.transferOperation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.mycards) {
                Intent intent = new Intent(TransferActivity.this, CardsActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", nCompte);
                intent.putExtra("selectedCard", selectedCard);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.transferOperation) {
                return true;
            } else if (itemId == R.id.chequeDeposit) {
                Intent intent = new Intent(TransferActivity.this, ChequeDepositActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", nCompte);
                intent.putExtra("selectedCard", selectedCard);
                startActivity(intent);
                finish();
                return true;
            }else if (itemId == R.id.cdh){
                Intent intent = new Intent(TransferActivity.this, CheckDepositHistoryActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", nCompte);
                intent.putExtra("selectedCard", selectedCard);
                startActivity(intent);
                finish();
                return true;
            }else{
                return false;
            }
        });
    }

    public void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}

