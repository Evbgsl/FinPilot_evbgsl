package com.evbgsl.finpilot.core;

import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
    String id,
    TransactionType type,
    String category,
    Money amount,
    LocalDateTime dateTime,
    String note) {
  public static Transaction income(String category, Money amount, String note) {
    return new Transaction(
        UUID.randomUUID().toString(),
        TransactionType.INCOME,
        category,
        amount,
        LocalDateTime.now(),
        note);
  }

  public static Transaction expense(String category, Money amount, String note) {
    return new Transaction(
        UUID.randomUUID().toString(),
        TransactionType.EXPENSE,
        category,
        amount,
        LocalDateTime.now(),
        note);
  }

  public static Transaction transferOut(String category, Money amount, String note) {
    return new Transaction(
        UUID.randomUUID().toString(),
        TransactionType.TRANSFER_OUT,
        category,
        amount,
        LocalDateTime.now(),
        note);
  }

  public static Transaction transferIn(String category, Money amount, String note) {
    return new Transaction(
        UUID.randomUUID().toString(),
        TransactionType.TRANSFER_IN,
        category,
        amount,
        LocalDateTime.now(),
        note);
  }
}
