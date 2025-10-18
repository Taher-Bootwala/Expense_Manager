// AddExpenseActivity.java - Add New Expense
package com.example.myapplication.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.modules.Expense;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDate, etNote;
    private Spinner spinnerCategory;
    private Button btnSaveExpense, btnSelectDate;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not authenticated, redirect to LoginActivity
            startActivity(new Intent(AddExpenseActivity.this, LoginActivity.class));
            finish(); // Finish this activity to prevent it from appearing in the back stack
            return; // Stop further execution of this method
        }
        userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        progressBar = findViewById(R.id.progressBar);

        // Set up category spinner
        String[] categories = {"Food", "Transport", "Shopping", "Bills", "Entertainment",
                "Healthcare", "Education", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        // Set today's date as default
        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        // Date picker button
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        // Save expense button
        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        etDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveExpense.setEnabled(false);

        String expenseId = mDatabase.child("expenses").child(userId).push().getKey();
        if (expenseId == null) {
            Toast.makeText(this, "Failed to generate expense ID", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnSaveExpense.setEnabled(true);
            return;
        }

        Expense expense = new Expense(expenseId, userId, amount, category, date, note,
                System.currentTimeMillis());

        mDatabase.child("expenses").child(userId).child(expenseId)
                .setValue(expense)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveExpense.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(AddExpenseActivity.this,
                                "Expense added successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddExpenseActivity.this,
                                "Failed to add expense",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}