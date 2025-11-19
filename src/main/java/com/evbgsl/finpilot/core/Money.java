package com.evbgsl.finpilot.core;

import java.math.BigDecimal;

public record Money(BigDecimal value) {

    static final String raw = "";

    public Money {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    public static Money of(String raw) {
        // нужно:
        // 1) проверить, что raw не null и не пустая
        // 2) создать BigDecimal
        // 3) вернуть new Money(...)

        return null;
    }

    public Money add(Money other) {
        // Добавим
        return other;
    }
    public Money subtract(Money other) {
        // Вычтем
        return other;
    }
    public int compareTo(Money other) {
        // Сравним
        return 0;
    }
}
