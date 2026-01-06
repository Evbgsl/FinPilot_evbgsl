package com.evbgsl.finpilot.core;

import java.math.BigDecimal;

public record Money(BigDecimal value) {

  public Money {
    if (value == null) {
      throw new IllegalArgumentException("value must not be null");
    }
  }

  public static Money of(String raw) {

    if (raw == null) {
      throw new IllegalArgumentException("raw string must not be null");
    }

    // удаляем пробелы по краям и проверяем пустоту
    if (raw.trim().isEmpty()) {
      throw new IllegalArgumentException("raw string must not be empty");
    }

    // создаём BigDecimal
    BigDecimal bd = new BigDecimal(raw);

    // возвращаем новый Money
    return new Money(bd);
  }

  // Money immutable, поэтому результат — НОВЫЙ объект
  public Money add(Money other) {
    if (other == null) throw new IllegalArgumentException("other must not be null");

    // this.value() — метод геттера, который генерируется record
    BigDecimal sum = this.value().add(other.value());
    return new Money(sum);
  }

  public Money subtract(Money other) {
    if (other == null) throw new IllegalArgumentException("other must not be null");

    BigDecimal diff = this.value().subtract(other.value());
    return new Money(diff);
  }

  public int compareTo(Money other) {
    if (other == null) throw new IllegalArgumentException("other must not be null");

    return this.value().compareTo(other.value());
  }

  public static Money zero() {
    return new Money(java.math.BigDecimal.ZERO);
  }

  @Override
  public String toString() {
    return value.toPlainString() + " ₽";
  }
}
