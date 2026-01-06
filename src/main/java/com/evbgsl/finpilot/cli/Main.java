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

import com.evbgsl.finpilot.service.ReportService;
import com.evbgsl.finpilot.service.ReportFilter;

import com.evbgsl.finpilot.service.TransferService;

import java.nio.file.Path;
import java.nio.file.Paths;



public class Main {
    public static void main(String[] args) {
        FileStorage walletStorage = new FileStorage();
        UserStorage userStorage = new UserStorage();
        AuthService authService = new AuthService(walletStorage, userStorage);
        TransferService transferService = new TransferService(authService, walletStorage);
        NotificationService notificationService = new NotificationService();
        WalletService walletService = new WalletService(notificationService);
        CategoryService categoryService = new CategoryService();
        BudgetService budgetService = new BudgetService();
        ReportService reportService = new ReportService();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                var wallet = authService.getCurrentWallet();
                if (wallet != null) {
                    walletStorage.save(wallet);
                }
                authService.saveUsers();
                System.out.println("\n[auto-save] Данные сохранены при завершении программы");
            } catch (Exception e) {
                System.out.println("\n[auto-save] Ошибка сохранения: " + e.getMessage());
            }
        }));


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
                } else if (line.startsWith("report summary")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    ReportFilter filter = ReportArgsParser.parse(line, "report summary");

                    // validate --only
                    if (filter.hasOnlyCategories()) {
                        var known = knownCategoriesFromOperations(wallet);
                        var missing = new java.util.ArrayList<String>();
                        for (var c : filter.onlyCategories()) {
                            if (!known.contains(c)) missing.add(c);
                        }
                        if (!missing.isEmpty()) {
                            System.out.println("Категории не найдены: " + String.join(", ", missing));
                            continue;
                        }
                    }

                    var s = reportService.summary(wallet, filter);
                    System.out.println("Итоги:");
                    System.out.println("Общий доход:  " + s.totalIncome());
                    System.out.println("Общий расход: " + s.totalExpense());
                    System.out.println("Баланс:       " + s.balance());
                } else if (line.startsWith("report categories")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    ReportFilter filter = ReportArgsParser.parse(line, "report categories");

                    if (filter.hasOnlyCategories()) {
                        var known = knownCategoriesFromOperations(wallet);
                        var missing = new java.util.ArrayList<String>();
                        for (var c : filter.onlyCategories()) {
                            if (!known.contains(c)) missing.add(c);
                        }
                        if (!missing.isEmpty()) {
                            System.out.println("Категории не найдены: " + String.join(", ", missing));
                            continue;
                        }
                    }

                    var rows = reportService.categories(wallet, filter);
                    if (rows.isEmpty()) {
                        System.out.println("Нет данных для отчёта (проверь фильтры)");
                        continue;
                    }

                    var tableRows = new java.util.ArrayList<java.util.List<String>>();
                    for (var r : rows) {
                        tableRows.add(java.util.List.of(r.category(), r.income().toString(), r.expense().toString()));
                    }

                    TablePrinter.print(
                            java.util.List.of("Категория", "Доход", "Расход"),
                            tableRows
                    );
                } else if (line.startsWith("report budgets")) {
                    authService.ensureLoggedIn();
                    var wallet = authService.getCurrentWallet();

                    ReportFilter filter = ReportArgsParser.parse(line, "report budgets");

                    if (filter.hasOnlyCategories()) {
                        // здесь “категория не найдена” логично проверять по бюджету тоже,
                        // но требование звучит именно про отсутствие категорий, так что проверяем по операциям.
                        var known = knownCategoriesFromOperations(wallet);
                        var missing = new java.util.ArrayList<String>();
                        for (var c : filter.onlyCategories()) {
                            if (!known.contains(c)) missing.add(c);
                        }
                        if (!missing.isEmpty()) {
                            System.out.println("Категории не найдены: " + String.join(", ", missing));
                            continue;
                        }
                    }

                    var rows = reportService.budgets(wallet, filter);
                    if (rows.isEmpty()) {
                        System.out.println("Нет данных по бюджетам (или бюджеты не заданы / фильтры отфильтровали всё)");
                        continue;
                    }

                    var tableRows = new java.util.ArrayList<java.util.List<String>>();
                    for (var r : rows) {
                        tableRows.add(java.util.List.of(
                                r.category(),
                                r.limit().toString(),
                                r.spent().toString(),
                                r.remaining().toString()
                        ));
                    }

                    TablePrinter.print(
                            java.util.List.of("Категория", "Лимит", "Потрачено", "Остаток"),
                            tableRows
                    );
                } else if (line.startsWith("transfer to ")) {
                    authService.ensureLoggedIn();

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 4) {
                        System.out.println("Использование: transfer to <login> <amount>");
                        continue;
                    }

                    String toLogin = parts[2].toLowerCase();
                    Money amount = Money.of(parts[3]);

                    String fromLogin = authService.getCurrentUser().login();

                    transferService.transfer(fromLogin, toLogin, amount);

                    System.out.println("Перевод выполнен: " + fromLogin + " -> " + toLogin + " (" + amount + ")");
                } else if (line.startsWith("export json ")) {
                    authService.ensureLoggedIn();

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 3) {
                        System.out.println("Использование: export json <path>");
                        continue;
                    }

                    Path path = Paths.get(parts[2]);
                    var wallet = authService.getCurrentWallet();

                    walletStorage.save(wallet, path);
                    System.out.println("Экспорт выполнен: " + path.toAbsolutePath());
                } else if (line.startsWith("import json ")) {
                    authService.ensureLoggedIn();

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 3) {
                        System.out.println("Использование: import json <path>");
                        continue;
                    }

                    Path path = Paths.get(parts[2]);
                    var imported = walletStorage.load(path)
                            .orElseThrow(() -> new IllegalArgumentException("Файл не найден или пустой: " + path));

                    String currentLogin = authService.getCurrentUser().login().trim().toLowerCase();
                    String importedLogin = imported.getOwnerLogin().trim().toLowerCase();

                    if (!importedLogin.equals(currentLogin)) {
                        System.out.println("Ошибка: кошелёк в файле принадлежит '" + importedLogin +
                                "', а вы залогинены как '" + currentLogin + "'");
                        continue;
                    }

                    // подменяем текущий кошелёк в памяти
                    authService.setCurrentWallet(imported);

                    // и сохраняем в стандартное хранилище wallets/<login>.json
                    walletStorage.save(imported);

                    System.out.println("Импорт выполнен: " + path.toAbsolutePath());
                } else if (line.equals("ops")) {
                    authService.ensureLoggedIn();
                    authService.getCurrentWallet().getOperations()
                            .forEach(System.out::println);
                } else {
                    System.out.println("Неизвестная команда: " + line);
                    System.out.println("Введите 'help' для просмотра возможных команд");
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static java.util.Set<String> knownCategoriesFromOperations(com.evbgsl.finpilot.core.Wallet wallet) {
        var set = new java.util.HashSet<String>();
        for (var t : wallet.getOperations()) {
            if (t.category() != null) set.add(t.category().trim().toLowerCase());
        }
        return set;
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

        System.out.println();
        System.out.println("Переводы:");
        System.out.println("  transfer to <login> <amount>      - перевод другому пользователю");

        System.out.println();
        System.out.println("Отчёты:");
        System.out.println("  report summary                    - общий доход/расход/баланс");
        System.out.println("  report categories                 - отчёт по категориям (доход/расход)");
        System.out.println("  report budgets                    - бюджеты: лимит/потрачено/остаток");

        System.out.println("  report summary [--from yyyy-mm-dd] [--to yyyy-mm-dd] [--only cat1,cat2]");
        System.out.println("  report categories [--from yyyy-mm-dd] [--to yyyy-mm-dd] [--only cat1,cat2]");
        System.out.println("  report budgets [--from yyyy-mm-dd] [--to yyyy-mm-dd] [--only cat1,cat2]");

        System.out.println();
        System.out.println("Импорт/экспорт:");
        System.out.println("export json <path>                  - экспорт текущего кошелька в JSON");
        System.out.println("import json <path>                  - импорт кошелька из JSON (для текущего пользователя)");
    }


}

