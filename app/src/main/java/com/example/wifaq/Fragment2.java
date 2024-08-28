package com.example.wifaq;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.cheque_deposit_activity2, container, false);

        Button disagreeBtn = view.findViewById(R.id.disagree);
        Button acceptBtn = view.findViewById(R.id.accept);

        disagreeBtn.setOnClickListener(v -> {
            ((ChequeDepositActivity) requireActivity()).showFragment1();
        });

        acceptBtn.setOnClickListener(v -> {
            ((ChequeDepositActivity) requireActivity()).showFragment3();
        });

        return view;
    }

}

