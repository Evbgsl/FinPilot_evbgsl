package com.evbgsl.finpilot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Wallet;
import java.util.List;
import org.junit.jupiter.api.Test;

class WalletServiceTest {

  @Test
  void addIncome_shouldAddIncomeOperation() {
    NotificationService ns = mock(NotificationService.class);
    WalletService ws = new WalletService(ns);

    Wallet wallet = new Wallet("u1");
    wallet.addCategory("salary");

    ws.addIncome(wallet, "salary", Money.of("500"), "");

    assertTrue(wallet.getOperations().stream().anyMatch(t -> t.amount().equals(Money.of("500"))));
    verifyNoInteractions(ns);
  }

  @Test
  void addExpense_shouldReturnNotificationsFromNotificationService() {
    NotificationService ns = mock(NotificationService.class);
    WalletService ws = new WalletService(ns);

    Wallet wallet = new Wallet("u1");
    wallet.addCategory("food");

    List<String> expected = List.of("note1", "note2");
    when(ns.afterExpense(wallet, "food", Money.of("200"))).thenReturn(expected);

    List<String> actual = ws.addExpense(wallet, "food", Money.of("200"), "");

    assertEquals(expected, actual);
    verify(ns, times(1)).afterExpense(wallet, "food", Money.of("200"));
  }
}
