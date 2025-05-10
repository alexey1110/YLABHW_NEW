package org.example.userInterface;

import org.example.model.User;
import org.example.service.TransactionService;
import org.example.service.GoalService;
import org.example.service.BudgetService;

import java.util.Scanner;

public class UserMenu {
    public static User show(Scanner scanner, User currentUser, TransactionService transactionService, GoalService goalService, BudgetService budgetService) {
        System.out.println("\n--- Добро пожаловать, " + currentUser.getName() + " ---");
        while (true){
            System.out.println("1. Добавить транзакцию");
            if (currentUser.isAdmin()){
                System.out.println("2. Редактировать транзакцию");
                System.out.println("3. Удалить транзакцию");
            }
            System.out.println("4. Просмотр транзакций");
            System.out.println("5. Управление бюджетом");
            System.out.println("6. Просмотр статистики");
            System.out.println("7. Управление целями");
            if (currentUser.isAdmin()) {
                System.out.println("8. Вернуться в меню администратора");
            } else {
                System.out.println("8. Выйти");
            }
            System.out.print("Выберите опцию: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    TransactionActions.add(scanner, transactionService, currentUser, goalService);
                    break;
                case 2:
                    TransactionActions.edit(scanner, transactionService, currentUser);
                    break;
                case 3:
                    TransactionActions.delete(scanner, transactionService, currentUser);
                    break;
                case 4:
                    TransactionActions.viewAll(scanner, transactionService, currentUser);
                    break;
                case 5:
                    BudgetActions.manage(scanner, budgetService, currentUser);
                    break;
                case 6:
                    StatisticsActions.showStatistics(transactionService, currentUser, scanner);
                    break;
                case 7:
                    GoalActions.manage(scanner, goalService, currentUser);
                    break;
                case 8:
                    System.out.println("Выход из системы.");
                    return null;
                default:
                    System.out.println("Неверный выбор, попробуйте снова.");
            }
        }
    }
}