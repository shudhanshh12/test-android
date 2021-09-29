package in.okcredit.ui.app_lock.set;

import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;

public interface AppLockContract {
    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {
        void enableAppLock(String pattern);

        void disableAppLock(String oldPatternAttempt);
    }

    interface View extends MVP.View, BaseContracts.Online.View, BaseContracts.Authenticated.View {
        void setAppLockStatus(boolean isActive);

        void goToAppLockPrefActivity(boolean isAppLockEnabled);

        void showIncorrectOldPatternMessage();

        void showPatternMismatchMessage();
    }
}
