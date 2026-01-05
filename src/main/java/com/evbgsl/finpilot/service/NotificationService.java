package com.evbgsl.finpilot.service;

import com.evbgsl.finpilot.core.Budget;
import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.core.Transaction;
import com.evbgsl.finpilot.core.TransactionType;
import com.evbgsl.finpilot.core.Wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    /**
     * Возвращает список уведомлений после добавления расхода.
     */
    public List<String> afterExpense(Wallet wallet, String category, Money expenseAmount) {
        List<String> notifications = new ArrayList<>();

        // 1) Проверка бюджета по категории
        wallet.getBudget(category).ifPresent(budget -> {
            Money spentBefore = sumExpensesByCategory(wallet, category).subtract(expenseAmount);
            Money spentAfter = sumExpensesByCategory(wallet, category);

            // 80% лимита: сработать только при пересечении порога
            if (crossed80Percent(budget, spentBefore, spentAfter)) {
                notifications.add("Предупреждение: вы достигли 80% бюджета по категории '"
                        + budget.category() + "'. Потрачено: " + spentAfter + " из " + budget.limit());
            }

            // Превышение: сработать только при первом выходе за лимит
            if (crossedLimit(budget, spentBefore, spentAfter)) {
                Money over = spentAfter.subtract(budget.limit());
                notifications.add("Лимит превышен по категории '" + budget.category()
                        + "'. Перерасход: " + over);
            }
        });

        // 2) Общий перерасход: расходы > доходов
        Money totalIncome = sumByType(wallet, TransactionType.INCOME);
        Money totalExpense = sumByType(wallet, TransactionType.EXPENSE);
        if (totalExpense.value().compareTo(totalIncome.value()) > 0) {
            Money diff = totalExpense.subtract(totalIncome);
            notifications.add("Внимание: расходы превысили доходы. Разница: " + diff);
        }

        return notifications;
    }

    private Money sumExpensesByCategory(Wallet wallet, String category) {
        BigDecimal sum = BigDecimal.ZERO;
        String key = normalize(category);

        for (Transaction t : wallet.getOperations()) {
            if (t.type() == TransactionType.EXPENSE && normalize(t.category()).equals(key)) {
                sum = sum.add(t.amount().value());
            }
        }
        return new Money(sum);
    }

    private Money sumByType(Wallet wallet, TransactionType type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : wallet.getOperations()) {
            if (t.type() == type) {
                sum = sum.add(t.amount().value());
            }
        }
        return new Money(sum);
    }

    private boolean crossed80Percent(Budget budget, Money before, Money after) {
        BigDecimal limit = budget.limit().value();
        BigDecimal threshold = limit.multiply(new BigDecimal("0.8"));

        return before.value().compareTo(threshold) < 0 && after.value().compareTo(threshold) >= 0
                && after.value().compareTo(limit) <= 0; // чтобы не мешать с "превышением"
    }

    private boolean crossedLimit(Budget budget, Money before, Money after) {
        BigDecimal limit = budget.limit().value();
        return before.value().compareTo(limit) <= 0 && after.value().compareTo(limit) > 0;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }
}
