package in.okcredit.ui.customer_profile;

import in.okcredit.backend.contract.Customer;
import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.ui._base_v2.BaseContracts;
import java.io.File;

public interface CustomerProfileContract {
    interface View extends BaseContracts.Online.View, BaseContracts.Authenticated.View {
        void setName(String customerName);

        void setInputFields(Customer customer);

        void setMobile(String mobile);

        void gotoDeleteScreen(String customerId);

        void openCamera();

        void openGallery();

        void setAddress(String address);

        void showNameLoading();

        void hideNameLoading();

        void showAddressLoading();

        void hideAddressLoading();

        void displayInvalidNameError();

        void displayInvalidAddressError();

        void showMobileLoading();

        void hideMobileLoading();

        void onMobileConflict(Customer conflict);

        void showActiveCyclicAccount(Supplier info);

        void showDeletedCyclicAccount(Supplier info);

        void displayInvalidMobileError();

        void displayProfileImageFile(File profileImage);

        void setProfileImageLocal(File localFile);

        void setProfileImageRemote(String url);

        void showSmsLangPopup(String language);

        void showSmsInitialPopup(boolean isChecked);

        void setCustomerPref(String lang, boolean alertEnable, String reminderMode, String mobile);

        void setCustomer(Customer customer);

        void showLoader();

        void hideLoader();

        void supplierCreditEnabledCustomer(boolean isSupplierCreditEnabledCustomer);

        void showBlockRelationShipDialog(String type, Customer customer);

        void setBlockField(boolean supplierCreditEnabledCustomer, Customer customer);

        void setOldPermissionState(boolean permissionCheck);

        void supplierAddTransactionRestriction(
                Boolean supplierCreditEnabledCustomer, Customer customer);

        void goToCustomerScreen();

        void setBlockState(boolean isBlocked);

        void openAddNumberPopup();

        void isAddTransactionRestricted(Boolean isAddTransactionRestricted);

        void setUnseenAndTotalBillsCount(Integer total, Integer newBill);

        void isBillFeatureEnabled(Boolean result);

        void isNewBillFeatureEnabled(Boolean result);
    }

    interface Presenter
            extends BaseContracts.Authenticated.Presenter<View>,
                    BaseContracts.Online.Presenter<View> {

        void onDeleteClicked();

        void onCameraClicked();

        void onGalleryClicked();

        void saveName(String description);

        void saveAddress(String address);

        void onEditMobileClicked();

        void setProfileImage(boolean isCamera, File profileImage);

        void saveMobile(String s);

        void onSmsLanguageClicked();

        void updateCustomerTxAlertLanguage(String language, String updatedLang);

        void txSmsSwitchChanged(boolean checked, boolean isFromPopup);

        void onReminderModeSelected(String sms);

        void addTransactionPermissionSwitchChanged(boolean checked);

        void blockTransaction(Customer.State state);

        void onBlockClicked();
    }
}
