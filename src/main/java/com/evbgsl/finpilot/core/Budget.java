package com.evbgsl.finpilot.core;

public record Budget(String category, Money limit) {
    public Budget {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория для бюджета не может быть пустой");
        }
        if (limit == null) {
            throw new IllegalArgumentException("Лимит бюджета не может быть null");
        }
        // можно запретить отрицательные и ноль (обычно бюджет должен быть > 0)
        if (limit.value().signum() <= 0) {
            throw new IllegalArgumentException("Лимит бюджета должен быть больше 0");
        }
    }
}
