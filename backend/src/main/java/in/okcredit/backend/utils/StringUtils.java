package in.okcredit.backend.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import in.okcredit.backend.contract.Constants;

public abstract class StringUtils {

    public static final String SINGLE_SPACE = " ";

    public static int findLastIndexOfDigit(final String text) {
        if (text == null) {
            return -1;
        }
        int lastIndex = -1;
        for (int i = text.length(); i != 0; i--) {
            String lastChar = text.substring(i - 1);
            if (Character.isDigit(lastChar.toCharArray()[0])) {
                lastIndex = i;
                break;
            }
        }
        return lastIndex;
    }

    @Nullable
    public static CharSequence quote(@NotNull String result) {
        return "\"" + " " + result + " " + "\"";
    }

    @NotNull
    public static String generateDeepLinkForCustomer(@Nullable String customerId) {
        return Constants.DEEPLINK_BASE_URL + "/customer/" + customerId;
    }
}
