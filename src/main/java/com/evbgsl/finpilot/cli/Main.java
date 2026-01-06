package com.evbgsl.finpilot.cli;

import com.evbgsl.finpilot.core.Money;
import com.evbgsl.finpilot.service.AuthService;

import com.evbgsl.finpilot.service.CategoryService;
import com.evbgsl.finpilot.core.Category;

import com.evbgsl.finpilot.service.BudgetService;

import java.util.Scanner;

import com.evbgsl.finpilot.service.NotificationService;
import com.evbgsl.finpilot.service.WalletService;
import com.evbgsl.finpilot.infra.FileStorage;

import com.evbgsl.finpilot.infra.UserStorage;



public class Main {
    public static void main(String[] args) {
        FileStorage walletStorage = new FileStorage();
        UserStorage userStorage = new UserStorage();
        AuthService authService = new AuthService(walletStorage, userStorage);
        NotificationService notificationService = new NotificationService();
        WalletService walletService = new WalletService(notificationService);
        CategoryService categoryService = new CategoryService();
        BudgetService budgetService = new BudgetService();

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
                } else if (line.startsWith("category add ")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    String name = line.substring("category add ".length()).trim();
                    categoryService.addCategory(wallet, name);
                    System.out.println("Категория добавлена: " + new Category(name).name());
                } else if (line.equalsIgnoreCase("category list")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    var categories = categoryService.listCategories(wallet);
                    if (categories.isEmpty()) {
                        System.out.println("Категорий пока нет");
                    } else {
                        System.out.println("Категории:");
                        for (var c : categories) {
                            System.out.println(" - " + c.name());
                        }
                    }
                } else if (line.startsWith("budget set ")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 4) {
                        System.out.println("Использование: budget set <category> <amount>");
                        continue;
                    }

                    String category = parts[2].toLowerCase();
                    Money limit = Money.of(parts[3]);

                    budgetService.setBudget(wallet, category, limit);
                    System.out.println("Бюджет установлен: " + category + " = " + limit);
                } else if (line.equals("budget list")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    var budgets = budgetService.listBudgets(wallet);
                    if (budgets.isEmpty()) {
                        System.out.println("Бюджеты не заданы");
                        continue;
                    }

                    System.out.println("Бюджеты:");
                    for (var b : budgets) {
                        System.out.println(" - " + b.category() + ": " + b.limit());
                    }
                } else if (line.startsWith("income add ")) {
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

                    var notifications = walletService.addExpense(wallet, category, amount, note);
                    System.out.println("Расход добавлен");

                    for (String msg : notifications) {
                        System.out.println(msg);
                    }
                } else if (line.equals("balance")) {
                    authService.ensureLoggedIn();

                    var wallet = authService.getCurrentWallet();

                    System.out.println("Текущий баланс: " + wallet.getBalance());
                } else if (line.equalsIgnoreCase("exit")) {
                    var wallet = authService.getCurrentWallet();
                    if (wallet != null) {
                        walletStorage.save(wallet);
                        System.out.println("Кошелёк сохранён.");
                    }

                    authService.saveUsers();
                    System.out.println("Пользователи сохранены.");

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
        System.out.println("Команды FinPilot:");
        System.out.println();

        System.out.println("Общее:");
        System.out.println("  help                              - показать эту справку");
        System.out.println("  exit                              - выход из программы");
        System.out.println("  whoami                            - показать текущего пользователя");

        System.out.println();
        System.out.println("Авторизация:");
        System.out.println("  register <login> <password>       - регистрация нового пользователя");
        System.out.println("  login <login> <password>          - вход под пользователем");
        System.out.println("  logout                            - выход из аккаунта");

        System.out.println();
        System.out.println("Операции:");
        System.out.println("  income add <category> <amount>    - добавить доход");
        System.out.println("  expense add <category> <amount>   - добавить расход");
        System.out.println("  balance                           - показать текущий баланс");

        System.out.println();
        System.out.println("Категории:");
        System.out.println("  category add <name>               - добавить категорию (можно с пробелами)");
        System.out.println("  category list                     - вывести список категорий");

        System.out.println();
        System.out.println("Бюджеты:");
        System.out.println("  budget set <category> <amount>    - установить бюджет на категорию");
        System.out.println("  budget list                       - вывести список бюджетов");
    }


}

