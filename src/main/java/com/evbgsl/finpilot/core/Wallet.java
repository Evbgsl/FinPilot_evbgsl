package com.evbgsl.finpilot.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class Wallet {
    private final String ownerLogin;
    private final List<Transaction> operations;

    private final Set<Category> categories;
    private final Map<String, Budget> budgets = new HashMap<>();

    public Wallet(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.trim().isEmpty()) {
            throw new IllegalArgumentException("ownerLogin must not be empty");
        }
        this.ownerLogin = ownerLogin;
        this.operations = new ArrayList<>();
        this.categories = new HashSet<>();
    }

    public void addIncome(String category, Money amount, String note) {
        var t = Transaction.income(category, amount, note);
        operations.add(t);
    }

    public void addExpense(String category, Money amount, String note) {
        var t = Transaction.expense(category, amount, note);
        operations.add(t);
    }

    public Money getBalance() {
        Money result = Money.zero(); // если у тебя есть zero()

        for (Transaction t : operations) {
            if (t.type() == TransactionType.INCOME) {
                result = result.add(t.amount());
            } else if (t.type() == TransactionType.EXPENSE) {
                result = result.subtract(t.amount());
            }
        }
        return result;
    }


    public List<Transaction> getOperations() {
        return List.copyOf(operations);
    }

    public void addOperationRaw(Transaction t) {
        if (t == null) return;
        operations.add(t);
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void addCategory(String name) {
        Category category = new Category(name); // тут внутри trim/lower/валидация
        boolean added = categories.add(category);
        if (!added) {
            throw new IllegalArgumentException("Категория уже существует: " + category.name());
        }
    }

    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>(categories);
        list.sort(Comparator.comparing(Category::name));
        return list;
    }

    public boolean hasCategory(String name) {
        String key = normalize(name);
        return categories.stream().anyMatch(c -> c.name().equals(key));
    }


    public void setBudget(String category, Money limit) {
        String key = normalize(category);
        budgets.put(key, new Budget(key, limit));
    }

    public Map<String, Budget> getBudgets() {
        return Map.copyOf(budgets);
    }

    public Optional<Budget> getBudget(String category) {
        String key = normalize(category);
        return Optional.ofNullable(budgets.get(key));
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }






}
