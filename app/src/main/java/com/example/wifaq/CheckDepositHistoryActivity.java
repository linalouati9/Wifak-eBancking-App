package com.example.wifaq;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Objects;

public class CheckDepositHistoryActivity extends AppCompatActivity {
    private Helper helper;
    private List<CheckTransaction> transactions;
    private CheckTransactionAdapter adapter = null;

    long userId;
    String nCompte;
    String selectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.checkdeposit_history_activity);

        userId = getIntent().getLongExtra("userId", -1);
        nCompte = getIntent().getStringExtra("nCompte");
        selectedCard = getIntent().getStringExtra("selectedCard");

        helper = new Helper(CheckDepositHistoryActivity.this);
        transactions = helper.getCheckTransactionsHistoty();

        ImageView noTransactionsImageView = findViewById(R.id.noTransactionsImageView);
        TextView noTransactionsTextView = findViewById(R.id.noTransactionsTextView);
        ListView listView = findViewById(R.id.cdh);

        if(transactions.size() > 0){
            listView.setVisibility(View.VISIBLE);
            adapter = new CheckTransactionAdapter(CheckDepositHistoryActivity.this, transactions, nCompte);
            listView.setAdapter(adapter);
        }else{
            noTransactionsImageView.setVisibility(View.VISIBLE);
            noTransactionsTextView.setVisibility(View.VISIBLE);
        }


        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.cdh);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.mycards) {
                Intent intent = new Intent(CheckDepositHistoryActivity.this, CardsActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", nCompte);
                intent.putExtra("selectedCard", this.selectedCard);

                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.transferOperation) {
                if (!Objects.equals(selectedCard, "")) {
                    Intent intent = new Intent(CheckDepositHistoryActivity.this, TransferActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("nCompte", nCompte);
                    intent.putExtra("selectedCard", selectedCard);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckDepositHistoryActivity.this, "You cannot perform a transaction without selecting a credit card. Please return to the Cards section to choose one", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.chequeDeposit) {
                Intent intent = new Intent(CheckDepositHistoryActivity.this, ChequeDepositActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", nCompte);
                intent.putExtra("selectedCard", selectedCard);
                startActivity(intent);
                finish();
                return true;
            }else if (itemId == R.id.cdh) {
                return true;
            }else{
                return false;
            }
        });
    }

}
