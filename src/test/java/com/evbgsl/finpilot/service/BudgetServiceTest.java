package com.evbgsl.finpilot.service;

import static org.junit.jupiter.api.Assertions.*;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Wallet;
import org.junit.jupiter.api.Test;

class BudgetServiceTest {

  @Test
  void setBudget_shouldStoreBudget_whenCategoryExists() {
    Wallet wallet = new Wallet("u1");
    wallet.addCategory("food");

    BudgetService bs = new BudgetService();
    bs.setBudget(wallet, "food", Money.of("1000"));

    var budgetOpt = wallet.getBudget("food");
    assertTrue(budgetOpt.isPresent());
    assertEquals(Money.of("1000"), budgetOpt.get().limit());
  }

  @Test
  void setBudget_shouldThrow_whenCategoryNotFound() {
    Wallet wallet = new Wallet("u1");
    // категорию НЕ добавляем

    BudgetService bs = new BudgetService();

    var ex =
        assertThrows(
            IllegalArgumentException.class, () -> bs.setBudget(wallet, "food", Money.of("1000")));

    assertTrue(ex.getMessage().contains("Категория не найдена"));
  }

  @Test
  void setBudget_shouldThrow_whenCategoryIsBlank() {
    Wallet wallet = new Wallet("u1");
    BudgetService bs = new BudgetService();

    var ex =
        assertThrows(
            IllegalArgumentException.class, () -> bs.setBudget(wallet, "   ", Money.of("1000")));

    assertTrue(ex.getMessage().contains("Категория не может быть пустой"));
  }

  @Test
  void listBudgets_shouldReturnSortedByCategory() {
    Wallet wallet = new Wallet("u1");
    wallet.addCategory("taxi");
    wallet.addCategory("food");

    BudgetService bs = new BudgetService();
    bs.setBudget(wallet, "taxi", Money.of("200"));
    bs.setBudget(wallet, "food", Money.of("100"));

    var list = bs.listBudgets(wallet);

    assertEquals(2, list.size());
    assertEquals("food", list.get(0).category());
    assertEquals("taxi", list.get(1).category());
  }
}
