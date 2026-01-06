package com.evbgsl.finpilot.infra;

import com.evbgsl.finpilot.core.Wallet;
import com.evbgsl.finpilot.infra.dto.WalletDto;
import com.evbgsl.finpilot.infra.json.BigDecimalAdapter;
import com.evbgsl.finpilot.infra.json.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class FileStorage {

    private final Path baseDir;
    private final Gson gson;

    public FileStorage() {
        this(Paths.get("wallets"));
    }

    public FileStorage(Path baseDir) {
        this.baseDir = baseDir;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(BigDecimal.class, new BigDecimalAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void save(Wallet wallet) {
        if (wallet == null) return;
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать папку wallets: " + e.getMessage(), e);
        }
        Path file = baseDir.resolve(wallet.getOwnerLogin() + ".json");
        save(wallet, file);
    }

    public void save(Wallet wallet, Path path) {
        if (wallet == null) return;
        if (path == null) throw new IllegalArgumentException("path is null");

        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            WalletDto dto = WalletMapper.toDto(wallet);
            String json = gson.toJson(dto);

            Files.writeString(path, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить файл: " + path + " (" + e.getMessage() + ")", e);
        }
    }

    public Optional<Wallet> load(String login) {
        if (login == null || login.trim().isEmpty()) return Optional.empty();
        Path file = baseDir.resolve(login.trim().toLowerCase() + ".json");
        return load(file);
    }

    public Optional<Wallet> load(Path path) {
        if (path == null) return Optional.empty();

        try {
            if (!Files.exists(path)) return Optional.empty();

            String json = Files.readString(path, StandardCharsets.UTF_8);
            WalletDto dto = gson.fromJson(json, WalletDto.class);
            if (dto == null) return Optional.empty();

            return Optional.of(WalletMapper.fromDto(dto));

        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл: " + path + " (" + e.getMessage() + ")", e);
        }
    }


}
