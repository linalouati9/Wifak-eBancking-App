package com.example.wifaq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<Card> cards;
    private OnCardSelectedListener listener;
    private int selectedPosition = -1; // Store the selected position
    private BankAccount bankAccount;

    public interface OnCardSelectedListener {
        void onCardSelected(Card card);
    }

    public CardAdapter(List<Card> cards, OnCardSelectedListener listener, BankAccount bankAccount) {
        this.cards = cards;
        this.listener = listener;
        this.bankAccount = bankAccount;
    }

    // Method to set the initially selected card
    public void setSelectedCard(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged(); // Update the display
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);

        LocalDate expiration_date = card.getExpirationDate();
        int month = 0;
        int year = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            month = expiration_date.getMonthValue();
            year = expiration_date.getYear();
        }
        String expiration_date_str = String.format("%02d/%d", month, year); // Format MM/YYYY

        // Update card details
        holder.cardNumber.setText(card.getCardNumber());
        holder.cardHolder.setText(card.getHolder());
        holder.expirationDate.setText(expiration_date_str);
        holder.cardCategory.setText(card.getCategory().toString());
        holder.cardType.setImageResource(card.getCategory() == Card.Category.GOLD_NATIONALE ? R.drawable.ic_mastercard : R.drawable.ic_visa);
        holder.cardFunctionnality.setText(
                card.getFunctionnality() == Card.Functionnality.MASTER ? "MASTER" : "SLAVE"
        );

        // Update the balance based on card functionality
        if (card.getFunctionnality() == Card.Functionnality.MASTER && bankAccount != null) {
            holder.balance.setText(String.format("%s TND", bankAccount.getBalance()));
        } else {
            holder.balance.setText(String.format("%s TND", card.getBalance()));
        }

        // Update the checked state
        holder.isChecked.setChecked(position == selectedPosition);

        // Set the click listener for the radio button
        holder.isChecked.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            listener.onCardSelected(card);

            // Refresh only the items that need updating
            notifyItemChanged(previousPosition); // Deselect the old card
            notifyItemChanged(selectedPosition); // Select the new card
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumber, cardHolder, expirationDate, balance, cardCategory, cardFunctionnality;
        ImageView cardType;
        RadioButton isChecked;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNumber = itemView.findViewById(R.id.cardNumber);
            cardHolder = itemView.findViewById(R.id.cardHolder);
            expirationDate = itemView.findViewById(R.id.expirationDate);
            balance = itemView.findViewById(R.id.balance);
            cardCategory = itemView.findViewById(R.id.cardCategory);
            cardType = itemView.findViewById(R.id.cardType);
            isChecked = itemView.findViewById(R.id.isChecked);
            cardFunctionnality = itemView.findViewById(R.id.functionnality);
        }
    }
}
