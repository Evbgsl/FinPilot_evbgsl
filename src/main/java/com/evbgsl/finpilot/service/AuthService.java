package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.User;
import com.evbgsl.finpilot.core.Wallet;
import com.evbgsl.finpilot.infra.FileStorage;

import com.evbgsl.finpilot.infra.FileStorage;
import com.evbgsl.finpilot.infra.UserStorage;

import java.util.HashMap;
import java.util.Map;


import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private final Map<String, User> users;
    private final FileStorage walletStorage;
    private final UserStorage userStorage;
    private User currentUser;
    private Wallet currentWallet;


    public AuthService(FileStorage walletStorage, UserStorage userStorage) {
        this.walletStorage = walletStorage;
        this.userStorage = userStorage;
        this.users = new HashMap<>(userStorage.loadUsers());
    }



    public void register(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (users.containsKey(login)) {
            throw new IllegalArgumentException("Пользователь с таким логином существует: " + login);
        }
        users.put(login, new User(login, password));
        userStorage.saveUsers(users);
    }

    public void login(String login, String password) {
        String key = normalize(login);

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (!users.containsKey(login)) {
            throw new IllegalArgumentException("Пользователя с таким логином не существует: " + login);
        }

        User user = users.get(login);

        if (user == null) {
            throw new IllegalArgumentException("Пользователя с таким логином не существует: " + login);
        }

        if (!password.equals(user.passwordHash())) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        currentUser = user;
        currentWallet = walletStorage.load(key).orElseGet(() -> new Wallet(key));
    }

    public void saveUsers() {
        userStorage.saveUsers(users);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }



    public void logout() {
        currentUser = null;
        currentWallet = null;
    }

    public Wallet getCurrentWallet() {
        return currentWallet;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void ensureLoggedIn() {
        if (currentUser == null) {
            throw new IllegalStateException("Сначала войдите: login <login> <password>");
        }
    }
}
