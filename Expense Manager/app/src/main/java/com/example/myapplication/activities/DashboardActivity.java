// DashboardActivity.java - Main Dashboard
package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvMonthlyBudget, tvTotalSpent, tvRemaining, tvBudgetStatus;
    private ProgressBar budgetProgressBar, loadingProgress;
    private Button btnAddExpense, btnViewExpenses, btnSetBudget, btnProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private double monthlyBudget = 0.0;
    private double totalSpent = 0.0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not authenticated, redirect to LoginActivity
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish(); // Finish this activity to prevent it from appearing in the back stack
            return; // Stop further execution of this method
        }
        userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
        budgetProgressBar = findViewById(R.id.budgetProgressBar);
        loadingProgress = findViewById(R.id.loadingProgress);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnViewExpenses = findViewById(R.id.btnViewExpenses);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnProfile = findViewById(R.id.btnProfile);

        // Load user data
        loadUserData();
        loadMonthlyExpenses();

        // Button click listeners
        btnAddExpense.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddExpenseActivity.class)));
        btnViewExpenses.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ViewExpensesActivity.class)));
        btnSetBudget.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SetBudgetActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the user is still authenticated
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
            loadMonthlyExpenses();
        }
    }

    private void loadUserData() {
        loadingProgress.setVisibility(View.VISIBLE);

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    Double budget = snapshot.child("monthlyBudget").getValue(Double.class);

                    tvWelcome.setText("Welcome, " + (fullName != null ? fullName : "User") + "!");
                    monthlyBudget = budget != null ? budget : 0.0;
                    tvMonthlyBudget.setText(String.format(Locale.getDefault(), "₹%.2f", monthlyBudget));

                    updateBudgetDisplay();
                }
                loadingProgress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(DashboardActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMonthlyExpenses() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());

        mDatabase.child("expenses").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalSpent = 0.0;
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    String expenseDate = expenseSnapshot.child("date").getValue(String.class);
                    Double amount = expenseSnapshot.child("amount").getValue(Double.class);

                    if (expenseDate != null && amount != null) {
                        try {
                            // Assuming date format is dd-MM-yyyy
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Calendar expenseCalendar = Calendar.getInstance();
                            expenseCalendar.setTime(Objects.requireNonNull(sdf.parse(expenseDate)));

                            if (monthFormat.format(expenseCalendar.getTime()).equals(currentMonth)) {
                                totalSpent += amount;
                            }
                        } catch (Exception e) {
                            // Handle parsing exception
                        }
                    }
                }
                tvTotalSpent.setText(String.format(Locale.getDefault(), "₹%.2f", totalSpent));
                updateBudgetDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBudgetDisplay() {
        double remaining = monthlyBudget - totalSpent;
        tvRemaining.setText(String.format(Locale.getDefault(), "₹%.2f", remaining));

        if (monthlyBudget > 0) {
            int progress = (int) ((totalSpent / monthlyBudget) * 100);
            budgetProgressBar.setProgress(Math.min(progress, 100));

            if (progress >= 100) {
                tvBudgetStatus.setText("⚠️ Budget Exceeded!");
                tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (progress >= 75) {
                tvBudgetStatus.setText("⚠️ 75% Budget Used");
                tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                tvBudgetStatus.setText("✓ Within Budget");
                tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        } else {
            budgetProgressBar.setProgress(0);
            tvBudgetStatus.setText("Set a monthly budget to track spending");
            tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }
}
