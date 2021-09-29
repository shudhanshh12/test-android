package tech.okcredit.android.base.utils;

import android.text.TextUtils;

public abstract class MobileUtils {

    public static String parseMobile(String mobile) {
        if (mobile == null || TextUtils.isEmpty(mobile)) {
            return "";
        }

        // remove all `non digit` characters
        mobile = mobile.replaceAll("\\D+", "");

        // remove leading zeros
        mobile = mobile.replaceFirst("^0+(?!$)", "");

        // remove `91` if number of digits is 12
        if (mobile.length() == 12 && mobile.startsWith("91")) {
            mobile = mobile.substring(2);
        }

        // mobile number has to be `10` digits long
        if (mobile.length() != 10) {
            return "";
        }

        // mobile number should start with `5, 6, 7, 8, or 9`
        if (mobile.substring(0, 1).matches("[5-9]")) {
            return mobile;
        } else {
            return "";
        }
    }

    public static String normalize(String mobile) {
        if (mobile == null || TextUtils.isEmpty(mobile)) {
            return "";
        }
        mobile = mobile.replaceAll("\\s", "");
        mobile = mobile.trim().replace(" ", "").replace("-", "");
        if (!mobile.isEmpty() && mobile.charAt(0) == '0') {
            return mobile.substring(1);
        } else if (mobile.startsWith("+91")) {
            return mobile.replace("+91", "");
        } else {
            mobile = mobile.trim().replace("+", "");
            if (mobile.length() >= 10) {
                mobile = mobile.substring(mobile.length() - 10);
                if (mobile.matches("([0-9]{10})")) {
                    return mobile;
                }
            }
            return "";
        }
    }

    public static boolean isPhoneNumberValid(String phoneNumber) {
        return !parseMobile(phoneNumber).equalsIgnoreCase("");
    }
}
