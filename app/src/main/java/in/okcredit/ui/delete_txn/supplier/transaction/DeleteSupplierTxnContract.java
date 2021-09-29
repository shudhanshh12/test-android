package in.okcredit.ui.delete_txn.supplier.transaction;

import in.okcredit.merchant.suppliercredit.Transaction;
import in.okcredit.ui._base_v2.BaseContracts;

public interface DeleteSupplierTxnContract {
    interface View
            extends BaseContracts.Loading.View,
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
            extends BaseContracts.Loading.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {
        void delete(String transactionId);

        void checkPasswordSet();
    }
}
