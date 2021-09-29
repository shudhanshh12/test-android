package in.okcredit.sales_ui.utils;

import android.text.InputFilter;
import android.text.Spanned;

// restricts user input strictly to 2 decimal places
public class DecimalDigitsInputFilter implements InputFilter {
    private static final char DOT = '.';
    private int digitsAfterZero;

    public DecimalDigitsInputFilter() {
        this(2);
    }

    public DecimalDigitsInputFilter(int digitsAfterZero) {
        this.digitsAfterZero = digitsAfterZero;
    }

    @Override
    public CharSequence filter(
            CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String t = dest.toString();
        String[] arr = t.split("\\.");
        if (arr != null && arr.length == 2) {
            int pos = t.indexOf(DOT);
            if (arr[1].length() >= digitsAfterZero && dstart > pos) {
                return "";
            }
        }
        return null;
    }
}
