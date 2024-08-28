package com.example.wifaq;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Objects;

public class CardsActivity extends AppCompatActivity {

    private Helper helper;
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;
    private List<Card> cards;
    private ListView transactionsListView;
    private TextView noTransactionsTextView;
    private TextView transactionsHistoryTextView;
    private Card selectedCard;

    private User user;
    private BankAccount bankAccount;
    private long userId;
    private String compteId;
    private String selectedCard_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards_activity);

        helper = new Helper(CardsActivity.this);

        transactionsListView = findViewById(R.id.transactionsListView);
        noTransactionsTextView = findViewById(R.id.noTransactionsTextView);
        transactionsHistoryTextView = findViewById(R.id.transactionsHistoryTextView);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initially From MainActivity
        userId = getIntent().getLongExtra("userId", -1);
        compteId = getIntent().getStringExtra("nCompte");

        // From another activities
        selectedCard_str = getIntent().getStringExtra("selectedCard");

        if (userId != -1 && compteId != null) {
            TextView rib = findViewById(R.id.RIB);
            rib.setText("R.I.B - " + helper.getBankAccount(null, compteId).getRIB());

            user = helper.getUser(null, userId); // fetch user details by the userId = CIN
            bankAccount = helper.getBankAccount(null, compteId);

            cards = helper.getCards(bankAccount.getNCompte());

            if (cards == null || cards.isEmpty()) {
                // Case impossible to have : e-banking accounts are given to people with at least 1 credit card
                Toast.makeText(this, "No cards found for this user.", Toast.LENGTH_LONG).show();
                //return;
            }

            // Found MASTER card
            Card masterCard = null;
            int masterCardIndex = -1;
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).getFunctionnality() == Card.Functionnality.MASTER) {
                    masterCard = cards.get(i);
                    masterCardIndex = i;
                    break;
                }
            }

            // Swap the MASTER card with the first card, if it exists
            if (masterCard != null && masterCardIndex != -1) {
                // Échanger les cartes dans la liste
                Card firstCard = cards.get(0);
                cards.set(0, masterCard);
                cards.set(masterCardIndex, firstCard);
            }

            if (selectedCard_str != null) {
                for (Card card : cards) {
                    if (card.getCardNumber().equals(selectedCard_str)) {
                        selectedCard = card;
                        break;
                    }
                }
            }

            cardAdapter = new CardAdapter(cards, new CardAdapter.OnCardSelectedListener() {
                @Override
                public void onCardSelected(Card card) {
                    selectedCard = card;
                    viewTransactionsHistory(selectedCard); // transactions history of selected card
                }
            }, bankAccount);

            recyclerView.setAdapter(cardAdapter);

            if (selectedCard != null) {
                int position = cards.indexOf(selectedCard);
                if (position != -1) {
                    recyclerView.scrollToPosition(position);
                    cardAdapter.setSelectedCard(position);
                    viewTransactionsHistory(selectedCard);
                }
            }

            setupBottomNavigation();
        }else{
            // Impossible case
            Intent intent = getIntent();
            finish();
        }
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mycards);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.mycards) {
                // Handle My Cards action
                return true;
            }else if (itemId == R.id.transferOperation) {
                if (userId != -1 && selectedCard != null && compteId != null) {
                    Intent intent = new Intent(CardsActivity.this, TransferActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("nCompte", compteId);
                    intent.putExtra("selectedCard", selectedCard.getCardNumber());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CardsActivity.this, "Please select a credit card first", Toast.LENGTH_SHORT).show();
                }
                return true;
            }else if (itemId == R.id.chequeDeposit) {
                Intent intent = new Intent(CardsActivity.this, ChequeDepositActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", compteId);
                if (selectedCard != null) {
                    intent.putExtra("selectedCard", selectedCard.getCardNumber());
                }else {
                    intent.putExtra("selectedCard", "");
                }
                startActivity(intent);
                return true;
            }else if (itemId == R.id.cdh) {
                Intent intent = new Intent(CardsActivity.this, CheckDepositHistoryActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", compteId);
                if (selectedCard != null) {
                    intent.putExtra("selectedCard", selectedCard.getCardNumber());
                }else{
                    intent.putExtra("selectedCard", "");
                }
                startActivity(intent);
                finish();
                return true;
            }else{
                return false;
            }
        });
    }

    private void viewTransactionsHistory(Card card) {
        List<Transaction> transactions = helper.getTransactionsHistory(card);

        transactionsHistoryTextView.setVisibility(View.VISIBLE);
        if (transactions == null || transactions.isEmpty()) {
            transactionsListView.setVisibility(View.GONE);
            noTransactionsTextView.setVisibility(View.VISIBLE);
        } else {
            noTransactionsTextView.setVisibility(View.GONE);
            transactionsListView.setVisibility(View.VISIBLE);

            TransactionAdapter adapter = new TransactionAdapter(this, transactions, card);
            transactionsListView.setAdapter(adapter);
        }
    }
}
