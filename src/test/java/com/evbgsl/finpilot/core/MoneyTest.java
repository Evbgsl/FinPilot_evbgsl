package com.evbgsl.finpilot.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MoneyTest {

  // корректность сложения
  @Test
  void addShouldReturnSum() {
    Money a = Money.of("10");
    Money b = Money.of("5");

    Money result = a.add(b);

    assertEquals(Money.of("15"), result);
  }

  // корректность вычитания
  @Test
  void subtractShouldReturnDifference() {
    // given
    Money a = Money.of("20");
    Money b = Money.of("3");

    // when
    Money result = a.subtract(b);

    // then
    assertEquals(Money.of("17"), result);
  }

  // коректность сравнения
  @Test
  void compareToShouldReturnNegativeZeroOrPositive() {
    // small < big, small == same, big > small
    Money small = Money.of("10");
    Money same = Money.of("10");
    Money big = Money.of("20");

    // Проверяем три возможных исхода compareTo:
    // отрицательное, ноль и положительное значение
    assertTrue(small.compareTo(big) < 0);
    assertEquals(0, small.compareTo(same));
    assertTrue(big.compareTo(small) > 0);
  }

  // Проверяем исключение - не принимаем null
  @Test
  void ofShouldRejectNull() {
    // Money.of(null) бросает IllegalArgumentException
    assertThrows(IllegalArgumentException.class, () -> Money.of(null));
  }

  // Проверяем исключение - не принимаем пустые строки
  @Test
  void ofShouldRejectEmptyOrBlank() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(""));
    assertThrows(IllegalArgumentException.class, () -> Money.of("   "));
  }
}
