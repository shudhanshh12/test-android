package in.okcredit.ui.app_lock.preference;

import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;

public interface AppLockPrefContract {
    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {}

    interface View extends MVP.View, BaseContracts.Online.View, BaseContracts.Authenticated.View {
        void setAppLockStatus(boolean isActive);
    }
}
