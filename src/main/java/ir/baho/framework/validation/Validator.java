package ir.baho.framework.validation;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class Validator {

    private Validator() {
    }

    public static boolean isValid(String pattern, String matcher) {
        Pattern pat = Pattern.compile(pattern);
        Matcher match = pat.matcher(matcher);
        return match.matches();
    }

    public static <T> boolean isValid(T t) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            jakarta.validation.Validator validator = factory.getValidator();
            return validator.validate(t).size() == 0;
        }
    }

    public static boolean isNumber(String value) {
        return value.matches("[-|+]?(\\p{N})+");
    }

    public static boolean isCharacter(String value) {
        return value.matches("(\\p{L})+");
    }

    public static boolean isCharacterOrNumber(String value) {
        return value.matches("(\\p{L}|\\p{N})+");
    }

    public static boolean isEmail(String email) {
        return isValid("^([0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*@([0-9a-zA-Z][-\\w]*[0-9a-zA-Z]\\.)+[a-zA-Z]{2,9})$", email);
    }

    public static boolean isDomain(String domain) {
        return isValid("^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$", domain);
    }

    public static boolean isUrl(String url) {
        return isValid("^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$", url);
    }

    public static boolean isTel(String tel) {
        return isValid("(0|\\+98|0098)+[1-8]{1}+[0-9]{9}", tel);
    }

    public static boolean isCell(String cell) {
        return isValid("((09)|(\\+989)|(00989))+[0-9]{9}", cell);
    }

    public static boolean isTelOrCell(String number) {
        return isValid("((0)|(\\+98)|(0098))+[1-9]{1}+[0-9]{9}", number);
    }

    public static boolean isPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return false;
        }
        if (postalCode.length() != 10) {
            return false;
        }
        for (int i = 0; i < postalCode.length(); i++) {
            if (!Character.isDigit(postalCode.charAt(i))) {
                return false;
            }
        }
        if (postalCode.equals("0000000000") || postalCode.equals("1111111111")
                || postalCode.equals("2222222222") || postalCode.equals("3333333333")
                || postalCode.equals("4444444444") || postalCode.equals("5555555555")
                || postalCode.equals("6666666666") || postalCode.equals("7777777777")
                || postalCode.equals("8888888888") || postalCode.equals("9999999999")) {
            return false;
        }
        return !postalCode.contains("0") && !postalCode.contains("2") &&
                Character.getNumericValue(postalCode.charAt(4)) != 5 && isUnique(postalCode.substring(0, 4));
    }

    public static boolean isImei(String imei) {
        if (imei == null || imei.isBlank()) {
            return false;
        }
        if (imei.length() != 15) {
            return false;
        }
        for (int i = 0; i < imei.length(); i++) {
            if (!Character.isDigit(imei.charAt(i))) {
                return false;
            }
        }
        int sum = 0;
        for (int i = 0; i < 14; i++) {
            int digit = Character.getNumericValue(imei.charAt(i));
            if (i % 2 == 0) {
                sum += digit;
            } else {
                int doubled = digit * 2;
                sum += (doubled % 10) + (doubled / 10);
            }
        }
        int checksum = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(imei.charAt(imei.length() - 1));
        return checksum == lastDigit;
    }

    public static boolean isNationalId(String nationalId) {
        if (nationalId == null || nationalId.isBlank()) {
            return false;
        }
        if (nationalId.length() != 10) {
            return false;
        }
        for (int i = 0; i < nationalId.length(); i++) {
            if (!Character.isDigit(nationalId.charAt(i))) {
                return false;
            }
        }
        if (nationalId.equals("0000000000") || nationalId.equals("1111111111")
                || nationalId.equals("2222222222") || nationalId.equals("3333333333")
                || nationalId.equals("4444444444") || nationalId.equals("5555555555")
                || nationalId.equals("6666666666") || nationalId.equals("7777777777")
                || nationalId.equals("8888888888") || nationalId.equals("9999999999")) {
            return false;
        }
        int sum = IntStream.range(0, nationalId.length() - 1).map(i -> (Byte.parseByte(String.valueOf(nationalId.charAt(i))) * (10 - i))).sum();
        return (nationalId.charAt(9) == String.valueOf(((sum % 11) >= 2) ? (11 - (sum % 11)) : (sum % 11)).charAt(0));
    }

    public static boolean isCompanyId(String companyId) {
        if (companyId == null || companyId.isBlank()) {
            return false;
        }
        if (companyId.length() != 11) {
            return false;
        }
        for (int i = 0; i < companyId.length(); i++) {
            if (!Character.isDigit(companyId.charAt(i))) {
                return false;
            }
        }
        if (companyId.equals("00000000000") || companyId.equals("11111111111")
                || companyId.equals("22222222222") || companyId.equals("33333333333")
                || companyId.equals("44444444444") || companyId.equals("55555555555")
                || companyId.equals("66666666666") || companyId.equals("77777777777")
                || companyId.equals("88888888888") || companyId.equals("99999999999")) {
            return false;
        }
        char[] chars = companyId.toCharArray();
        int control = Character.getNumericValue(chars[10]);
        int[] j = {29, 27, 23, 19, 17};
        int factor = Character.getNumericValue(chars[9]) + 2;
        return IntStream.range(0, 10).map(i -> factor + Character.getNumericValue(chars[i]) * j[i % 5]).sum() % 11 == control;
    }

    public static boolean isUsername(String username) {
        return isValid("^[a-zA-Z0-9_-]{4,64}$", username);
    }

    public static boolean isPassword(String password) {
        return isValid("^[a-zA-Z0-9@#$%_-]{4,64}$", password);
    }

    public static boolean isIp(String ip) {
        return isValid("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$", ip);
    }

    public static boolean isMacAddress(String macAddress) {
        return isValid("^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$", macAddress);
    }

    public static boolean isHexadecimal(String value) {
        return isValid("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", value);
    }

    public static boolean isISBN(String isbn) {
        return isValid("ISBN(-1(?:(0)|3))?:?\\x20+(?(1)(?(2)(?:(?=.{13}$)\\d{1,5}([ -])\\d{1,7}\\3\\d{1,6}\\3(?:\\d|x)$)|(?:(?=.{17}$)97(?:8|9)([ -])\\d{1,5}\\4\\d{1,7}\\4\\d{1,6}\\4\\d$))|(?(.{13}$)(?:\\d{1,5}([ -])\\d{1,7}\\5\\d{1,6}\\5(?:\\d|x)$)|(?:(?=.{17}$)97(?:8|9)([ -])\\d{1,5}\\6\\d{1,7}\\6\\d{1,6}\\6\\d$)))", isbn);
    }

    public static boolean isTime(String time) {
        return isValid("(((([0-1]{1}[0-9]{1})|(2[0-3]{1})|([0-9]{1}))|((([0-1]{1}[0-9]{1})|(2[0-3]{1})|([0-9]{1}))((:(([0-5]{1}[0-9]{1})|([0-9]{1})):(([0-5]{1}[0-9]{1})|([0-9]{1})))|(:(([0-5]{1}[0-9]{1})|([0-9]{1}))))))|(((([0-1]{1}[0-9]{1})|(2[0-3]{1}))|((([0-1]{1}[0-9]{1})|(2[0-3]{1}))(([0-5]{1}[0-9]{1}[0-5]{1}[0-9]{1})|([0-5]{1}[0-9]{1}))))))", time);
    }

    private static boolean isUnique(String str) {
        for (char c : str.toCharArray()) {
            if (str.indexOf(c) != str.lastIndexOf(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAngle(Number value) {
        double latitude = value.doubleValue();
        return latitude >= 0 && latitude <= 360;
    }

    public static boolean isAltitude(Number value) {
        double latitude = value.doubleValue();
        return latitude >= 0 && latitude <= 8848;
    }

    public static boolean isLatitude(Number value) {
        double latitude = value.doubleValue();
        return latitude >= -90 && latitude <= 90;
    }

    public static boolean isLongitude(Number value) {
        double longitude = value.doubleValue();
        return longitude >= -180 && longitude <= 180;
    }

}
