package tech.okcredit.android.base.utils.keyboardUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.fragment.app.FragmentActivity;
import kotlin.Deprecated;

@Deprecated(message = "Use activity/fragment extension instead")
public class KeyboardVisibilityEvent {

    private static final double KEYBOARD_MIN_HEIGHT_RATIO = 0.15;

    /**
     * Set keyboard visibility change event listener. This automatically remove registered event
     * listener when the Activity is destroyed
     *
     * @param activity Activity
     * @param listener KeyboardVisibilityEventListener
     */
    public static Unregistrar setEventListener(
            final Activity activity, final KeyboardVisibilityEventListener listener) {

        final Unregistrar unregistrar = registerEventListener(activity, listener);
        activity.getApplication()
                .registerActivityLifecycleCallbacks(
                        new AutoActivityLifecycleCallback(activity) {
                            @Override
                            protected void onTargetActivityDestroyed() {
                                unregistrar.unregister();
                            }
                        });
        return unregistrar;
    }

    /**
     * Set keyboard visibility change event listener.
     *
     * @param activity Activity
     * @param listener KeyboardVisibilityEventListener
     * @return Unregistrar
     */
    public static Unregistrar registerEventListener(
            final Activity activity, final KeyboardVisibilityEventListener listener) {

        if (activity == null) {
            throw new NullPointerException("Parameter:activity must not be null");
        }

        int softInputAdjust =
                activity.getWindow().getAttributes().softInputMode
                        & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;

        // fix for #37 and #38.
        // The window will not be resized in case of SOFT_INPUT_ADJUST_NOTHING
        if ((softInputAdjust & WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) {
            throw new IllegalArgumentException(
                    "Parameter:activity window SoftInputMethod is SOFT_INPUT_ADJUST_NOTHING. In this case window will not be resized");
        }

        if (listener == null) {
            throw new NullPointerException("Parameter:listener must not be null");
        }

        final View activityRoot = getActivityRoot(activity);

        final ViewTreeObserver.OnGlobalLayoutListener layoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    private final Rect r = new Rect();

                    private boolean wasOpened = false;

                    @Override
                    public void onGlobalLayout() {
                        try {
                            activityRoot.getWindowVisibleDisplayFrame(r);

                            int screenHeight = activityRoot.getRootView().getHeight();
                            int heightDiff = screenHeight - r.height();

                            boolean isOpen = heightDiff > screenHeight * KEYBOARD_MIN_HEIGHT_RATIO;

                            if (isOpen == wasOpened) {
                                // keyboard state has not changed
                                return;
                            }

                            wasOpened = isOpen;

                            listener.onVisibilityChanged(isOpen);
                        } catch (Exception e) {
                        }
                    }
                };
        activityRoot.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        return new SimpleUnregistrar(activity, layoutListener);
    }

    /**
     * Determine if keyboard is visible
     *
     * @param activity Activity
     * @return Whether keyboard is visible or not
     */
    public static boolean isKeyboardVisible(Activity activity) {
        Rect r = new Rect();

        View activityRoot = getActivityRoot(activity);

        activityRoot.getWindowVisibleDisplayFrame(r);

        int screenHeight = activityRoot.getRootView().getHeight();
        int heightDiff = screenHeight - r.height();

        return heightDiff > screenHeight * KEYBOARD_MIN_HEIGHT_RATIO;
    }

    static View getActivityRoot(Activity activity) {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    public static void showKeyboard(Context context, EditText target, View rootView) {
        if (context == null) {
            return;
        }
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.toggleSoftInputFromWindow(
                        rootView.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                if (target != null) {
                    target.requestFocus();
                }
            }
        } catch (Exception e) {

        }
    }

    public static void hideKeyboard(Context context, View target) {
        if (context == null || target == null) {
            return;
        }

        InputMethodManager imm = getInputMethodManager(context);
        imm.hideSoftInputFromWindow(target.getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity activity) {
        try {
            View view = activity.getWindow().getDecorView();

            if (view != null) {
                hideKeyboard(activity, view);
            }
        } catch (Exception e) {

        }
    }

    private static InputMethodManager getInputMethodManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static void stateHiddentWindowSoftInputMode(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null) return;
        fragmentActivity
                .getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
