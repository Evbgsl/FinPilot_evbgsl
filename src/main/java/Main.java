import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("FinPilot CLI запущен. Введите 'help' для отображения команд, 'exit' для выхода.");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("> ");
            if(!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine();
            if (line.isEmpty()) {
                continue;
            }

            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Выход из программы");
                break;
            } else if (line.equalsIgnoreCase("help")) {
                printHelp();
            } else {
                System.out.println("Неизвестная команда: " + line);
                System.out.println("Введите 'help' для просмотра возможных команд");
            }
        }
    }

    public static void printHelp() {
        System.out.println("Возможные команды");
        System.out.println("help - для вывода всех возможных команд");
        System.out.println("exit - для выхода из программы");
        // позже дополним
    }

}

