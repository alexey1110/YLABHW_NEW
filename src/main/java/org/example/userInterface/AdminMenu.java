package org.example.userInterface;

import org.example.model.User;
import org.example.service.BudgetService;
import org.example.service.GoalService;
import org.example.service.TransactionService;
import org.example.service.UserService;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class AdminMenu {
    public static User show(Scanner scanner, User currentUser, TransactionService transactionService, GoalService goalService, BudgetService budgetService, UserService userService) {

        while (true){
            System.out.println("\n--- Добро пожаловать, в меню Администратора " + currentUser.getName() + " ---");

            System.out.println("1. Посмотреть список пользователей");
            System.out.println("2. Заблокировать пользователя");
            System.out.println("3. Раблокировать пользователя");
            System.out.println("4. Удалить пользователя");
            System.out.println("5. Войти в меню пользователя");
            System.out.println("6. Выход");
            int choice1 = scanner.nextInt();
            scanner.nextLine();

            switch (choice1) {
                case 1:
                    viewUsers(userService);
                    break;
                case 2:
                    System.out.println("Введите id польователя чобы заблокировать пользователя");
                    blockUser(scanner.nextLong(), userService);
                    break;
                case 3:
                    System.out.println("Введите id польователя чобы заблокировать пользователя");
                    unblockUser(scanner.nextLong(), userService);
                    break;
                case 4:
                    System.out.println("Введите id польователя чобы удалить пользователя");
                    deleteUser(scanner.nextLong(), userService);
                    break;
                case 5:
                    choiceUser(userService, currentUser, scanner, transactionService, goalService, budgetService );
                    break;
                case 6:
                    return null;
                default:
                    System.out.println("Неверный выбор, попробуйте снова.");
            }
        }
    }

    public static void viewUsers(UserService userService){
        Map<Long, User> users = userService.getAllUsers();
        for (Map.Entry<Long, User> user : users.entrySet()){
            System.out.println(user.getKey() + " - " + user.getValue());
        }
    }

    public static void choiceUser(UserService userService, User adminUser, Scanner scanner, TransactionService transactionService, GoalService goalService, BudgetService budgetService){
        System.out.println("Введите id польователя чобы выбрать пользователя");
        Optional<User> user = userService.getUserById(scanner.nextInt());
        scanner.nextLine();

        if(user.isPresent()){
            User selectedUser = user.get();
            selectedUser.setAdmin(true);
            UserMenu.show(scanner, selectedUser, transactionService, goalService, budgetService);
            selectedUser.setAdmin(false);
        } else {
            System.out.println("Пользователь с таким ID не найден.");
        }
    }

    public static void deleteUser(long userId, UserService userService){
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isPresent()) {
            userService.deleteUser(userOpt.get().getEmail());
        } else {
            System.out.println("Ошибка");
        }
    }

    public static void blockUser(long userId, UserService userService) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isPresent()) {
            userService.blockUser(userId);
            System.out.println("Пользователь заблокирован.");
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    public static void unblockUser(long userId, UserService userService) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isPresent()) {
            userService.unblockUser(userId);
            System.out.println("Пользователь разблокирован.");
        } else {
            System.out.println("Пользователь не найден.");
        }
    }
}
