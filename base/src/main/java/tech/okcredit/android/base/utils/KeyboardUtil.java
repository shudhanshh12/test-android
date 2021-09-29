package tech.okcredit.android.base.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import kotlin.Deprecated;
import timber.log.Timber;

@Deprecated(message = "Use activity/fragment extension instead")
public class KeyboardUtil {
    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            try {
                View focusedView = activity.getCurrentFocus();
                if (focusedView == null) focusedView = new View(activity);

                InputMethodManager inputManager =
                        (InputMethodManager)
                                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                focusedView.clearFocus();
            } catch (Exception e) {
                Timber.e(e, "Exception while closing keyboard");
            }
        }
    }

    public static void hideKeyboard(@NonNull Fragment fragment) {
        try {
            InputMethodManager inputManager =
                    (InputMethodManager)
                            fragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    fragment.getView().getRootView().getWindowToken(), 0);
        } catch (Exception e) {
            Timber.e(e, "Exception while closing keyboard");
        }
    }

    public static void hideKeyboard(Context context, View view) {
        try {
            InputMethodManager inputManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
        } catch (Exception e) {
            Timber.e(e, "Exception while closing keyboard");
        }
    }

    public static void showKeyboard(@NonNull Context context, View view) {
        if (context != null) {
            try {
                InputMethodManager inputManager =
                        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                view.requestFocus();
            } catch (Exception e) {
                Timber.e(e, "Exception while showing keyboard");
            }
        }
    }

    public static void showKeyboardImplicit(@NonNull Context context, View view) {
        try {
            InputMethodManager inputManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            view.requestFocus();
        } catch (Exception e) {
            Timber.e(e, "Exception while showing keyboard");
        }
    }
}
