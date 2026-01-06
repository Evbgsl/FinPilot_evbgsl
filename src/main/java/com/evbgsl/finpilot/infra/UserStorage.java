package com.evbgsl.finpilot.infra;

import com.evbgsl.finpilot.core.User;
import com.evbgsl.finpilot.infra.dto.UsersDto;
import com.evbgsl.finpilot.infra.json.BigDecimalAdapter;
import com.evbgsl.finpilot.infra.json.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserStorage {

    private final Path baseDir;
    private final Path usersFile;
    private final Gson gson;

    public UserStorage() {
        this(Paths.get("wallets"));
    }

    public UserStorage(Path baseDir) {
        this.baseDir = baseDir;
        this.usersFile = baseDir.resolve("users.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(BigDecimal.class, new BigDecimalAdapter())
                .setPrettyPrinting()
                .create();
    }

    public Map<String, User> loadUsers() {
        try {
            if (!Files.exists(usersFile)) {
                return new HashMap<>();
            }
            String json = Files.readString(usersFile, StandardCharsets.UTF_8);
            UsersDto dto = gson.fromJson(json, UsersDto.class);
            if (dto == null || dto.users == null) {
                return new HashMap<>();
            }

            Map<String, User> result = new HashMap<>();
            for (UsersDto.UserDto u : dto.users) {
                if (u == null) continue;
                String login = normalize(u.login);
                if (login.isEmpty()) continue;
                String pass = u.passwordHash == null ? "" : u.passwordHash;
                result.put(login, new User(login, pass));
            }
            return result;

        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить пользователей: " + e.getMessage(), e);
        }
    }

    public void saveUsers(Map<String, User> users) {
        try {
            Files.createDirectories(baseDir);

            UsersDto dto = new UsersDto();
            if (users != null) {
                // стабильно пишем в одном порядке (приятно для git и глаз)
                users.keySet().stream().sorted().forEach(login -> {
                    User u = users.get(login);
                    if (u == null) return;
                    UsersDto.UserDto ud = new UsersDto.UserDto();
                    ud.login = u.login();
                    ud.passwordHash = u.passwordHash();
                    dto.users.add(ud);
                });
            }

            String json = gson.toJson(dto);
            Files.writeString(usersFile, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить пользователей: " + e.getMessage(), e);
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }
}
