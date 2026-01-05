package com.evbgsl.finpilot.core;

public record Category(String name) {
    public Category {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        name = name.trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("category name must not be empty");
        }

        name = name.toLowerCase();
    }
}
