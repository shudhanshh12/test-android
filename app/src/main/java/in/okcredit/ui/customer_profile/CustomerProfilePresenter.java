package in.okcredit.ui.customer_profile;

import android.content.Context;
import android.util.Pair;
import android.widget.Toast;

import com.google.common.base.Strings;

import dagger.Lazy;
import in.okcredit.R;
import in.okcredit.di.UiThread;
import in.okcredit.analytics.Event;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Tracker;
import in.okcredit.backend._offline.database.DueInfoRepo;
import in.okcredit.backend._offline.error.CustomerErrors;
import in.okcredit.backend.contract.Customer;
import in.okcredit.backend._offline.usecase.GetCustomerImpl;
import in.okcredit.backend._offline.usecase.UpdateCustomer;
import in.okcredit.fileupload.usecase.IImageLoader;
import in.okcredit.fileupload.usecase.IImageStorageStatus;
import in.okcredit.fileupload.usecase.IUploadFile;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import in.okcredit.merchant.core.model.Customer.CustomerSyncStatus;
import in.okcredit.merchant.customer_ui.ui.dialogs.BlockRelationShipDialogFragment;
import in.okcredit.merchant.customer_ui.usecase.IsAddTransactionRestricted;
import in.okcredit.merchant.customer_ui.usecase.IsSupplierCreditEnabledCustomer;
import in.okcredit.shared._base_v2.BasePresenter;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.shared.service.keyval.KeyValService;
import in.okcredit.shared.utils.AbFeatures;
import in.okcredit.supplier.usecase.SyncSupplierEnabledCustomerIdsImpl;
import in.okcredit.ui._base_v2.BaseContracts;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

import java.io.File;
import java.util.UUID;

import javax.inject.Inject;

import org.jetbrains.annotations.Nullable;

import tech.okcredit.android.ab.AbRepository;
import tech.okcredit.android.base.extensions.ContextExtensions;
import tech.okcredit.android.base.language.LocaleManager;
import tech.okcredit.android.base.preferences.Scope;
import tech.okcredit.android.base.utils.MobileUtils;
import tech.okcredit.android.base.utils.ThreadUtils;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import tech.okcredit.use_case.GetAccountsTotalBills;
import timber.log.Timber;

import static tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_KEY_SMS_TOGGLE_OFF;

