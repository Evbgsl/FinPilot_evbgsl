package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Wallet;
import com.evbgsl.finpilot.infra.FileStorage;

public class TransferService {

    private final AuthService authService;
    private final FileStorage walletStorage;

    public TransferService(AuthService authService, FileStorage walletStorage) {
        this.authService = authService;
        this.walletStorage = walletStorage;
    }

    public void transfer(String fromLogin, String toLogin, Money amount) {
        String from = norm(fromLogin);
        String to = norm(toLogin);

        if (from.isEmpty() || to.isEmpty()) {
            throw new IllegalArgumentException("Логин отправителя/получателя не может быть пустым");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("Нельзя переводить самому себе");
        }
        if (amount == null || amount.value().signum() <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше 0");
        }
        if (!authService.userExists(to)) {
            throw new IllegalArgumentException("Получатель не найден: " + to);
        }

        Wallet sender = authService.getCurrentWallet();
        if (sender == null || !norm(sender.getOwnerLogin()).equals(from)) {
            throw new IllegalStateException("Нет активного кошелька отправителя");
        }

        // (опционально) проверка, что средств хватает
        if (sender.getBalance().value().compareTo(amount.value()) < 0) {
            throw new IllegalArgumentException("Недостаточно средств для перевода");
        }

        Wallet receiver = walletStorage.load(to).orElseGet(() -> new Wallet(to));

        sender.addTransferOut(toLogin, amount, "");
        receiver.addTransferIn(fromLogin, amount, "");

        // критично: сохранить ОБА кошелька
        walletStorage.save(sender);
        walletStorage.save(receiver);
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
