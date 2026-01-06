package com.evbgsl.finpilot.infra.dto;

import com.evbgsl.finpilot.core.Budget;
import com.evbgsl.finpilot.core.Category;
import com.evbgsl.finpilot.core.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletDto {
    public String ownerLogin;
    public List<Transaction> operations = new ArrayList<>();
    public List<Category> categories = new ArrayList<>();
    public Map<String, Budget> budgets = new HashMap<>();
}
