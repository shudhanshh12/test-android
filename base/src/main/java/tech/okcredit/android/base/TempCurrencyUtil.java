package tech.okcredit.android.base;

import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import tech.okcredit.android.base.language.LocaleManager;

// TODO: Remove once Refactoring of Utils
public class TempCurrencyUtil {

    public static void renderV2(long amount, TextView textView, Boolean isPayment) {

        @ColorRes int color = R.color.tx_payment;
        if (amount < 0L) {
            color = R.color.red_primary;
        } else if (!isPayment) {
            color = R.color.red_primary;
        }

        textView.setText(String.format("₹%s", formatV2(amount)));
        textView.setTextColor(ContextCompat.getColor(textView.getContext(), color));
    }

    public static String formatV2(long amount) {
        if (amount < 0L) {
            amount *= -1;
        }

        long fraction = amount % 100;
        String fractionString;
        if (fraction == 0) {
            fractionString = "";
        } else if (fraction < 10) {
            fractionString = ".0" + fraction;
        } else {
            fractionString = "." + fraction;
        }
        amount = amount / 100;

        if (amount < 1000) {
            return String.format("%s%s", format("###", amount), fractionString);
        } else {
            double hundreds = amount % 1000;
            int other = (int) (amount / 1000);
            return String.format(
                    "%s,%s%s", format(",##", other), format("000", hundreds), fractionString);
        }
    }

    private static String format(String pattern, Object value) {
        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(LocaleManager.Companion.getEnglishLocale());
        return new DecimalFormat(pattern, symbols).format(value);
    }
}
