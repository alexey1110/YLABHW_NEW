package org.example.service;

import org.example.model.Goal;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.model.transactionEnum.TransactionType;
import org.example.model.transactionEnum.Category;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final GoalService goalService;
    private final BudgetService budgetService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository, GoalService goalService, BudgetService budgetService, UserRepository userRepository, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.goalService = goalService;
        this.budgetService = budgetService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public void addTransaction(long userId, double amount, Category category, String description, TransactionType type, Long goalId) {
        if(!userRepository.findById(userId).get().isBlocked()){
            Transaction transaction = new Transaction(userId, amount, LocalDate.now(), category, description, type, goalId);

            if (type == TransactionType.INCOME && goalId != null) {
                double percentToSave = 0.1;
                double amountToSave = amount * percentToSave;
                Goal goal = goalService.getGoalById(goalId);
                if( amountToSave > goal.getTargetAmount() - goal.getCurrentAmount()){
                    goalService.addSavingsToGoal(goalId, goal.getTargetAmount() - goal.getCurrentAmount());
                    transaction.setAmount(amount - amountToSave + (goal.getTargetAmount() - goal.getCurrentAmount()));
                    transactionRepository.save(transaction);
                } else {
                    goalService.addSavingsToGoal(goalId, amountToSave);
                    transaction.setAmount(amount - amountToSave);
                    transactionRepository.save(transaction);
                }
            } else if (type == TransactionType.INCOME && goalId == null){
                transactionRepository.save(transaction);
            }

            if (type == TransactionType.EXPENSE) {
                if (calculateBalance(userId) >= amount) {
                    notificationService.notificationSend(userId, amount);
                    budgetService.addExpense(userId, amount);
                    transactionRepository.save(transaction);
                } else {
                    System.out.println("Недостаточно средств");
                }
            }
        } else {
            System.out.println("Невозможно совершить транзакцию, вы заблокированы");
        }

    }

    public void editTransaction(long userId, long transactionId, double newAmount, Category newCategory, String newDescription) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isPresent() && transactionOpt.get().getUserId() == userId) {
            Transaction transaction = transactionOpt.get();

            if (transaction.getType() == TransactionType.EXPENSE) {
                budgetService.addExpense(userId, newAmount - transaction.getAmount());
            }
            transaction.setAmount(newAmount);
            transaction.setCategory(newCategory);
            transaction.setDescription(newDescription);
            transactionRepository.update(transactionId, transaction);
            System.out.println("Transaction updated successfully.");
        } else {
            System.out.println("Transaction not found or does not belong to the user.");
        }
    }

    public void deleteTransaction(long userId, long transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isPresent() && transactionOpt.get().getUserId() == userId) {
            Transaction transaction = transactionOpt.get();
            if (transaction.getGoalId() != null) {
                System.out.println("Transaction is linked to a goal, it can't be deleted directly.");
                return;
            }

            if (transaction.getType() == TransactionType.EXPENSE) {
                budgetService.addExpense(userId, -transaction.getAmount());
            }
            if (transactionRepository.delete(transactionId)) {
                System.out.println("Transaction deleted successfully.");
            } else {
                System.out.println("Transaction not found.");
            }
        } else {
            System.out.println("Transaction not found or does not belong to the user.");
        }
    }

    public void viewAllTransactions(long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        if (transactions.isEmpty()) {
            System.out.println("No transactions available for user.");
        } else {
            for (Transaction transaction : transactions) {
                System.out.println(transaction.getId() + ": " + transaction.getAmount() + " " + transaction.getType() + " (" + transaction.getCategory() + ")");
            }
        }
    }

    public double calculateBalance(long userId) {
        double totalIncome = transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return totalIncome - totalExpense;
    }

    public double calculateIncomeForPeriod(long userId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> (transaction.getDate().isEqual(start) || transaction.getDate().isAfter(start)) &&
                        (transaction.getDate().isEqual(end) || transaction.getDate().isBefore(end)) &&
                        transaction.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateExpenseForPeriod(long userId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> (transaction.getDate().isEqual(start) || transaction.getDate().isAfter(start)) &&
                        (transaction.getDate().isEqual(end) || transaction.getDate().isBefore(end)) &&
                        transaction.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateExpensesByCategory(long userId, Category category) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getCategory() == category && transaction.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }


    public double calculateTotalIncome(long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotalExpense(long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}