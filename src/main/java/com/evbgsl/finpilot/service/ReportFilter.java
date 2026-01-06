package com.evbgsl.finpilot.service;

import java.time.LocalDate;
import java.util.Set;

public record ReportFilter(LocalDate from, LocalDate to, Set<String> onlyCategories) {
    public boolean hasDateRange() {
        return from != null || to != null;
    }

    public boolean hasOnlyCategories() {
        return onlyCategories != null && !onlyCategories.isEmpty();
    }
}
