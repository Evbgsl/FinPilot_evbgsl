package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Category;
import com.evbgsl.finpilot.core.Wallet;

import java.util.List;

public class CategoryService {

    public void addCategory(Wallet wallet, String name) {
        if (wallet == null) {
            throw new IllegalStateException("Сначала войдите: login <login> <password>");
        }
        // Вся валидация (null/empty/trim/lower + дубль) уже внутри Wallet/Category
        wallet.addCategory(name);
    }

    public List<Category> listCategories(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalStateException("Сначала войдите: login <login> <password>");
        }
        return wallet.getCategories();
    }
}
