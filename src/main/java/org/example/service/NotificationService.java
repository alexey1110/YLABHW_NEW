package org.example.service;

public class NotificationService {
    private final BudgetService budgetService;

    private final GoalService goalService;

    public NotificationService(BudgetService budgetService, GoalService goalService) {
        this.budgetService = budgetService;
        this.goalService = goalService;
    }

    public boolean notificationSend(long userId, double amount){
        if(budgetService.isBudgetExceeded(userId, amount)){
            System.out.println("Send Email for User: " + userId + ". Your monthLimit is exceeded.");
            return false;
        }
        return true;
    }
}
