package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Budget;
import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Wallet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BudgetService {

  public void setBudget(Wallet wallet, String category, Money limit) {
    if (wallet == null) throw new IllegalArgumentException("Wallet не может быть null");

    String name = normalize(category);
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Категория не может быть пустой");
    }

    // Если у тебя есть категории в кошельке, делаем проверку.
    // Если категорий пока нет — просто закомментируй этот блок.
    if (!wallet.hasCategory(name)) {
      throw new IllegalArgumentException("Категория не найдена: " + name);
    }

    wallet.setBudget(name, limit);
  }

  public List<Budget> listBudgets(Wallet wallet) {
    if (wallet == null) throw new IllegalArgumentException("Wallet не может быть null");
    List<Budget> list = new ArrayList<>(wallet.getBudgets().values());
    list.sort(Comparator.comparing(Budget::category));
    return list;
  }

  private String normalize(String s) {
    if (s == null) return "";
    return s.trim().toLowerCase();
  }
}
