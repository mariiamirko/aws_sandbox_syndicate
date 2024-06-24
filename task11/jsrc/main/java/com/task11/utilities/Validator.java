package com.task11.utilities;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@UtilityClass
public class Validator {
    public static String validateDateFormat(String toValidate) throws RuntimeException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            dateFormat.parse(toValidate);
        } catch (ParseException e) {
            throw new RuntimeException("400");
        }
        return toValidate;
    }

    public static String validateTimeFormat(final String toValidate) throws RuntimeException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setLenient(false);

        try {
            dateFormat.parse(toValidate);
        } catch (ParseException e) {
            throw new RuntimeException("400");
        }
        return toValidate;
    }
}
