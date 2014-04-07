package com.lawenlerk.jotty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by EnLerk on 3/15/14.
 */
public abstract class Utilities {
    private static String currencyString = "$"; // TODO extract this into settings

    public static Date parseDate(String dateString, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDate(Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }


    public static String addCurrencyString(String amountString) {
        return currencyString + " " + amountString;
    }

    public static String getCurrencyString() {
        return currencyString;
    }

    public static String toDecimals(Double amount, int decimals) {
        // Round to x number of decimals first before performing padding. Else, if input has more than x amount of decimals it will not be cut down to x decimals
        amount = Math.round(amount * Math.pow(10, (double) decimals)) / Math.pow(10, (double) decimals);
        String amountString = Double.toString(amount);
        for (int i = amountString.length() - (amountString.indexOf('.') + 1); i < decimals; i++) {
            amountString = amountString + '0';
        }
        return amountString;
    }

    public static int dateToInt(Date date) {
        // Example: 2014-03-16 becomes
        // 2014 * 10000 + 3 * 100 + 16
        // = 20140000 + 300 + 16
        // = 20140316

        int key = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        key += calendar.get(Calendar.YEAR) * 10000;
        key += (calendar.get(Calendar.MONTH) + 1) * 100;
        key += calendar.get(Calendar.DAY_OF_MONTH);

        return key;
    }

    public static String formatCurrency(double total) {
        return addCurrencyString(toDecimals(total, 2));
    }
}
