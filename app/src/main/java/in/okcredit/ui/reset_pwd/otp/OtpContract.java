package in.okcredit.ui.reset_pwd.otp;

import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;

public interface OtpContract {
    interface View extends MVP.View, BaseContracts.Online.View, BaseContracts.Loading.View {
        void setMobile(String mobile);

        void showIncorrectOtpError();

        void gotoPasswordScreen();

        void showExpiredOtpError();

        void showAutoreadLoading();

        void setTimer(long secondsRemaining);

        void showVerificationLoading();

        void showVerificationSuccess();

        void hideResendButton();

        void showResendButton();
    }

    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Loading.Presenter<View> {
        void verifyOtp(String otp);

        void reSendOtp();
    }
}
