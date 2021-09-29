package in.okcredit.ui.delete_txn;

import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;
import merchant.okcredit.accounting.model.Transaction;

public interface DeleteTxnContract {
    interface View
            extends MVP.View,
                    BaseContracts.Loading.View,
                    BaseContracts.Online.View,
                    BaseContracts.Authenticated.View {
        void setTransaction(Transaction transaction);

        void goToCustomerScreen();

        void goToAuthScreen();

        void goToSetNewPinScreen();

        void showDeleteLoading();

        void showUpdatePinDialog();

        void hideDeleteLoading();
    }

    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Loading.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {
        void delete(String transactionId);

        void checkPasswordSet();

        void deleteDiscount(String transactionId);
    }
}
