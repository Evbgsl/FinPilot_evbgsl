package com.evbgsl.finpilot.core;

import java.util.ArrayList;
import java.util.List;

public class Wallet {
    private final String ownerLogin;
    private final List<Transaction> operations;
    private Money balance;

    public Wallet(String ownerLogin) {
        this.ownerLogin = ownerLogin;
        this.operations = new ArrayList<>();
        this.balance = Money.of("0");
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
}
