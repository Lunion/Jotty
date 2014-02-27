package com.lawenlerk.jotcash;

import java.util.Calendar;

/**
 * Created by enlerklaw on 2/25/14.
 */
public class Transaction {
    // Private variables
    int id;
    float amount;
    String type;
    Calendar date;
    String category;

    public Transaction() {

    }

    public Transaction(int id, float amount, String type, Calendar date, String category) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.category = category;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
