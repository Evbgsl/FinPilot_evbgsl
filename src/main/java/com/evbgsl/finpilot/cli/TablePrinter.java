package com.evbgsl.finpilot.cli;

import java.util.ArrayList;
import java.util.List;

public class TablePrinter {

  public static void print(List<String> headers, List<List<String>> rows) {
    List<Integer> widths = calcWidths(headers, rows);

    printLine(widths);
    printRow(headers, widths);
    printLine(widths);

    for (var r : rows) {
      printRow(r, widths);
    }
    printLine(widths);
  }

  private static List<Integer> calcWidths(List<String> headers, List<List<String>> rows) {
    List<Integer> w = new ArrayList<>();
    for (int i = 0; i < headers.size(); i++) {
      w.add(headers.get(i).length());
    }

    for (var r : rows) {
      for (int i = 0; i < r.size(); i++) {
        w.set(i, Math.max(w.get(i), r.get(i).length()));
      }
    }
    return w;
  }

  private static void printLine(List<Integer> widths) {
    StringBuilder sb = new StringBuilder();
    sb.append("+");
    for (int w : widths) {
      sb.append("-".repeat(w + 2)).append("+");
    }
    System.out.println(sb);
  }

  private static void printRow(List<String> row, List<Integer> widths) {
    StringBuilder sb = new StringBuilder();
    sb.append("|");
    for (int i = 0; i < widths.size(); i++) {
      String cell = i < row.size() ? row.get(i) : "";
      sb.append(" ").append(padRight(cell, widths.get(i))).append(" |");
    }
    System.out.println(sb);
  }

  private static String padRight(String s, int width) {
    if (s.length() >= width) return s;
    return s + " ".repeat(width - s.length());
  }
}
