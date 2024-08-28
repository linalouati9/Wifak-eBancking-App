package com.example.wifaq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class ChequeDepositActivity extends AppCompatActivity {

    long userId;
    String nCompte;
    String selectedCard;
    private Helper h = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.cheque_deposit_activity);

            userId = getIntent().getLongExtra("userId", -1);
            nCompte = getIntent().getStringExtra("nCompte");
            selectedCard = getIntent().getStringExtra("selectedCard");

            if (savedInstanceState == null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                Fragment1 fragment1 = new Fragment1();
                Fragment2 fragment2 = new Fragment2();
                Fragment3 fragment3 = new Fragment3();
                Bundle bundle = new Bundle();
                bundle.putLong("userId", userId); // Pass the data
                bundle.putString("nCompte", nCompte);
                bundle.putString("selectedCard", selectedCard);
                fragment3.setArguments(bundle);

                transaction.add(R.id.fragment_container, fragment1, "Fragment1");
                transaction.add(R.id.fragment_container, fragment2, "Fragment2");
                transaction.add(R.id.fragment_container, fragment3, "Fragment3");

                transaction.hide(fragment2);
                transaction.hide(fragment3);

                transaction.commit();
            }
            setupBottomNavigation();
        }

    public void showFragment1() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag("Fragment1");
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag("Fragment2");
        Fragment fragment3 = getSupportFragmentManager().findFragmentByTag("Fragment3");

        if (fragment1 != null) transaction.show(fragment1);
        if (fragment2 != null) transaction.hide(fragment2);
        if (fragment3 != null) transaction.hide(fragment3);

        transaction.commit();
    }

    public void showFragment2() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag("Fragment1");
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag("Fragment2");
        Fragment fragment3 = getSupportFragmentManager().findFragmentByTag("Fragment3");

        if (fragment1 != null) transaction.hide(fragment1);
        if (fragment2 != null) transaction.show(fragment2);
        if (fragment3 != null) transaction.hide(fragment3);

        transaction.commit();
    }

    public void showFragment3() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag("Fragment1");
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag("Fragment2");
        Fragment fragment3 = getSupportFragmentManager().findFragmentByTag("Fragment3");

        if (fragment1 != null) transaction.hide(fragment1);
        if (fragment2 != null) transaction.hide(fragment2);
        if (fragment3 != null) transaction.show(fragment3);

        transaction.commit();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.chequeDeposit);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.mycards) {
                Intent intent = new Intent(ChequeDepositActivity.this, CardsActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("nCompte", nCompte);
                intent.putExtra("selectedCard", this.selectedCard);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.transferOperation) {
                if (!Objects.equals(selectedCard, "")) {
                    Intent intent = new Intent(ChequeDepositActivity.this, TransferActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("nCompte", nCompte);
                    intent.putExtra("selectedCard", selectedCard);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ChequeDepositActivity.this, "You cannot perform a transaction without selecting a credit card. Please return to the Cards section to choose one", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.chequeDeposit) {
                return true;
            }else if (itemId == R.id.cdh){
                Intent intent = new Intent(ChequeDepositActivity.this, CheckDepositHistoryActivity.class);
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


    }






