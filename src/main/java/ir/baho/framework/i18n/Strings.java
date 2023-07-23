package ir.baho.framework.i18n;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import lombok.SneakyThrows;

import java.util.Locale;

public class Strings {

    private Strings() {
    }

    @SneakyThrows
    public static String getText(String value, Locale locale) {
        if (locale != null && locale.getLanguage().startsWith("fa")) {
            return new ArabicShaping(ArabicShaping.DIGITS_EN2AN + ArabicShaping.DIGIT_TYPE_AN_EXTENDED).shape(String.valueOf(value));
        }
        return value;
    }

    public static String spell(long value, Locale locale) {
        if (value > 999999999999L) {
            throw new NumberFormatException("Value is too large");
        }
        NumberFormat formatter = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);
        return formatter.format(value);
    }

}
