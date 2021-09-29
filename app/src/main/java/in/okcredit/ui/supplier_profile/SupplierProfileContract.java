package in.okcredit.ui.supplier_profile;

import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;
import java.io.File;

public interface SupplierProfileContract {
    interface View extends MVP.View, BaseContracts.Online.View, BaseContracts.Authenticated.View {
        void setName(String customerName);

        void setMobile(String mobile);

        void gotoDeleteScreen(String supplierId);

        void openCamera();

        void openGallery();

        void showMobileEditBox();

        void setAddress(String address);

        void showNameLoading();

        void hideNameLoading();

        void showAddressLoading();

        void showUpdatePinDialog();

        void hideAddressLoading();

        void displayInvalidNameError();

        void displayInvalidAddressError();

        void showMobileLoading();

        void hideMobileLoading();

        void showActiveCyclicAccount(SupplierCreditServerErrors.Error info);

        void onMobileConflict(Supplier conflict);

        void displayInvalidMobileError();

        void displayProfileImageFile(File profileImage);

        void setProfileImageLocal(File localFile);

        void setProfileImageRemote(String url);

        void showSmsLangPopup(String language, String cusId);

        void showSmsInitialPopup(boolean isChecked);

        void setSupplierPref(String lang, boolean alertEnable);

        void goToResetPasswordScreen(String mobile);

        void showDeletePasswordLoader();

        void hideDeletePasswordLoader();

        void setSupplier(Supplier supplier);

        void showTransactionRestrictionDeletion(Supplier supplier);

        void showBlockRelationShipDialog(
                String type, String name, String number, String profileImg);

        void setBlockField(boolean isBlocked);

        void goToSupplierScreen();

        void setUnseenAndTotalBillsCount(Integer total, Integer newBill);

        void isBillFeatureEnabled(Boolean result);

        void isNewBillFeatureEnabled(Boolean result);

        void goToHomeWithClearStack();
    }

    interface Presenter
            extends MVP.Presenter<View>,
                    BaseContracts.Authenticated.Presenter<View>,
                    BaseContracts.Online.Presenter<View> {

        void onDeleteClicked();

        void onCameraClicked();

        void onGalleryClicked();

        void saveName(String description);

        void saveAddress(String address);

        void onEditMobileClicked();

        void setProfileImage(boolean isCameraImage, File profileImage);

        void saveMobile(String s);

        void onSmsLanguageClicked();

        void updateSupplierTxAlertLanguage(String language, String updatedLang);

        void txSmsSwitchChanged(boolean checked, boolean isFromPopup);

        void onReminderModeSelected(String sms);

        void blockTransaction(int state);

        void onBlockClicked();

        void onEditNameClicked();

        void onEditAddressClicked();
    }
}
