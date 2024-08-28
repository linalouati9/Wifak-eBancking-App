package com.example.wifaq;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;

// Tesseract is not an efficient OCR for checks since it is written by hand: use Google Vision API
public class Fragment3 extends Fragment {

    private static final int CAMERA_CAPTURE_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Helper helper;
    // private TessBaseAPI tessBaseAPI;
    private long userId;
    private String nCompte;
    private String selectedCard;

    private ImageView checkFront;
    private ImageView checkBack;

    private String extractedText;
    private ImageView imageViewToUpdate; // Track which ImageView needs updating
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cheque_deposit_activity3, container, false);

        helper = new Helper(requireContext());
        if (getArguments() != null) {
            userId = getArguments().getLong("userId");
            nCompte = getArguments().getString("nCompte");
            selectedCard = getArguments().getString("selectedCard");
        } else {
            throw new IllegalStateException("Arguments expected but not received");
        }

        // copyTrainedDataFiles();
        // tessBaseAPI = new TessBaseAPI();
        // String dataPath = getFilesDir() + "/tesseract";
        // Initialize Tesseract with the path and the language
        // tessBaseAPI.init(dataPath, "eng");

        User user = helper.getUser(null, userId);
        String ToaccountToInject = user.getFirstName() + " " + user.getName();

        EditText nCompteEditText = view.findViewById(R.id.nCompte);
        nCompteEditText.setText(nCompte);
        nCompteEditText.setEnabled(false);

        // Initialize EditTexts
        EditText checkIdEditText = view.findViewById(R.id.checkId);
        EditText amountEditText = view.findViewById(R.id.amount);
        EditText lettresAmountEditText = view.findViewById(R.id.lettresAmount);
        EditText toaccountEditText = view.findViewById(R.id.toaccount);
        EditText sourceRIBEditText = view.findViewById(R.id.sourceRIB);
        EditText sourceHolderEditText = view.findViewById(R.id.sourceHolder);
        EditText provenceEditText = view.findViewById(R.id.provence);
        EditText dateEditText = view.findViewById(R.id.date);
        EditText memoEditText = view.findViewById(R.id.memo);

        // Initialize ImageViews
        checkFront = view.findViewById(R.id.checkFront);
        checkBack = view.findViewById(R.id.checkBack);


        // Set click listeners for image capture
        checkFront.setOnClickListener(v -> requestCameraPermissionAndOpenCamera(checkFront));
        checkBack.setOnClickListener(v -> requestCameraPermissionAndOpenCamera(checkBack));

        // Initialize Confirm button
        Button confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(view1 -> {
            String checkId = checkIdEditText.getText().toString();
            String amountStr = amountEditText.getText().toString();
            String lettresAmount = lettresAmountEditText.getText().toString();
            String toaccount = toaccountEditText.getText().toString();
            String sourceRIB = sourceRIBEditText.getText().toString();
            String sourceHolder = sourceHolderEditText.getText().toString();
            String provence = provenceEditText.getText().toString();
            String dateStr = dateEditText.getText().toString();
            String nAccount = nCompteEditText.getText().toString();
            String memo = memoEditText.getText().toString();

            // Compare drawables with default image to ensure that the check is captured
            Drawable checkFront_drawable = checkFront.getDrawable();
            Drawable checkBack_drawable = checkBack.getDrawable();

            Drawable ressource_drawable = ContextCompat.getDrawable(requireContext(), R.drawable.check_capture);

            boolean result1 = false; boolean result2 = false;
            if(checkFront_drawable instanceof BitmapDrawable && ressource_drawable instanceof BitmapDrawable){
                Bitmap checkFront_bitmap = ((BitmapDrawable) checkFront_drawable).getBitmap();
                Bitmap checkBack_bitmap = ((BitmapDrawable) checkBack_drawable).getBitmap();
                Bitmap ressource_bitmap = ((BitmapDrawable) ressource_drawable).getBitmap();

                result1 = checkFront_bitmap.sameAs(ressource_bitmap);
                result2 = checkBack_bitmap.sameAs(ressource_bitmap);
            }

            // Validate required fields
            if (checkId.isEmpty() || amountStr.isEmpty() || lettresAmount.isEmpty() || sourceRIB.isEmpty() || sourceHolder.isEmpty() || result1 || result2){
                Toast.makeText(getContext(), "Make sure that all required fields are filled", Toast.LENGTH_SHORT).show();
                return;
            }
            // Apply OCR : Make sure that the form information matches with the OCR detection
            boolean result = validateCheckInformation(extractedText);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);

            ImageView imageView = dialogView.findViewById(R.id.dialog_image);
            TextView messageTextView = dialogView.findViewById(R.id.dialog_message);

            if (result) {
                imageView.setImageResource(R.drawable.ic_non_fraud);
                messageTextView.setText("Congratulations!\n Your transaction information matches the OCR detection results");

                builder.setTitle("OCR RESULT")
                        .setView(dialogView)
                        .setPositiveButton("CONFIRM", (dialog, which) -> {
                            boolean exists = helper.isCheckTransactionExists(Integer.parseInt(checkId));
                            if (exists) {
                                Toast.makeText(requireContext(), "This check has already been deposited. Please ensure that a check transaction is not used more than once", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                CheckTransaction checkTransaction = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    checkTransaction = new CheckTransaction(Integer.parseInt(checkId), Float.parseFloat(amountStr), lettresAmount, toaccount, sourceRIB, sourceHolder, provence, !dateStr.isEmpty() ? LocalDate.parse(dateStr) : LocalDate.parse(""), nAccount, memo);
                                }
                                boolean approvable = helper.isCheckTransactionApprovable(checkTransaction); // The source account has the amount for the transaction
                                if (approvable) {
                                    // Funds available
                                    // 1- Add the transaction
                                    long insertResult = helper.addCheckTransaction(checkTransaction);
                                    // 2- Redirect the user to the check transaction history
                                    if(insertResult != -1){
                                        Toast.makeText(requireContext(), "Good job!", Toast.LENGTH_LONG).show();
                                        // redirect to transaction history
                                        Intent intent = new Intent(requireContext(), CheckDepositHistoryActivity.class);
                                        intent.putExtra("userId", userId);
                                        intent.putExtra("nCompte", nCompte);
                                        intent.putExtra("selectedCard", selectedCard);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                    else {
                                        Toast.makeText(requireContext(), "An error occurred while adding the check transaction. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "The source account does not have sufficient funds for the transaction.", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", (dialog, which) -> resetForm());
            } else {
                imageView.setImageResource(R.drawable.ic_non_fraud);
                messageTextView.setText("OCR Results Discrepancy!\nInformation Found Differs from Submitted Data");

                builder.setTitle("OCR RESULT")
                        .setView(dialogView)
                        .setPositiveButton("RETRY", (dialog, which) -> dialog.dismiss())
                        .setNegativeButton("CANCEL", (dialog, which) -> resetForm());
            }

            builder.create().show();
        });

        return view;
    }

    private void requestCameraPermissionAndOpenCamera(ImageView imageView) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera(imageView);
        }
    }

    private void openCamera(ImageView imageView) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            imageViewToUpdate = imageView; // Set the ImageView to update
            startActivityForResult(intent, CAMERA_CAPTURE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check which ImageView was clicked
                if (imageViewToUpdate != null) {
                    openCamera(imageViewToUpdate);
                }
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to use this feature", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                if (imageBitmap != null && imageViewToUpdate != null) {
                    imageViewToUpdate.setImageBitmap(imageBitmap);
                }

                // Extract text from the image
                extractedText = "Hello"; // Replace with actual text extraction logic
            }
        }
    }

    private boolean validateCheckInformation(String ocrText) {
        // Add logic to validate the check information against OCR results
        return true;
    }

    public void resetForm() {
        if (getView() == null) return;

        // Find and clear the form fields
        EditText checkIdEditText = getView().findViewById(R.id.checkId);
        EditText amountEditText = getView().findViewById(R.id.amount);
        EditText lettresAmountEditText = getView().findViewById(R.id.lettresAmount);
        EditText toaccountEditText = getView().findViewById(R.id.toaccount);
        EditText sourceRIBEditText = getView().findViewById(R.id.sourceRIB);
        EditText sourceHolderEditText = getView().findViewById(R.id.sourceHolder);
        EditText provenceEditText = getView().findViewById(R.id.provence);
        EditText dateEditText = getView().findViewById(R.id.date);
        EditText memoEditText = getView().findViewById(R.id.memo);

        checkIdEditText.setText("");
        amountEditText.setText("");
        lettresAmountEditText.setText("");
        toaccountEditText.setText("");
        sourceRIBEditText.setText("");
        sourceHolderEditText.setText("");
        provenceEditText.setText("");
        dateEditText.setText("");
        memoEditText.setText("");

        // Reset image views to default image
        ImageView checkFrontImageView = getView().findViewById(R.id.checkFront);
        ImageView checkBackImageView = getView().findViewById(R.id.checkBack);
        checkFrontImageView.setImageResource(R.drawable.check_capture);
        checkBackImageView.setImageResource(R.drawable.check_capture);
    }
}

/*
    private String extractTextFromImage(Bitmap bitmap) {
        Bitmap processedBitmap = preprocessImage(bitmap);
        tessBaseAPI.setImage(processedBitmap);
        String extractedText = tessBaseAPI.getUTF8Text();
        Log.d("OCR", "Extracted Text: " + extractedText);
        return extractedText;
    }

    private Bitmap preprocessImage(Bitmap bitmap) {
        Bitmap grayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int pixel = bitmap.getPixel(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int gray = (red + green + blue) / 3;
                grayBitmap.setPixel(x, y, (0xff << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }
        return grayBitmap;
    }

    private void copyTrainedDataFiles() {
        AssetManager assetManager = requireContext().getAssets();
        String[] files;
        String dataPath = requireContext().getFilesDir() + "/tesseract/tessdata/";

        try {
            File dir = new File(dataPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            files = assetManager.list("tessdata");
            if (files != null) {
                for (String filename : files) {
                    File outFile = new File(dataPath, filename);
                    if (!outFile.exists()) {
                        try (InputStream in = assetManager.open("tessdata/" + filename);
                             OutputStream out = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        if (tessBaseAPI != null) {
            tessBaseAPI.end();
        }
        super.onDestroyView();
    }*/

