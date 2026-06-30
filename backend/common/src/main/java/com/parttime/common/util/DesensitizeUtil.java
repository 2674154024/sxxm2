package com.parttime.common.util;

public class DesensitizeUtil {

    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 4) + "********" + idCard.substring(12);
    }
}