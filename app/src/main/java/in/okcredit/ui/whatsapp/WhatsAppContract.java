package in.okcredit.ui.whatsapp;

import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;

public interface WhatsAppContract {
    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Online.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View> {
        void onWhatsAppEnableClicked();

        void onWhatsAppDisableClicked();

        void onWhatsappUsClicked(boolean b);
    }

    interface View extends MVP.View, BaseContracts.Online.View, BaseContracts.Authenticated.View {

        void setMobile(String mobile);

        void goToHomeScreen();

        void hideDisableButton();

        void goToHelpScreen();

        void openWhatsapp(String helpNumber);
    }
}
