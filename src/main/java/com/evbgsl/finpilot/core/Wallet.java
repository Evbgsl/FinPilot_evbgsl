package com.evbgsl.finpilot.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    Money result = Money.zero();

    for (var t : operations) {
      if (t.type().isIncomeLike()) {
        result = result.add(t.amount());
      } else if (t.type().isExpenseLike()) {
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

  public void addTransferOut(String toLogin, Money amount, String note) {
    operations.add(
        Transaction.transferOut(
            "transfer", amount, "to=" + normalize(toLogin) + " " + safeNote(note)));
  }

  public void addTransferIn(String fromLogin, Money amount, String note) {
    operations.add(
        Transaction.transferIn(
            "transfer", amount, "from=" + normalize(fromLogin) + " " + safeNote(note)));
  }

  private String safeNote(String note) {
    return note == null ? "" : note.trim();
  }

  private String normalize(String s) {
    return s == null ? "" : s.trim().toLowerCase();
  }
}