public class CustomerProfilePresenter extends BasePresenter<CustomerProfileContract.View>
        implements CustomerProfileContract.Presenter {
    private final DueInfoRepo dueInfoRepo;
    private final SyncSupplierEnabledCustomerIdsImpl syncSupplierEnabledCustomerIds;

    private final GetCustomerImpl getCustomer;
    private final String customerId;
    private final UpdateCustomer updateCustomer;
    private final IUploadFile uploadFile;
    private final IImageLoader imageLoader;
    private final Context context;
    private final KeyValService keyValService;
    private final GetActiveBusinessId getActiveBusinessId;
    private final Tracker tracker;
    private final IsSupplierCreditEnabledCustomer isSupplierCreditEnabledCustomer;
    private final IsAddTransactionRestricted isAddTransactionRestricted;
    private final Lazy<LocaleManager> localeManager;
    private final Lazy<GetAccountsTotalBills> getAccountsTotalBillsLazy;
    private final Lazy<AbRepository> abLazy;
    private boolean isEditMobile;
    private Customer customer;
    private boolean isProfilePicChanged;
    private String smsLanguage = LocaleManager.LANGUAGE_ENGLISH;

    @Inject
    public CustomerProfilePresenter(
            @UiThread Scheduler uiScheduler,
            GetCustomerImpl getCustomer,
            @ViewModelParam(CustomerProfileActivity.EXTRA_CUSTOMER_ID) String customerId,
            @ViewModelParam(CustomerProfileActivity.IS_EDIT_MOBILE) boolean isEditMobile,
            UpdateCustomer updateCustomer,
            IUploadFile uploadFile,
            IImageLoader imageLoader,
            KeyValService keyValService,
            Context context,
            DueInfoRepo dueInfoRepo,
            IsSupplierCreditEnabledCustomer isSupplierCreditEnabledCustomer,
            Tracker tracker,
            SyncSupplierEnabledCustomerIdsImpl syncSupplierEnabledCustomerIds,
            IsAddTransactionRestricted isAddTransactionRestricted,
            Lazy<GetAccountsTotalBills> getAccountsTotalBillsLazy,
            Lazy<AbRepository> abLazy,
            Lazy<LocaleManager> localeManager,
            GetActiveBusinessId getActiveBusinessId) {
        super(uiScheduler);
        this.getCustomer = getCustomer;
        this.customerId = customerId;
        this.updateCustomer = updateCustomer;
        this.isEditMobile = isEditMobile;
        this.uploadFile = uploadFile;
        this.imageLoader = imageLoader;
        this.context = context;
        this.keyValService = keyValService;
        this.tracker = tracker;
        this.dueInfoRepo = dueInfoRepo;
        this.isSupplierCreditEnabledCustomer = isSupplierCreditEnabledCustomer;
        this.syncSupplierEnabledCustomerIds = syncSupplierEnabledCustomerIds;
        this.isAddTransactionRestricted = isAddTransactionRestricted;
        this.localeManager = localeManager;
        this.getAccountsTotalBillsLazy = getAccountsTotalBillsLazy;
        this.abLazy = abLazy;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    @Override
    protected void loadData() {
        addTask(
                getCustomer
                        .execute(customerId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                customer -> {
                                    this.customer = customer;

                                    ifAttached(view -> view.setName(customer.getDescription()));
                                    ifAttached(view -> view.setMobile(customer.getMobile()));
                                    ifAttached(view -> view.setAddress(customer.getAddress()));
                                    ifAttached(view -> view.setCustomer(customer));
                                    ifAttached(view -> view.setInputFields(customer));

                                    if (isEditMobile) {
                                        ifAttached(
                                                CustomerProfileContract.View::openAddNumberPopup);
                                        isEditMobile = false;
                                    }

                                    smsLanguage = localeManager.get().getLanguage();
                                    boolean alertEnabled = customer.isTxnAlertEnabled();

                                    if (!Strings.isNullOrEmpty(customer.getLang())) {
                                        smsLanguage = customer.getLang();
                                    }
                                    String reminderMode = "whatsapp";
                                    if (!Strings.isNullOrEmpty(customer.getReminderMode())) {
                                        reminderMode = customer.getReminderMode();
                                    }

                                    String finalReminderMode = reminderMode;
                                    ifAttached(
                                            view ->
                                                    view.setCustomerPref(
                                                            smsLanguage,
                                                            alertEnabled,
                                                            finalReminderMode,
                                                            customer.getMobile()));

                                    if (customer.getProfileImage() != null) {
                                        addTask(
                                                imageLoader
                                                        .load(customer.getProfileImage())
                                                        .storage(
                                                                new IImageStorageStatus() {
                                                                    @Override
                                                                    public void onLocalFile(
                                                                            @Nullable File file) {
                                                                        Timber.i("Local Image Url");
                                                                        if (!isProfilePicChanged) {
                                                                            ifAttached(
                                                                                    view ->
                                                                                            view
                                                                                                    .setProfileImageLocal(
                                                                                                            file));
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onRemoteUrl(
                                                                            @Nullable
                                                                                    String
                                                                                    remoteUrl) {
                                                                        Timber.i(
                                                                                "Remote Image Url");
                                                                        if (!isProfilePicChanged) {
                                                                            ifAttached(
                                                                                    view ->
                                                                                            view
                                                                                                    .setProfileImageRemote(
                                                                                                            remoteUrl));
                                                                        }
                                                                    }
                                                                }));
                                    }
                                },
                                throwable -> {
                                    if (isInternetIssue(throwable)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
        addTask(
                isSupplierCreditEnabledCustomer
                        .execute(customerId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                isSupplierCreditEnabledCustomer ->
                                        ifAttached(
                                                view ->
                                                        view.supplierCreditEnabledCustomer(
                                                                isSupplierCreditEnabledCustomer)),
                                throwable -> ifAttached(MVP.View::showError)));

        addTask(
                Observable.combineLatest(
                        isSupplierCreditEnabledCustomer.execute(customerId),
                        getCustomer.execute(customerId),
                        Pair::new)
                        .observeOn(uiScheduler)
                        .subscribe(
                                booleanCustomerPair -> {
                                    ifAttached(
                                            view ->
                                                    view.supplierAddTransactionRestriction(
                                                            booleanCustomerPair.first,
                                                            booleanCustomerPair.second));
                                    ifAttached(
                                            view ->
                                                    view.setBlockField(
                                                            booleanCustomerPair.first,
                                                            booleanCustomerPair.second));
                                },
                                throwable -> ifAttached(MVP.View::showError)));

        addTask(
                isSupplierCreditEnabledCustomer
                        .execute(customerId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                isSupplierCreditEnabledCustomer ->
                                        ifAttached(
                                                view ->
                                                        view.supplierCreditEnabledCustomer(
                                                                isSupplierCreditEnabledCustomer)),
                                throwable -> ifAttached(MVP.View::showError)));

        addTask(
                isAddTransactionRestricted
                        .execute(customerId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                isAddTransactionRestricted ->
                                        ifAttached(
                                                view ->
                                                        view.isAddTransactionRestricted(
                                                                isAddTransactionRestricted)),
                                throwable -> ifAttached(MVP.View::showError)));

        addTask(
                getAccountsTotalBillsLazy
                        .get()
                        .execute(customerId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                result ->
                                        ifAttached(
                                                view ->
                                                        view.setUnseenAndTotalBillsCount(
                                                                result.getTotalCount(),
                                                                result.getUnseenCount())),
                                throwable -> ifAttached(MVP.View::showError)));
        addTask(
                abLazy.get()
                        .isFeatureEnabled(AbFeatures.BILL_MANAGER, false, null)
                        .observeOn(uiScheduler)
                        .subscribe(
                                result -> ifAttached(view -> view.isBillFeatureEnabled(result)),
                                throwable -> ifAttached(view -> view.showError())));
        addTask(
                abLazy.get()
                        .isFeatureEnabled(AbFeatures.NEW_ON_BILL_ICON, false, null)
                        .observeOn(uiScheduler)
                        .subscribe(
                                result -> ifAttached(view -> view.isNewBillFeatureEnabled(result)),
                                throwable -> ifAttached(view -> view.showError())));
    }

    @Override
    public void saveName(String name) {
        if (name != null && name.length() < 1) {
            ifAttached(view -> view.displayInvalidNameError());
            return;
        } else if (name != null && name.equals(customer.getDescription())) {
            ifAttached(view -> view.setName(customer.getDescription()));
            ifAttached(CustomerProfileContract.View::hideNameLoading);
            return;
        }
        ifAttached(CustomerProfileContract.View::showNameLoading);
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                name,
                                customer.getAddress(),
                                customer.getProfileImage(),
                                customer.getMobile(),
                                customer.getLang(),
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                false,
                                false,
                                customer.getState(),
                                false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    tracker.trackUpdateProfileV1(
                                            PropertyValue.CUSTOMER,
                                            PropertyValue.NAME,
                                            customerId);

                                    ifAttached(CustomerProfileContract.View::hideNameLoading);
                                    ifAttached(view -> view.setName(customer.getDescription()));
                                },
                                e -> {
                                    ifAttached(CustomerProfileContract.View::hideNameLoading);
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void saveAddress(String address) {
        if (address != null && address.length() < 1) {
            ifAttached(CustomerProfileContract.View::displayInvalidAddressError);
            return;
        } else if (address != null && address.equals(customer.getAddress())) {
            ifAttached(view -> view.setAddress(customer.getAddress()));
            return;
        }
        ifAttached(CustomerProfileContract.View::showAddressLoading);
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                address,
                                customer.getProfileImage(),
                                customer.getMobile(),
                                customer.getLang(),
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                false,
                                false,
                                customer.getState(),
                                false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (address == null || address.isEmpty()) {
                                        tracker.trackUpdateProfileV7(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.ADDRESS,
                                                customerId,
                                                PropertyValue.TRUE);
                                    } else {
                                        tracker.trackUpdateProfileV1(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.ADDRESS,
                                                customerId);
                                    }
                                    ifAttached(CustomerProfileContract.View::hideAddressLoading);
                                    ifAttached(view -> view.setAddress(customer.getAddress()));
                                },
                                e -> {
                                    Timber.w(e, "update customer failed");
                                    ifAttached(CustomerProfileContract.View::hideAddressLoading);
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void setProfileImage(boolean isCameraImage, File profileImage) {
        String receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID() + ".jpg";

        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                customer.getAddress(),
                                receiptUrl,
                                customer.getMobile(),
                                customer.getLang(),
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                false,
                                false,
                                customer.getState(),
                                false)
                        .andThen(
                                uploadFile.schedule(
                                        IUploadFile.CUSTOMER_PHOTO,
                                        receiptUrl,
                                        profileImage.getAbsolutePath()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isProfilePicChanged = true;

                                    if (isCameraImage) {
                                        tracker.trackUpdateProfileV5(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.PHOTO,
                                                PropertyValue.CAMERA,
                                                customerId);
                                    } else {
                                        tracker.trackUpdateProfileV5(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.PHOTO,
                                                PropertyValue.GALLERY,
                                                customerId);
                                    }
                                    ifAttached(view -> view.displayProfileImageFile(profileImage));
                                },
                                e -> {
                                    Timber.w(e, "update profile pic failed");
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void saveMobile(String mobile) {
        String parseMobile = MobileUtils.parseMobile(mobile);
        if (parseMobile != null && parseMobile.length() != 10) {
            ifAttached(CustomerProfileContract.View::displayInvalidMobileError);
            return;
        }

        ifAttached(CustomerProfileContract.View::showMobileLoading);
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                customer.getAddress(),
                                customer.getProfileImage(),
                                parseMobile,
                                customer.getLang(),
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                true,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                false,
                                false,
                                customer.getState(),
                                false)
                        .andThen(syncSupplierEnabledCustomerIds.execute())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (mobile == null || "".equals(mobile)) {
                                        tracker.trackUpdateProfileV7(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.MOBILE,
                                                customerId,
                                                PropertyValue.TRUE);
                                    } else {
                                        tracker.trackUpdateProfileV1(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.MOBILE,
                                                customerId);
                                    }

                                    ifAttached(CustomerProfileContract.View::hideMobileLoading);
                                    ifAttached(view -> view.setMobile(parseMobile));
                                },
                                e -> {
                                    if (e instanceof CustomerErrors.MobileConflict) {
                                        tracker.trackError(
                                                "customer_profile",
                                                PropertyValue.MOBILE_COFLICT,
                                                "",
                                                PropertyValue.CUSTOMER);
                                        ifAttached(
                                                view ->
                                                        view.onMobileConflict(
                                                                ((CustomerErrors.MobileConflict) e)
                                                                        .getConflict()));
                                    } else if (e instanceof CustomerErrors.ActiveCyclicAccount) {
                                        tracker.trackError(
                                                "customer_profile",
                                                PropertyValue.CYCLIC_ACCOUNT,
                                                "",
                                                PropertyValue.CUSTOMER);
                                        ifAttached(CustomerProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                view ->
                                                        view.showActiveCyclicAccount(
                                                                ((CustomerErrors
                                                                        .ActiveCyclicAccount)
                                                                        e)
                                                                        .getConflict()));
                                    } else if (e instanceof CustomerErrors.DeletedCyclicAccount) {
                                        tracker.trackError(
                                                "customer_profile",
                                                PropertyValue.CYCLIC_ACCOUNT,
                                                "",
                                                PropertyValue.CUSTOMER);
                                        ifAttached(CustomerProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                view ->
                                                        view.showDeletedCyclicAccount(
                                                                ((CustomerErrors
                                                                        .DeletedCyclicAccount)
                                                                        e)
                                                                        .getConflict()));
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(CustomerProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(CustomerProfileContract.View::hideMobileLoading);
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void onSmsLanguageClicked() {
        ifAttached(view -> view.showSmsLangPopup(smsLanguage));
    }

    @Override
    public void updateCustomerTxAlertLanguage(String language, String updatedLang) {
        ifAttached(CustomerProfileContract.View::showLoader);
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                customer.getAddress(),
                                customer.getProfileImage(),
                                customer.getMobile(),
                                updatedLang,
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                false,
                                false,
                                customer.getState(),
                                false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (!updatedLang.equals(language)) {
                                        tracker.trackUpdateProfileV3(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.SMS_LANG,
                                                updatedLang,
                                                customerId);
                                    } else { // if same language selected we consider it as default
                                        tracker.trackUpdateProfileV4(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.SMS_LANG,
                                                updatedLang,
                                                language,
                                                customerId);
                                    }

                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                },
                                e -> {
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                    if (isAuthenticationIssue(e)) {
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void txSmsSwitchChanged(boolean checked, boolean isFromPopup) {
        if (!isInitialSmsSwitchOff() && !isFromPopup) {
            getActiveBusinessId.execute().flatMapCompletable(businessId ->
                    keyValService.put(PREF_BUSINESS_KEY_SMS_TOGGLE_OFF, String.valueOf(true), new Scope.Business(businessId))
            ).subscribeOn(ThreadUtils.INSTANCE.database())
                    .blockingAwait();
            ifAttached(view -> view.showSmsInitialPopup(checked));
        } else {

            if (customer == null) {
                return;
            }

            ifAttached(CustomerProfileContract.View::showLoader);
            addTask(
                    updateCustomer
                            .execute(
                                    customerId,
                                    customer.getDescription(),
                                    customer.getAddress(),
                                    customer.getProfileImage(),
                                    customer.getMobile(),
                                    customer.getLang(),
                                    customer.getReminderMode(),
                                    checked,
                                    true,
                                    false,
                                    customer.getDueInfo_activeDate(),
                                    false,
                                    false,
                                    false,
                                    false,
                                    customer.getState(),
                                    false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        tracker.trackUpdateProfileV3(
                                                PropertyValue.CUSTOMER,
                                                PropertyValue.SMS_SETTINS,
                                                String.valueOf(checked),
                                                customerId);
                                        ifAttached(CustomerProfileContract.View::hideLoader);
                                    },
                                    e -> {
                                        ifAttached(CustomerProfileContract.View::hideLoader);
                                        if (isAuthenticationIssue(e)) {

                                            ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                        } else if (isInternetIssue(e)) {

                                            ifAttached(
                                                    BaseContracts.Online.View
                                                            ::showNoInternetMessage);
                                        } else {

                                            ifAttached(MVP.View::showError);
                                        }
                                    }));
        }
    }

    @Override
    public void onReminderModeSelected(String mode) {
        ifAttached(CustomerProfileContract.View::showLoader);
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                customer.getAddress(),
                                customer.getProfileImage(),
                                customer.getMobile(),
                                customer.getLang(),
                                mode,
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                false,
                                false,
                                customer.getState(),
                                false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    tracker.trackUpdateProfileV3(
                                            PropertyValue.CUSTOMER,
                                            PropertyValue.REMINDER_SETTING,
                                            PropertyValue.SMS,
                                            customerId);
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                },
                                e -> {
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                    if (isAuthenticationIssue(e)) {

                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(e)) {

                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void addTransactionPermissionSwitchChanged(boolean permissionCheck) {
        if (customer == null) {
            ifAttached(MVP.View::showError);
            return;
        }
        ifAttached(CustomerProfileContract.View::showLoader);
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                customer.getAddress(),
                                customer.getProfileImage(),
                                customer.getMobile(),
                                customer.getLang(),
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                permissionCheck,
                                true,
                                customer.getState(),
                                false)
                        .andThen(syncSupplierEnabledCustomerIds.execute())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    tracker.trackUpdateProfileV3(
                                            PropertyValue.CUSTOMER,
                                            PropertyValue.CUSTOMER_PERMISSION,
                                            String.valueOf(permissionCheck),
                                            customerId);
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                },
                                e -> {
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                    if (isAuthenticationIssue(e)) {
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(
                                                view ->
                                                        view.setOldPermissionState(
                                                                !permissionCheck));
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void blockTransaction(Customer.State state) {
        if (state == Customer.State.BLOCKED) {
            tracker.trackBlockRelation(
                    Event.BLOCK_RELATION, PropertyValue.CUSTOMER, customerId);
        } else {
            tracker.trackUnBlockRelation(
                    Event.UNBLOCK_RELATION,
                    PropertyValue.CUSTOMER,
                    customerId,
                    "Customer Profile");
        }
        ifAttached(CustomerProfileContract.View::showLoader);

        if (ContextExtensions.isConnectedToInternet(context)) {
            updateBlockedState(state);
        } else {
            ifAttached(CustomerProfileContract.View::hideLoader);
            ifAttached(BaseContracts.Online.View::showNoInternetMessage);
        }
    }

    private void blockRelation(Customer customer) {
        if (customer.getState() == Customer.State.BLOCKED) {
            ifAttached(
                    view ->
                            view.showBlockRelationShipDialog(
                                    BlockRelationShipDialogFragment.Companion.getTYPE_UNBLOCK(),
                                    customer));
            tracker.trackUnBlockRelation(
                    Event.UNBLOCK_RELATION_CLICKED,
                    PropertyValue.CUSTOMER,
                    customerId,
                    "Customer Profile");
        } else {
            ifAttached(
                    view ->
                            view.showBlockRelationShipDialog(
                                    BlockRelationShipDialogFragment.Companion.getTYPE_BLOCK(),
                                    customer));
            tracker.trackBlockRelation(
                    Event.BLOCK_RELATION_CLICKED,
                    PropertyValue.CUSTOMER,
                    customerId
            );
        }
    }

    private void updateBlockedState(Customer.State state) {
        addTask(
                updateCustomer
                        .execute(
                                customerId,
                                customer.getDescription(),
                                customer.getAddress(),
                                customer.getProfileImage(),
                                customer.getMobile(),
                                customer.getLang(),
                                customer.getReminderMode(),
                                customer.isTxnAlertEnabled(),
                                false,
                                false,
                                customer.getDueInfo_activeDate(),
                                false,
                                false,
                                customer.isAddTransactionPermissionDenied(),
                                false,
                                state,
                                true)
                        .andThen(syncSupplierEnabledCustomerIds.execute())
                        .andThen(dueInfoRepo.clearDueDateForCustomer(customerId))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    ifAttached(
                                            view ->
                                                    view.setBlockState(
                                                            state == Customer.State.BLOCKED));
                                    ifAttached(CustomerProfileContract.View::goToCustomerScreen);
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                },
                                e -> {
                                    ifAttached(CustomerProfileContract.View::hideLoader);
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                view ->
                                                        view.setBlockState(
                                                                customer.getState()
                                                                        == Customer.State.BLOCKED));
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(
                                                view ->
                                                        view.setBlockState(
                                                                customer.getState()
                                                                        == Customer.State.BLOCKED));
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void onBlockClicked() {
        if (customer != null) {
            blockRelation(customer);
        } else {
            addTask(
                    getCustomer
                            .execute(customerId)
                            .observeOn(uiScheduler)
                            .subscribe(
                                    customer -> {
                                        this.customer = customer;
                                        blockRelation(customer);
                                    },
                                    throwable -> {
                                        if (isInternetIssue(throwable)) {
                                            ifAttached(
                                                    BaseContracts.Online.View
                                                            ::showNoInternetMessage);
                                        } else {
                                            ifAttached(MVP.View::showError);
                                        }
                                    }));
        }
    }

    @Override
    public void onInternetRestored() {
        loadData();
    }

    @Override
    public void onDeleteClicked() {
        if (customer != null && customer.getCustomerSyncStatus() == CustomerSyncStatus.DIRTY.getCode()) {
            Toast.makeText(context, R.string.ce_delete_customer_err_error_unsynced_account, Toast.LENGTH_SHORT).show();
        } else {
            ifAttached(view -> view.gotoDeleteScreen(customerId));
        }
    }

    @Override
    public void onCameraClicked() {
        ifAttached(CustomerProfileContract.View::openCamera);
    }

    @Override
    public void onGalleryClicked() {
        ifAttached(CustomerProfileContract.View::openGallery);
    }

    @Override
    public void onEditMobileClicked() {
        if (customer != null && customer.getState() != Customer.State.BLOCKED) {
            ifAttached(view -> view.openAddNumberPopup());
        }
    }

    @Override
    public void onAuthenticationRestored() {
        loadData();
    }

    private Boolean isInitialSmsSwitchOff() {
        String businessId = getActiveBusinessId.execute().subscribeOn(ThreadUtils.INSTANCE.database()).blockingGet();
        if (keyValService
                .contains(PREF_BUSINESS_KEY_SMS_TOGGLE_OFF, new Scope.Business(businessId))
                .subscribeOn(ThreadUtils.INSTANCE.database())
                .blockingGet()) {
            String value =
                    keyValService
                            .get(PREF_BUSINESS_KEY_SMS_TOGGLE_OFF, new Scope.Business(businessId))
                            .subscribeOn(ThreadUtils.INSTANCE.database())
                            .blockingFirst();
            return !Strings.isNullOrEmpty(value) && value.equals("true");
        } else {
            return false;
        }
    }
}
