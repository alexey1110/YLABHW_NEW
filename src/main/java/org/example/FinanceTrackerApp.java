package org.example;

import org.example.model.User;
import org.example.service.*;
import org.example.repository.UserRepository;
import org.example.repository.GoalRepository;
import org.example.repository.BudgetRepository;
import org.example.repository.TransactionRepository;
import org.example.userInterface.AdminMenu;
import org.example.userInterface.MainMenu;
import org.example.userInterface.UserMenu;

import java.util.Scanner;

public class FinanceTrackerApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static UserRepository userRepository = new UserRepository();
    private static TransactionRepository transactionRepository = new TransactionRepository();
    private static GoalRepository goalRepository = new GoalRepository();
    private static BudgetRepository budgetRepository = new BudgetRepository();
    private static UserService userService = new UserService(userRepository);
    private static GoalService goalService = new GoalService(goalRepository);
    private static BudgetService budgetService = new BudgetService(budgetRepository);
    private static NotificationService notificationService = new NotificationService(budgetService, goalService);
    private static TransactionService transactionService = new TransactionService(transactionRepository, goalService, budgetService, userRepository, notificationService);

    private static User currentUser = null;

    public static void main(String[] args) {
        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else if (!currentUser.isAdmin()){
                currentUser = showUserMenu();
            } else {
                currentUser = showAdminMenu();
            }
        }
    }

    private static void showMainMenu() {
        currentUser = MainMenu.show(currentUser, scanner, userService);
    }

    private static User showUserMenu() {
        return UserMenu.show(scanner, currentUser, transactionService, goalService, budgetService);
    }
    private static User showAdminMenu() {
        return AdminMenu.show(scanner, currentUser, transactionService, goalService, budgetService, userService);
    }
}