package com.evbgsl.finpilot.core;

public enum TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER_IN,
    TRANSFER_OUT;

    public boolean isIncomeLike() {
        return this == INCOME || this == TRANSFER_IN;
    }

    public boolean isExpenseLike() {
        return this == EXPENSE || this == TRANSFER_OUT;
    }
}
