package com.lunion.jotty;

import java.util.Calendar;

/**
 * Created by enlerklaw on 2/25/14.
 */
public class Transaction {
    public static final String EXPENSE = "EXPENSE";
    public static final String INCOME = "INCOME";
    // Private variables
    public int id;
    public Calendar timeCreated;
    public double amount;
    public String type;
    public Calendar date;
    public String category;
    public String description;

    public Transaction() {
        timeCreated = Calendar.getInstance();
        amount = 0;
        type = Transaction.EXPENSE;
        date = Calendar.getInstance();
        category = "";
        description = "";
    }

    public Transaction(double amount, String type, Calendar date, String category, String description) {
        this.timeCreated = Calendar.getInstance();

        this.amount = amount;
        this.type = type;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    public boolean isComplete() {
        if (timeCreated == null || amount < 0 || type == null || date == null || category == null) {
            return false;
        } else {
            return true;
        }
    }

}
