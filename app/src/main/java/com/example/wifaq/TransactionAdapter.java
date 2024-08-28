package com.example.wifaq;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private final Card card;
    private final Helper h;

    private static class ViewHolder {
        TextView sourceCreditCardHolderTextView;
        TextView sourceCreditCardNumberTextView;
        TextView dateAndTimeTextView;
        TextView amountTextView;
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, Card card) {
        super(context, 0, transactions);
        this.h = new Helper(context);
        this.card = card;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Transaction transaction = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_item_activity, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.sourceCreditCardHolderTextView = convertView.findViewById(R.id.sourceCreditCardHolder);
            viewHolder.sourceCreditCardNumberTextView = convertView.findViewById(R.id.sourceCreditCardNumber);
            viewHolder.dateAndTimeTextView = convertView.findViewById(R.id.dateAndTime);
            viewHolder.amountTextView = convertView.findViewById(R.id.amount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set transaction details to the view holder
        if (transaction.getSourceCardNumber().equals(card.getCardNumber())) {
            Card destCard = h.getCreditCardById(transaction.getDestCardNumber());
            viewHolder.sourceCreditCardHolderTextView.setText(destCard.getHolder());
            viewHolder.sourceCreditCardNumberTextView.setText(destCard.getCardNumber());
            viewHolder.amountTextView.setText(String.format("- %s TND", transaction.getAmount()));
            viewHolder.amountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));

        }
        if (transaction.getDestCardNumber().equals(card.getCardNumber())) {
            Card sourceCard = h.getCreditCardById(transaction.getSourceCardNumber());
            viewHolder.sourceCreditCardHolderTextView.setText(sourceCard.getHolder());
            viewHolder.sourceCreditCardNumberTextView.setText(sourceCard.getCardNumber());
            viewHolder.amountTextView.setText(String.format("+ %s TND", transaction.getAmount()));
            viewHolder.amountTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));

        }
        // Format and set date and time
        LocalDate date = transaction.getTransactionDate();
        LocalTime time = transaction.getTransactionTime();

        int day = 0; int month = 0; int year = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            day = date.getDayOfMonth();
            month = date.getMonthValue();
            year = date.getYear();
        }
        String dateStr = String.format("%02d/%02d/%04d, ", day, month, year);

        int hour = 0;
        int minute = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hour = time.getHour();
            minute = time.getMinute();
        }
        String timeStr;
        if (hour >= 12) {
            timeStr = String.format("%02d:%02d PM", hour - 12, minute);
        } else {
            timeStr = String.format("%02d:%02d AM", hour, minute);
        }

        viewHolder.dateAndTimeTextView.setText(String.format("%s %s", dateStr, timeStr));

        return convertView;
    }
}
