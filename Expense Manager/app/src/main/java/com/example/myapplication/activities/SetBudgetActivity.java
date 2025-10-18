// SetBudgetActivity.java - Set/Update Monthly Budget
package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class SetBudgetActivity extends AppCompatActivity {

    private TextView tvCurrentBudget;
    private EditText etNewBudget;
    private Button btnSaveBudget;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private double currentBudget = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not authenticated, redirect to LoginActivity
            startActivity(new Intent(SetBudgetActivity.this, LoginActivity.class));
            finish(); // Finish this activity to prevent it from appearing in the back stack
            return; // Stop further execution of this method
        }
        userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget);
        etNewBudget = findViewById(R.id.etNewBudget);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        progressBar = findViewById(R.id.progressBar);

        // Load current budget
        loadCurrentBudget();

        // Save budget button
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void loadCurrentBudget() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("users").child(userId).child("monthlyBudget")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Double budget = snapshot.getValue(Double.class);
                            currentBudget = budget != null ? budget : 0.0;
                            tvCurrentBudget.setText(String.format(Locale.getDefault(), "Current Budget: ₹%.2f", currentBudget));
                        } else {
                            tvCurrentBudget.setText("Current Budget: Not Set");
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SetBudgetActivity.this,
                                "Failed to load budget",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveBudget() {
        String budgetStr = etNewBudget.getText().toString().trim();

        if (TextUtils.isEmpty(budgetStr)) {
            etNewBudget.setError("Budget amount is required");
            etNewBudget.requestFocus();
            return;
        }

        double newBudget;
        try {
            newBudget = Double.parseDouble(budgetStr);
            if (newBudget <= 0) {
                etNewBudget.setError("Budget must be greater than 0");
                etNewBudget.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etNewBudget.setError("Invalid amount");
            etNewBudget.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveBudget.setEnabled(false);

        mDatabase.child("users").child(userId).child("monthlyBudget")
                .setValue(newBudget)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveBudget.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(SetBudgetActivity.this,
                                "Budget updated successfully!",
                                Toast.LENGTH_SHORT).show();
                        etNewBudget.setText("");
                    } else {
                        Toast.makeText(SetBudgetActivity.this,
                                "Failed to update budget",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
