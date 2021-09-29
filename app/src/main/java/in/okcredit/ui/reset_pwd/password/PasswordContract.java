package in.okcredit.ui.reset_pwd.password;

import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;

public interface PasswordContract {
    interface View extends MVP.View, BaseContracts.Online.View, BaseContracts.Loading.View {
        void showInvalidPasswordError();

        void gotoHomeScreen(String requestedScreen);
    }

    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Loading.Presenter<View> {
        void resetPassword(String newPassword);
    }
}
