package com.evbgsl.finpilot.cli;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.service.AuthService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AuthService authService = new AuthService();

        System.out.println("FinPilot CLI запущен. Введите 'help' для отображения команд, 'exit' для выхода.");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                if (line.startsWith("register ")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 3) {
                        System.out.println("Использование: register <login> <password>");
                        continue;
                    }
                    String login = parts[1].toLowerCase();
                    String password = parts[2];
                    authService.register(login, password);
                    System.out.println("Пользователь зарегистрирован: " + login);
                } else if (line.startsWith("login ")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 3) {
                        System.out.println("Использование: login <login> <password>");
                        continue;
                    }
                    String login = parts[1].toLowerCase();
                    String password = parts[2];
                    authService.login(login, password);
                    System.out.println("Вход выполнен: " + login);
                } else if (line.equals("logout")) {
                    authService.logout();
                    System.out.println("Вы вышли из аккаунта.");
                } else if (line.equals("whoami")) {
                    var user = authService.getCurrentUser();
                    System.out.println(user == null ? "Не залогинен" : user.login());
                }
                else if (line.startsWith("income add ")) {
                    authService.ensureLoggedIn();

                    var wallet = authService.getCurrentWallet();

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 4) {
                        System.out.println("Использование: income add <category> <amount>");
                        continue;
                    }

                    String category = parts[2].toLowerCase();
                    Money amount = Money.of(parts[3]);
                    String note = ""; // позже сделаем нормальные заметки
                    wallet.addIncome(category, amount, note);
                    System.out.println("Доход добавлен");

                } else if (line.startsWith("expense add ")) {
                    authService.ensureLoggedIn();

                    var wallet = authService.getCurrentWallet();

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 4) {
                        System.out.println("Использование: expense add <category> <amount>");
                        continue;
                    }

                    String category = parts[2].toLowerCase();
                    Money amount = Money.of(parts[3]);
                    String note = "";
                    wallet.addExpense(category, amount, note);
                    System.out.println("Расход добавлен");
                } else if (line.equals("balance")) {
                    authService.ensureLoggedIn();

                    var wallet = authService.getCurrentWallet();

                    System.out.println("Текущий баланс: " + wallet.getBalance());
                } else if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Выход из программы");
                    break;
                } else if (line.equalsIgnoreCase("help")) {
                    printHelp();
                } else {
                    System.out.println("Неизвестная команда: " + line);
                    System.out.println("Введите 'help' для просмотра возможных команд");
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public static void printHelp() {
        System.out.println("Возможные команды");
        System.out.println("help - для вывода всех возможных команд");
        System.out.println("exit - для выхода из программы");
        System.out.println("whoami - для просмотра имени текущего пользователя");
        System.out.println("register <login> <password> - регистрация нового пользователя");
        System.out.println("login <login> <password> - вход для текущего пользователя");
        System.out.println("logout - выход для текущего пользователя");
        System.out.println("income add <category> <amount> - добавить приход денежных стредств");
        System.out.println("expense add <category> <amount> - добавить расход денежных стредств");
        System.out.println("balance - отразить баланс текущего пользователя");
    }
}

