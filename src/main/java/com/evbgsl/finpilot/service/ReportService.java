package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Budget;
import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Transaction;
import com.evbgsl.finpilot.core.TransactionType;
import com.evbgsl.finpilot.core.Wallet;

import java.math.BigDecimal;
import java.util.*;

public class ReportService {

    public ReportSummary summary(Wallet wallet) {
        Money income = sumByType(wallet, TransactionType.INCOME);
        Money expense = sumByType(wallet, TransactionType.EXPENSE);
        Money balance = income.subtract(expense);
        return new ReportSummary(income, expense, balance);
    }

    public List<CategoryRow> categories(Wallet wallet) {
        Map<String, Totals> map = new TreeMap<>();

        for (Transaction t : wallet.getOperations()) {
            String cat = normalize(t.category());
            Totals totals = map.computeIfAbsent(cat, k -> new Totals());

            if (t.type() == TransactionType.INCOME) {
                totals.income = totals.income.add(t.amount());
            } else if (t.type() == TransactionType.EXPENSE) {
                totals.expense = totals.expense.add(t.amount());
            }
        }

        List<CategoryRow> rows = new ArrayList<>();
        for (var e : map.entrySet()) {
            rows.add(new CategoryRow(e.getKey(), e.getValue().income, e.getValue().expense));
        }
        return rows;
    }

    public List<BudgetRow> budgets(Wallet wallet) {
        // предполагается, что в Wallet есть getBudgets(): Map<String, Budget>
        Map<String, Budget> budgets = wallet.getBudgets();
        if (budgets.isEmpty()) return List.of();

        // посчитаем расходы по категориям
        Map<String, Money> spentByCategory = new HashMap<>();
        for (Transaction t : wallet.getOperations()) {
            if (t.type() == TransactionType.EXPENSE) {
                String cat = normalize(t.category());
                Money current = spentByCategory.getOrDefault(cat, Money.zero());
                spentByCategory.put(cat, current.add(t.amount()));
            }
        }

        List<BudgetRow> rows = new ArrayList<>();
        for (Budget b : budgets.values()) {
            String cat = normalize(b.category());
            Money spent = spentByCategory.getOrDefault(cat, Money.zero());
            Money remaining = b.limit().subtract(spent);
            rows.add(new BudgetRow(cat, b.limit(), spent, remaining));
        }

        rows.sort(Comparator.comparing(BudgetRow::category));
        return rows;
    }

    private Money sumByType(Wallet wallet, TransactionType type) {
        Money sum = Money.zero();
        for (Transaction t : wallet.getOperations()) {
            if (t.type() == type) {
                sum = sum.add(t.amount());
            }
        }
        return sum;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }

    private static class Totals {
        Money income = Money.zero();
        Money expense = Money.zero();
    }

    // DTO-шки отчётов
    public record ReportSummary(Money totalIncome, Money totalExpense, Money balance) {}
    public record CategoryRow(String category, Money income, Money expense) {}
    public record BudgetRow(String category, Money limit, Money spent, Money remaining) {}
}
