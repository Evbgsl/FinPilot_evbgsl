package com.evbgsl.finpilot.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Wallet {
    private final String ownerLogin;
    private final List<Transaction> operations;
    private Money balance;

    private final Set<Category> categories;

    public Wallet(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.trim().isEmpty()) {
            throw new IllegalArgumentException("ownerLogin must not be empty");
        }
        this.ownerLogin = ownerLogin;
        this.operations = new ArrayList<>();
        this.balance = Money.of("0");
        this.categories = new HashSet<>();
    }

    public void addIncome(String category, Money amount, String note) {
        var t = Transaction.income(category, amount, note);
        operations.add(t);
        balance = balance.add(t.amount());
    }

    public void addExpense(String category, Money amount, String note) {
        var t = Transaction.expense(category, amount, note);
        operations.add(t);
        balance = balance.subtract(t.amount());
    }

    public Money getBalance() {
        return balance;
    }

    public List<Transaction> getOperations() {
        return operations;
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
}
