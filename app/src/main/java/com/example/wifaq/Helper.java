package com.example.wifaq;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Helper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.db";
    private static final String DATABASE_PATH = "/data/data/com.example.wifaq/databases/";
    private final Context context;
    private SQLiteDatabase database;

    public Helper(Context context) {
        super(context, DATABASE_NAME, null, 3);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to create tables, as they are already created in the .db file
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TransactionsHistory");
        db.execSQL("DROP TABLE IF EXISTS CreditCard");
        db.execSQL("DROP TABLE IF EXISTS Account");
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS BankAccount");
        db.execSQL("DROP TABLE IF EXISTS CheckTransaction");
        onCreate(db);
    }

    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        if (!dbExist) {
            this.getReadableDatabase();
            copyDatabase();
        }
    }

    private boolean checkDatabase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDatabase() throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream outputStream = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public void openDatabase() throws IOException {
        String myPath = DATABASE_PATH + DATABASE_NAME;
        database = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    public boolean checkIfAccountExists(Account account) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM Account WHERE username = ? AND password = ?",
                    new String[]{account.getUsername(), account.getPassword()});
            exists = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("Helper", "Error checking account existence", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return exists;
    }

    public Account getAccount(String usernamee, String passwordd){
        SQLiteDatabase db = this.getReadableDatabase();
        Account account = null;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM Account " +
                    "WHERE Account.username = ? and Account.password = ? ", new String[]{usernamee, passwordd});

            if (cursor != null && cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                long idUser = cursor.getLong(cursor.getColumnIndexOrThrow("idUser"));
                String nCompte = cursor.getString(cursor.getColumnIndexOrThrow("nCompte"));


                account = new Account(username, password, idUser, nCompte);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return account;
    }
    public User getUser(String username, long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = null;

        try {
            if(username != null) {
                cursor = db.rawQuery("SELECT User.* FROM Account " +
                        "INNER JOIN User ON Account.idUser = User.cin " +
                        "WHERE Account.username = ?", new String[]{username});
            }else{
                cursor = db.rawQuery("SELECT * FROM User " +
                        "WHERE User.cin = ?", new String[]{String.valueOf(userId)});
            }
            if (cursor != null && cursor.moveToFirst()) {
                // Extract data from the cursor
                long cin = cursor.getLong(cursor.getColumnIndexOrThrow("cin"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow("firstName"));
                LocalDate birth = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    birth = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("birth")));
                }
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                user = new User(cin, name, firstName, birth, phone, email, address);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return user;
    }

    public BankAccount getBankAccount(String username, String CompteId){
        SQLiteDatabase db = this.getReadableDatabase();
        BankAccount bankAccount = null;
        Cursor cursor = null;

        try {
            if(username != null){
                cursor = db.rawQuery("SELECT BankAccount.* FROM Account " +
                        "INNER JOIN BankAccount ON Account.nCompte = BankAccount.nCompte " +
                        "WHERE Account.username = ?", new String[]{username});
            }else{
                cursor = db.rawQuery("SELECT * FROM BankAccount " +
                        "WHERE nCompte = ?", new String[]{CompteId});
            }
            if (cursor != null && cursor.moveToFirst()) {
                String nCompte = cursor.getString(cursor.getColumnIndexOrThrow("nCompte"));
                String RIB = cursor.getString(cursor.getColumnIndexOrThrow("RIB"));
                float balance = cursor.getFloat(cursor.getColumnIndexOrThrow("balance"));

                bankAccount = new BankAccount(nCompte, RIB, balance);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return bankAccount;
    }


    public List<Card> getCards(String nCompte) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Card> cards = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM CreditCard WHERE nCompte = ?",
                    new String[]{nCompte});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String cardNumber = cursor.getString(cursor.getColumnIndexOrThrow("cardNumber"));
                    String holder = cursor.getString(cursor.getColumnIndexOrThrow("holder"));
                    String expirationDateString = cursor.getString(cursor.getColumnIndexOrThrow("expirationDate"));
                    LocalDate expirationDate = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        expirationDate = LocalDate.parse(expirationDateString);
                    }
                    float balance = cursor.getFloat(cursor.getColumnIndexOrThrow("balance"));
                    String categoryString = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    Card.Category category = Card.Category.valueOf(categoryString);

                    String functionnalityString = cursor.getString(cursor.getColumnIndexOrThrow("functionnality"));
                    Card.Functionnality functionnality = Card.Functionnality.valueOf(functionnalityString);

                    Card card = new Card(cardNumber, holder, expirationDate, balance, category, functionnality);
                    cards.add(card);

                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            Log.e("Helper", "Error fetching cards", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return cards;
    }

    public List<Transaction> getTransactionsHistory(Card card) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Transaction> transactions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM TransactionsHistory WHERE sourceCardNumber = ? OR destCardNumber = ?",
                    new String[]{card.getCardNumber(), card.getCardNumber()});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String sourceCardNumber = cursor.getString(cursor.getColumnIndexOrThrow("sourceCardNumber"));
                    String destCardNumber = cursor.getString(cursor.getColumnIndexOrThrow("destCardNumber"));

                    // Parsing LocalDate
                    String transactionDateString = cursor.getString(cursor.getColumnIndexOrThrow("transactionDate"));
                    LocalDate transactionDate = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        transactionDate = LocalDate.parse(transactionDateString);
                    }

                    // Parsing LocalTime
                    String timeString = cursor.getString(cursor.getColumnIndexOrThrow("transactionTime"));
                    LocalTime transactionTime = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        transactionTime = LocalTime.parse(timeString);
                    }

                    float amount = cursor.getFloat(cursor.getColumnIndexOrThrow("amount"));
                    String typeString = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    Transaction.Type type = Transaction.Type.valueOf(typeString);

                    Transaction transaction = new Transaction(sourceCardNumber, destCardNumber, transactionDate, transactionTime, amount, type);
                    transactions.add(transaction);

                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            Log.e("Helper", "Error fetching transactions", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return transactions;
    }

    public Card getCreditCardById(String cardNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Card card = null;
        try {
            cursor = db.rawQuery("SELECT * FROM CreditCard WHERE cardNumber = ?", new String[]{cardNumber});
            if (cursor != null && cursor.moveToFirst()) {
                String cardNum = cursor.getString(cursor.getColumnIndexOrThrow("cardNumber"));
                String holder = cursor.getString(cursor.getColumnIndexOrThrow("holder"));
                String expirationDateString = cursor.getString(cursor.getColumnIndexOrThrow("expirationDate"));
                LocalDate expirationDate = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    expirationDate = LocalDate.parse(expirationDateString);
                }
                float balance = cursor.getFloat(cursor.getColumnIndexOrThrow("balance"));
                String categoryString = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                Card.Category category = Card.Category.valueOf(categoryString);

                String functionnalityString = cursor.getString(cursor.getColumnIndexOrThrow("functionnality"));
                Card.Functionnality functionnality = Card.Functionnality.valueOf(functionnalityString);

                card = new Card(cardNumber, holder, expirationDate, balance, category, functionnality);
            }
        } catch (IllegalArgumentException e) {
            Log.e("Helper", "Error fetching credit card by ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return card;
    }

    // Transfer transaction method
    public long applyTransferTransaction(Transaction transaction, BankAccount bankAccount) {
        long result = -1;

        try {
            // Update balances
            updateBalances(transaction, bankAccount);

            // Insert transaction record
            result = addTransaction(transaction);

        } catch (Exception e) {
            Log.e("TransactionError", "Failed to apply transfer transaction", e);
        }
        return result;
    }


    private void updateBalances(Transaction transaction, BankAccount bankAccount) {

        String sourceCardNumber = transaction.getSourceCardNumber();
        String destCardNumber = transaction.getDestCardNumber();

        Card sourceCard = this.getCreditCardById(sourceCardNumber);
        Card destCard = this.getCreditCardById(destCardNumber);

        // Perform the transfer operation
        sourceCard.setBalance(sourceCard.getBalance() - transaction.getAmount());
        destCard.setBalance(destCard.getBalance() + transaction.getAmount());
        bankAccount.setBalance(bankAccount.getBalance() - transaction.getAmount());

        updateCreditCardBalance(sourceCardNumber, sourceCard.getBalance());
        updateCreditCardBalance(destCardNumber, destCard.getBalance());
        updateBankAccountBalance(bankAccount.getNCompte(), bankAccount.getBalance());
    }

    private void updateCreditCardBalance(String cardNumber, double newBalance) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("balance", newBalance);

        // Update the database
        int rowsAffected = db.update("CreditCard", values, "cardNumber = ?", new String[]{cardNumber});
        if (rowsAffected == 0) {
            Log.e("DatabaseError", "Failed to update card balance for card number: " + cardNumber);
        }

        db.close();
    }

    private void updateBankAccountBalance(String compteId, float newBalance) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("balance", newBalance);

        // Update the database
        int rowsAffected = db.update("BankAccount", values, "nCompte = ?", new String[]{compteId});
        if (rowsAffected == 0) {
            Log.e("DatabaseError", "Failed to update bank account with number " + compteId);
        }
        db.close();
    }

    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("sourceCardNumber", transaction.getSourceCardNumber());
        values.put("destCardNumber", transaction.getDestCardNumber());
        values.put("transactionDate", String.valueOf(transaction.getTransactionDate()));
        values.put("transactionTime", String.valueOf(transaction.getTransactionTime()));
        values.put("amount", transaction.getAmount());
        values.put("type", transaction.getType().name());

        // Insert the data into the database
        long result = db.insert("TransactionsHistory", null, values);

        if (result == -1) {
            Log.e("DatabaseError", "Failed to insert transaction");
        } else {
            Log.d("DatabaseSuccess", "Transaction inserted successfully with reference ID: " + result);
        }
        db.close();
        return result;
    }

    // Check transactions methods
    public boolean isCheckTransactionExists(int checkId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM CheckTransaction WHERE checkId = ?", new String[]{String.valueOf(checkId)});
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }


    public boolean isCheckTransactionApprovable(CheckTransaction checkTransaction){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
           String sourceRIB = checkTransaction.getSourceRIB();
           String nCompte = checkTransaction.getBeneficiaryAccountNumber();

           BankAccount sourceAccount = this.getAccountByRIB(sourceRIB);
           BankAccount targetAccount = this.getBankAccount(null, nCompte);

           if(sourceAccount.getBalance() > checkTransaction.getAmount()){
               return true;
           }
           return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public BankAccount getAccountByRIB(String RIB) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        BankAccount account = null;
        try {
            cursor = db.rawQuery("SELECT * FROM BankAccount WHERE RIB = ?", new String[]{RIB});
            if (cursor.moveToFirst()) {
                int nCptIndex = cursor.getColumnIndex("nCompte");
                int ribIndex = cursor.getColumnIndex("RIB");
                int balanceIndex = cursor.getColumnIndex("balance");

                if (nCptIndex != -1 && ribIndex != -1 && balanceIndex != -1) {
                    String nCpt = cursor.getString(nCptIndex);
                    String accountRIB = cursor.getString(ribIndex);
                    float balance = cursor.getFloat(balanceIndex);

                    if (accountRIB != null && !accountRIB.isEmpty() && balance >= 0) {
                        account = new BankAccount(nCpt, accountRIB, balance);
                    }
                } else {
                    throw new IllegalStateException("One or more columns are missing from the database query result.");
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return account;
    }


    public long addCheckTransaction(CheckTransaction checkTransaction){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("checkId", checkTransaction.getCheckId());
        values.put("amount", checkTransaction.getAmount());
        values.put("amountLetters", checkTransaction.getAmountLetters());
        values.put("beneficiaryName", checkTransaction.getBeneficiaryName());
        values.put("sourceRIB", checkTransaction.getSourceRIB());
        values.put("sourceHolder", checkTransaction.getSourceHolder());
        values.put("provence", checkTransaction.getProvence());
        values.put("datePrevue", checkTransaction.getDate().toString());
        values.put("beneficiaryAccountNumber", checkTransaction.getBeneficiaryAccountNumber());
        values.put("memo", checkTransaction.getMemo());

        // Insert the data into the database
        long result = db.insert("CheckTransaction", null, values);

        if (result == -1) {
            Log.e("DatabaseError", "Failed to insert check transaction");
        } else {
            Log.d("DatabaseSuccess", "Check transaction inserted successfully with reference ID: " + result);
        }
        if (db != null) {
            db.close();
        }

        return result;
    }

    public List<CheckTransaction> getCheckTransactionsHistoty(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        List<CheckTransaction> transactions = new ArrayList<>();

        try{
            cursor = db.rawQuery("SELECT * FROM CheckTransaction", new String[]{});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int checkId = cursor.getInt(cursor.getColumnIndexOrThrow("checkId"));
                    float amount = cursor.getLong(cursor.getColumnIndexOrThrow("amount"));
                    String amountLetters = cursor.getString(cursor.getColumnIndexOrThrow("amountLetters"));
                    String beneficiaryName = cursor.getString(cursor.getColumnIndexOrThrow("beneficiaryName"));
                    String sourceRIB = cursor.getString(cursor.getColumnIndexOrThrow("sourceRIB"));
                    String sourceHolder = cursor.getString(cursor.getColumnIndexOrThrow("sourceHolder"));
                    String provence = cursor.getString(cursor.getColumnIndexOrThrow("provence"));

                    String dataStr = cursor.getString(cursor.getColumnIndexOrThrow("datePrevue"));
                    LocalDate date = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        date = LocalDate.parse(dataStr);
                    }

                    String beneficiaryAccountNumber = cursor.getString(cursor.getColumnIndexOrThrow("beneficiaryAccountNumber"));
                    String memo = cursor.getString(cursor.getColumnIndexOrThrow("memo"));

                    CheckTransaction transaction = new CheckTransaction(checkId,
                            amount,
                            amountLetters,
                            beneficiaryName,
                            sourceRIB,
                            sourceHolder,
                            provence,
                            date,
                            beneficiaryAccountNumber,
                            memo);
                    transactions.add(transaction);

                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            Log.e("Helper", "Error fetching transactions", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return transactions;

    }

}


