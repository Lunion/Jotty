package com.lawenlerk.jotcash;

import java.util.Calendar;

/**
 * Created by enlerklaw on 2/25/14.
 */
public class Transaction {
    // Private variables
    public int id;
    public Calendar timeCreated;
    public double amount;
    int transactionType;
    public Calendar date;
    public String category;
    public String description;

    public static int EXPENSE = 1;
    public static int INCOME = 2;

    public Transaction() {
        timeCreated = Calendar.getInstance();
        amount = 0;
        transactionType = Transaction.EXPENSE;
        date = Calendar.getInstance();
        category = "";
        description = "";
    }

    public Transaction(double amount, int transactionType, Calendar date, String category, String description) {
        this.timeCreated = Calendar.getInstance();

        this.amount = amount;
        this.transactionType = transactionType;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    public boolean isComplete() {
        if (timeCreated == null || amount < 0 || transactionType == -1 || date == null || category == null) {
            return false;
        } else {
            return true;
        }
    }

}
