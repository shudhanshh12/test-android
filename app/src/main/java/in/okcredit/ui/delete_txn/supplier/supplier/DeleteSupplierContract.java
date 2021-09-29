package in.okcredit.ui.delete_txn.supplier.supplier;

import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;

public interface DeleteSupplierContract {
    interface View
            extends MVP.View,
                    BaseContracts.Loading.View,
                    BaseContracts.Online.View,
                    BaseContracts.Authenticated.View {
        void setCustomer(Supplier supplier);

        void gotoAddTxnScreen(String customerId, int type, long amount);

        void gotoHomeScreen();

        void showIncorrectPasswordError();

    }

    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Loading.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {
        void delete();

        void settle();

        void getSupplierAmount();
    }
}
