package com.siddhantkushwaha.raven.common.utility;

public class FormValidation {

    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PASS_REGEX  = "^[\\s\\S]{8,}$";
    private static final String NAME_REGEX  = "^[\\s\\S]{3,}$";

    public static boolean isEmailAddress(String text) {

        return text.matches(EMAIL_REGEX);
    }

    public static boolean isPassword(String text) {

        return text.matches(PASS_REGEX);
    }

    public static boolean isName(String text) {

        return text.matches(NAME_REGEX);
    }
}
