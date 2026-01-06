package com.evbgsl.finpilot.core;

public record User(String login, String passwordHash) {

  public User {
    if (login == null || login.trim().isEmpty()) {
      throw new IllegalArgumentException("Логин не должен быть пустым");
    }

    if (passwordHash == null || passwordHash.trim().isEmpty()) {
      throw new IllegalArgumentException("Пароль не должен быть пустым");
    }
  }

  // Для валидации и корректного создания пользователя перед вызовом конструктора используем паттерн
  // фабрика
  public static User of(String login, String rawPassword) {
    if (login == null || login.trim().isEmpty()) {
      throw new IllegalArgumentException("Логин не должен быть пустым");
    }
    if (rawPassword == null || rawPassword.isEmpty()) {
      throw new IllegalArgumentException("Пароль не должен быть пустым");
    }

    // Пока просто храним строку как есть
    String normalizedLogin = login.trim();
    String passwordHash = rawPassword; // потом заменим на hash(rawPassword)

    return new User(normalizedLogin, passwordHash);
  }
}
