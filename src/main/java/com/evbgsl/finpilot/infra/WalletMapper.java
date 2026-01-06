package com.evbgsl.finpilot.infra;

import com.evbgsl.finpilot.core.Wallet;
import com.evbgsl.finpilot.infra.dto.WalletDto;

public class WalletMapper {

  public static WalletDto toDto(Wallet wallet) {
    WalletDto dto = new WalletDto();
    dto.ownerLogin = wallet.getOwnerLogin();
    dto.operations.addAll(wallet.getOperations());
    dto.categories.addAll(wallet.getCategories()); // см. ниже про getCategories()
    dto.budgets.putAll(wallet.getBudgets());
    return dto;
  }

  public static Wallet fromDto(WalletDto dto) {
    if (dto == null || dto.ownerLogin == null) {
      throw new IllegalArgumentException("Некорректные данные кошелька в файле");
    }

    Wallet wallet = new Wallet(dto.ownerLogin);

    // категории
    for (var c : dto.categories) {
      wallet.addCategory(c.name());
    }

    // бюджеты
    for (var entry : dto.budgets.entrySet()) {
      wallet.setBudget(entry.getKey(), entry.getValue().limit());
    }

    // операции
    for (var t : dto.operations) {
      wallet.addOperationRaw(t); // см. ниже про addOperationRaw()
    }

    return wallet;
  }
}
