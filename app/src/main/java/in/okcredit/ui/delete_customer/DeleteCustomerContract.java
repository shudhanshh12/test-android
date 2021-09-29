package in.okcredit.ui.delete_customer;

import in.okcredit.backend.contract.Customer;
import in.okcredit.ui._base_v2.BaseContracts;

public interface DeleteCustomerContract {
    interface View
            extends BaseContracts.Loading.View,
                    BaseContracts.Online.View,
                    BaseContracts.Authenticated.View {
        void setCustomer(Customer customer);

        void gotoAddTxnScreen(String customerId, int type, long amount);

        void gotoHomeScreen();

        void showMessageWithRetry();

        void hideLoadingOnly();

        void goToResetPasswordScreen();

        void deleteCustomer();

        void showUpdatePinDialog();
    }

    interface Presenter
            extends BaseContracts.Loading.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {
        void delete();

        void settle();

        void onRetryClicked();

        void checkIsPasswordSet();
    }
}
