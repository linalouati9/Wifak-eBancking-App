package com.example.wifaq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CheckTransactionAdapter extends ArrayAdapter<CheckTransaction> {
    private Helper helper;
    private String nCompte;
    private static class ViewHolder {
        TextView checkId;
        TextView amount;
        TextView beneficiaryName;
        TextView sourceInfos;
        TextView provenceaAndDate;
        TextView memo;
    }

    public CheckTransactionAdapter(Context context, List<CheckTransaction> transactions, String nCompte) {
        super(context, 0, transactions);
        this.helper = new Helper(context);
        this.nCompte = nCompte;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckTransaction transaction = getItem(position);
        CheckTransactionAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.checktransaction_item_activity, parent, false);
            viewHolder = new CheckTransactionAdapter.ViewHolder();

            viewHolder.checkId = convertView.findViewById(R.id.checkId);
            viewHolder.amount = convertView.findViewById(R.id.amount);
            viewHolder.beneficiaryName = convertView.findViewById(R.id.beneficiaryName);
            viewHolder.sourceInfos = convertView.findViewById(R.id.sourceInfos);
            viewHolder.provenceaAndDate = convertView.findViewById(R.id.provenceaAndDate);
            viewHolder.memo = convertView.findViewById(R.id.memo);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CheckTransactionAdapter.ViewHolder) convertView.getTag();
        }

        if (nCompte.equals(transaction.getBeneficiaryAccountNumber())) {
            viewHolder.checkId.setText(String.format("Check Id : %d", transaction.getCheckId()));
            viewHolder.amount.setText(String.format("+%.2f TND", transaction.getAmount()));
            viewHolder.amount.setTextColor(ContextCompat.getColor(getContext(), R.color.green));

            viewHolder.beneficiaryName.setText(transaction.getBeneficiaryName());
            viewHolder.sourceInfos.setText("FROM " + transaction.getSourceHolder() + " - " + transaction.getSourceRIB());

            String message1 = transaction.getProvence() != null ? transaction.getProvence() : "";
            if (transaction.getDate() != null) {
                message1 += ", le " + transaction.getDate();
            }
            viewHolder.provenceaAndDate.setText(message1);

            String message2 = transaction.getMemo() != null ? transaction.getMemo() : "No comment";
            viewHolder.memo.setText(message2);

        } else {
            if (helper.getAccountByRIB(transaction.getSourceRIB()).getNCompte().equals(nCompte)) {
                viewHolder.checkId.setText("Check Id : " + String.valueOf(transaction.getCheckId()));
                viewHolder.amount.setText("-" + String.valueOf(transaction.getAmount()) + " TND");
                viewHolder.amount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));

                viewHolder.beneficiaryName.setText(transaction.getBeneficiaryName());
                viewHolder.sourceInfos.setText("FROM CURRENT ACCOUNT");

                String message1 = transaction.getProvence() != null ? transaction.getProvence() : "";
                if (transaction.getDate() != null) {
                    message1 += ", le " + transaction.getDate();
                }
                viewHolder.provenceaAndDate.setText(message1);

                String message2 = transaction.getMemo() != null ? transaction.getMemo() : "No comment";
                viewHolder.memo.setText(message2);
            }
        }

        return convertView;
    }
}
