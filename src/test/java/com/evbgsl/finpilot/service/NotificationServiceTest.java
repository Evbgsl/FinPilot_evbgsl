package com.evbgsl.finpilot.service;

import static org.junit.jupiter.api.Assertions.*;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Wallet;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

  @Test
  void shouldWarnWhenCross80Percent() {
    Wallet wallet = new Wallet("u1");
    wallet.addCategory("food");
    wallet.setBudget("food", Money.of("1000"));

    NotificationService ns = new NotificationService();
    WalletService ws = new WalletService(ns);

    // до 80%: 700
    ws.addExpense(wallet, "food", Money.of("700"), "");

    // пересекаем 80%: +200 -> 900
    List<String> notes = ws.addExpense(wallet, "food", Money.of("200"), "");

    assertTrue(notes.stream().anyMatch(s -> s.contains("80% бюджета")));
  }

  @Test
  void shouldNotifyWhenLimitExceeded() {
    Wallet wallet = new Wallet("u1");
    wallet.addCategory("food");
    wallet.setBudget("food", Money.of("1000"));

    NotificationService ns = new NotificationService();
    WalletService ws = new WalletService(ns);

    ws.addExpense(wallet, "food", Money.of("1000"), "");
    List<String> notes = ws.addExpense(wallet, "food", Money.of("1"), "");

    assertTrue(notes.stream().anyMatch(s -> s.contains("Лимит превышен")));
  }

  @Test
  void shouldWarnWhenExpensesGreaterThanIncome() {
    Wallet wallet = new Wallet("u1");
    wallet.addCategory("salary");
    wallet.addCategory("food");

    // доход 500
    wallet.addIncome("salary", Money.of("500"), "");

    NotificationService ns = new NotificationService();
    WalletService ws = new WalletService(ns);

    // расход 600 -> расходы > доходов
    List<String> notes = ws.addExpense(wallet, "food", Money.of("600"), "");

    assertTrue(notes.stream().anyMatch(s -> s.contains("расходы превысили доходы")));
  }
}
