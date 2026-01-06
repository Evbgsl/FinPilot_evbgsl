package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Wallet;
import java.util.List;

public class WalletService {

  private final NotificationService notificationService;

  public WalletService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  public void addIncome(Wallet wallet, String category, Money amount, String note) {
    wallet.addIncome(category, amount, note);
  }

  public List<String> addExpense(Wallet wallet, String category, Money amount, String note) {
    wallet.addExpense(category, amount, note);

    // После добавления расхода проверяем бюджеты и общий перерасход
    return notificationService.afterExpense(wallet, category, amount);
  }
}
