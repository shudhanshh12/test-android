package in.okcredit.ui._base_v2;

import in.okcredit.shared._base_v2.MVP;

public interface BaseContracts {
    /**
     * ************************************************************** ONLINE
     * **************************************************************
     */
    interface Online {
        interface View extends MVP.View {
            void showNoInternetMessage();
        }

        interface Presenter<V extends View> extends MVP.Presenter<V> {
            void onInternetRestored();
        }
    }

    /**
     * ************************************************************** AUTHENTICATED
     * **************************************************************
     */
    interface Authenticated {
        interface View extends MVP.View {
            void gotoLogin();
        }

        interface Presenter<V extends View> extends MVP.Presenter<V> {
            void onAuthenticationRestored();
        }
    }

    /**
     * ************************************************************** LOADING
     * **************************************************************
     */
    interface Loading {
        interface View extends MVP.View {
            void showLoading();

            void hideLoading();
        }

        interface Presenter<V extends View> extends MVP.Presenter<V> {}
    }
}
