package com.lawenlerk.jotcash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by EnLerk on 3/15/14.
 */
public abstract class Utilities {
    public static Date parseDate(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.parse(dateString);
    }

    public static String formatDate(Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

//    public static String formatCurrency(String amountString) {
//
//        return addCurrencyString(padZeroes(amountString, 2));
//    }

    public static String addCurrencyString(String amountString) {
        String currencyString = "$"; // TODO extract this into settings

        return currencyString + " " + amountString;
    }

    public static String padZeroes(Double amount, int decimals) {
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
        return addCurrencyString(padZeroes(total, 2));
    }
}
