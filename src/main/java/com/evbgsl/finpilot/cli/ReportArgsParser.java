package com.evbgsl.finpilot.cli;

import com.evbgsl.finpilot.service.ReportFilter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ReportArgsParser {

  public static ReportFilter parse(String line, String basePrefix) {
    String rest = line.substring(basePrefix.length()).trim();
    if (rest.isEmpty()) {
      return new ReportFilter(null, null, Set.of());
    }

    List<String> tokens = List.of(rest.split("\\s+"));
    LocalDate from = null;
    LocalDate to = null;
    Set<String> only = new LinkedHashSet<>();

    for (int i = 0; i < tokens.size(); i++) {
      String t = tokens.get(i);

      if (t.equals("--from")) {
        if (i + 1 >= tokens.size()) {
          throw new IllegalArgumentException("Параметр --from требует дату: yyyy-mm-dd");
        }
        from = parseDate(tokens.get(++i), "--from");
      } else if (t.equals("--to")) {
        if (i + 1 >= tokens.size()) {
          throw new IllegalArgumentException("Параметр --to требует дату: yyyy-mm-dd");
        }
        to = parseDate(tokens.get(++i), "--to");
      } else if (t.equals("--only")) {
        if (i + 1 >= tokens.size()) {
          throw new IllegalArgumentException("Параметр --only требует список категорий: cat1,cat2");
        }
        String raw = tokens.get(++i);
        for (String c : raw.split(",")) {
          String norm = normalize(c);
          if (!norm.isEmpty()) only.add(norm);
        }
        if (only.isEmpty()) {
          throw new IllegalArgumentException(
              "Параметр --only не должен быть пустым. Пример: --only food,taxi");
        }
      } else {
        throw new IllegalArgumentException("Неизвестный параметр: " + t);
      }
    }

    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("Некорректный диапазон: --from не может быть позже --to");
    }

    return new ReportFilter(from, to, only);
  }

  private static LocalDate parseDate(String s, String flag) {
    try {
      return LocalDate.parse(s);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Некорректная дата для " + flag + ": " + s + " (ожидается yyyy-mm-dd)");
    }
  }

  private static String normalize(String s) {
    if (s == null) return "";
    return s.trim().toLowerCase();
  }
}
