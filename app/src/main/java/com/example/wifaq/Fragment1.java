package com.example.wifaq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment1 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cheque_deposit_activity1, container, false);

        Button nextButton = view.findViewById(R.id.next);
        nextButton.setOnClickListener(v -> {
            ((ChequeDepositActivity) requireActivity()).showFragment2();
        });

        return view;
    }
}
