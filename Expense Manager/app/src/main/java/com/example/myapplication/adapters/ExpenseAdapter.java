// ExpenseAdapter.java - RecyclerView Adapter for Expenses
package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.modules.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> expenseList;

    public ExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvCategory.setText(expense.getCategory());
        holder.tvAmount.setText("₹" + String.format("%.2f", expense.getAmount()));
        holder.tvDate.setText(expense.getDate());

        if (expense.getNote() != null && !expense.getNote().isEmpty()) {
            holder.tvNote.setText(expense.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // Set category icon/color based on category
        String categoryIcon = getCategoryIcon(expense.getCategory());
        holder.tvCategoryIcon.setText(categoryIcon);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    private String getCategoryIcon(String category) {
        switch (category) {
            case "Food":
                return "🍔";
            case "Transport":
                return "🚗";
            case "Shopping":
                return "🛒";
            case "Bills":
                return "📄";
            case "Entertainment":
                return "🎬";
            case "Healthcare":
                return "🏥";
            case "Education":
                return "📚";
            default:
                return "💰";
        }
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvCategory, tvAmount, tvDate, tvNote;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }
}