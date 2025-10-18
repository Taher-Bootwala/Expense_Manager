// ViewExpensesActivity.java - View All Expenses
package com.example.myapplication.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ExpenseAdapter;
import com.example.myapplication.modules.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ViewExpensesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTotalExpenses, tvFilterDate, tvNoExpenses;
    private Button btnFilterByDate, btnShowAll;
    private ProgressBar progressBar;

    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private List<Expense> allExpenses;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private Calendar filterDate;
    private boolean isFiltered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not authenticated, redirect to LoginActivity
            startActivity(new Intent(ViewExpensesActivity.this, LoginActivity.class));
            finish(); // Finish this activity to prevent it from appearing in the back stack
            return; // Stop further execution of this method
        }
        userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvFilterDate = findViewById(R.id.tvFilterDate);
        tvNoExpenses = findViewById(R.id.tvNoExpenses);
        btnFilterByDate = findViewById(R.id.btnFilterByDate);
        btnShowAll = findViewById(R.id.btnShowAll);
        progressBar = findViewById(R.id.progressBar);

        // Set up RecyclerView
        expenseList = new ArrayList<>();
        allExpenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseAdapter);

        filterDate = Calendar.getInstance();

        // Load all expenses
        loadExpenses();

        // Filter by date button
        btnFilterByDate.setOnClickListener(v -> showDatePicker());

        // Show all button
        btnShowAll.setOnClickListener(v -> {
            isFiltered = false;
            tvFilterDate.setVisibility(View.GONE);
            expenseList.clear();
            expenseList.addAll(allExpenses);
            expenseAdapter.notifyDataSetChanged();
            updateTotalExpenses();
            checkEmptyList();
        });
    }

    private void loadExpenses() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("expenses").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allExpenses.clear();

                        for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                            Expense expense = expenseSnapshot.getValue(Expense.class);
                            if (expense != null) {
                                allExpenses.add(expense);
                            }
                        }

                        // Sort by timestamp (newest first)
                        Collections.sort(allExpenses, (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));

                        if (!isFiltered) {
                            expenseList.clear();
                            expenseList.addAll(allExpenses);
                        } else {
                            filterExpensesByDate();
                        }

                        expenseAdapter.notifyDataSetChanged();
                        updateTotalExpenses();
                        checkEmptyList();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ViewExpensesActivity.this,
                                "Failed to load expenses",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    filterDate.set(year, month, dayOfMonth);
                    isFiltered = true;
                    filterExpensesByDate();
                },
                filterDate.get(Calendar.YEAR),
                filterDate.get(Calendar.MONTH),
                filterDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void filterExpensesByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String selectedDate = dateFormat.format(filterDate.getTime());

        expenseList.clear();
        for (Expense expense : allExpenses) {
            if (expense.getDate().equals(selectedDate)) {
                expenseList.add(expense);
            }
        }

        expenseAdapter.notifyDataSetChanged();
        tvFilterDate.setText("Showing expenses for: " + selectedDate);
        tvFilterDate.setVisibility(View.VISIBLE);
        updateTotalExpenses();
        checkEmptyList();
    }

    private void updateTotalExpenses() {
        double total = 0.0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", total));
    }

    private void checkEmptyList() {
        if (expenseList.isEmpty()) {
            tvNoExpenses.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoExpenses.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
