package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Budget;
import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Transaction;
import com.evbgsl.finpilot.core.TransactionType;
import com.evbgsl.finpilot.core.Wallet;
import java.time.LocalDate;
import java.util.*;

public class ReportService {

    public ReportSummary summary(Wallet wallet) {
        return summary(wallet, null);
    }

    public ReportSummary summary(Wallet wallet, ReportFilter filter) {
        Money income = sumIncomeLike(wallet, filter);
        Money expense = sumExpenseLike(wallet, filter);
        Money balance = income.subtract(expense);
        return new ReportSummary(income, expense, balance);
    }

    private Money sumIncomeLike(Wallet wallet, ReportFilter filter) {
        Money sum = Money.zero();
        for (Transaction t : filteredOperations(wallet, filter)) {
            if (t.type().isIncomeLike()) {
                sum = sum.add(t.amount());
            }
        }
        return sum;
    }

    private Money sumExpenseLike(Wallet wallet, ReportFilter filter) {
        Money sum = Money.zero();
        for (Transaction t : filteredOperations(wallet, filter)) {
            if (t.type().isExpenseLike()) {
                sum = sum.add(t.amount());
            }
        }
        return sum;
    }





    public List<CategoryRow> categories(Wallet wallet) {
        Map<String, Totals> map = new TreeMap<>();

        for (Transaction t : wallet.getOperations()) {
            String cat = normalize(t.category());
            Totals totals = map.computeIfAbsent(cat, k -> new Totals());

            if (t.type().isIncomeLike()) {
                totals.income = totals.income.add(t.amount());
            } else if (t.type().isExpenseLike()) {
                totals.expense = totals.expense.add(t.amount());
            }

        }

        List<CategoryRow> rows = new ArrayList<>();
        for (var e : map.entrySet()) {
            rows.add(new CategoryRow(e.getKey(), e.getValue().income, e.getValue().expense));
        }
        return rows;
    }

    public List<CategoryRow> categories(Wallet wallet, ReportFilter filter) {
        Map<String, Totals> map = new TreeMap<>();

        for (Transaction t : filteredOperations(wallet, filter)) {
            String cat = normalize(t.category());
            Totals totals = map.computeIfAbsent(cat, k -> new Totals());

            if (t.type().isIncomeLike()) {
                totals.income = totals.income.add(t.amount());
            } else if (t.type().isExpenseLike()) {
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


    public List<BudgetRow> budgets(Wallet wallet, ReportFilter filter) {
        Map<String, Budget> budgets = wallet.getBudgets();
        if (budgets.isEmpty()) return List.of();

        Map<String, Money> spentByCategory = new HashMap<>();

        for (Transaction t : filteredOperations(wallet, filter)) {
            if (t.type() == TransactionType.EXPENSE) {
                String cat = normalize(t.category());
                Money current = spentByCategory.getOrDefault(cat, Money.zero());
                spentByCategory.put(cat, current.add(t.amount()));
            }
        }

        List<BudgetRow> rows = new ArrayList<>();
        for (Budget b : budgets.values()) {
            String cat = normalize(b.category());

            // если фильтр --only задан и эта категория не входит, то пропускаем строку бюджета
            if (filter != null && filter.hasOnlyCategories() && !filter.onlyCategories().contains(cat)) {
                continue;
            }

            Money spent = spentByCategory.getOrDefault(cat, Money.zero());
            Money remaining = b.limit().subtract(spent);
            rows.add(new BudgetRow(cat, b.limit(), spent, remaining));
        }

        rows.sort(Comparator.comparing(BudgetRow::category));
        return rows;
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

    private List<Transaction> filteredOperations(Wallet wallet, ReportFilter filter) {
        if (filter == null) return wallet.getOperations();

        List<Transaction> out = new ArrayList<>();
        for (Transaction t : wallet.getOperations()) {
            if (!passesDate(t, filter)) continue;
            if (!passesOnly(t, filter)) continue;
            out.add(t);
        }
        return out;
    }

    private boolean passesDate(Transaction t, ReportFilter filter) {
        if (filter.from() == null && filter.to() == null) return true;

        LocalDate d = t.dateTime().toLocalDate();
        if (filter.from() != null && d.isBefore(filter.from())) return false;
        if (filter.to() != null && d.isAfter(filter.to())) return false;
        return true;
    }

    private boolean passesOnly(Transaction t, ReportFilter filter) {
        if (!filter.hasOnlyCategories()) return true;
        return filter.onlyCategories().contains(normalize(t.category()));
    }

    private Money sumByType(Wallet wallet, TransactionType type, ReportFilter filter) {
        Money sum = Money.zero();
        for (Transaction t : filteredOperations(wallet, filter)) {
            if (t.type() == type) {
                sum = sum.add(t.amount());
            }
        }
        return sum;
    }

}
