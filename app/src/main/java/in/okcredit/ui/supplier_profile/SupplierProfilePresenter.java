package in.okcredit.ui.supplier_profile;

import android.annotation.SuppressLint;
import android.content.Context;

import org.jetbrains.annotations.Nullable;

import java.io.File;

import javax.inject.Inject;

import dagger.Lazy;
import in.okcredit.di.UiThread;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.Event;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Tracker;
import in.okcredit.backend._offline.usecase.UpdateSupplier;
import in.okcredit.backend.contract.GetMerchantPreference;
import in.okcredit.fileupload.usecase.IImageLoader;
import in.okcredit.fileupload.usecase.IImageStorageStatus;
import in.okcredit.fileupload.utils.SchedulerProvider;
import in.okcredit.frontend.ui.MainActivity;
import in.okcredit.merchant.contract.Business;
import in.okcredit.individual.contract.PreferenceKey;
import in.okcredit.merchant.customer_ui.ui.dialogs.BlockRelationShipDialogFragment;
import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors;
import in.okcredit.merchant.usecase.GetActiveBusinessImpl;
import in.okcredit.shared._base_v2.BasePresenter;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.shared.service.keyval.KeyValService;
import in.okcredit.shared.utils.AbFeatures;
import in.okcredit.supplier.usecase.GetSupplier;
import in.okcredit.ui._base_v2.BaseContracts;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import tech.okcredit.android.ab.AbRepository;
import tech.okcredit.android.auth.usecases.IsPasswordSet;
import tech.okcredit.android.base.extensions.ContextExtensions;
import tech.okcredit.android.base.language.LocaleManager;
import tech.okcredit.android.base.preferences.Scope;
import tech.okcredit.android.base.utils.MobileUtils;
import tech.okcredit.android.base.utils.ThreadUtils;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import tech.okcredit.contract.MerchantPrefSyncStatus;
import tech.okcredit.use_case.GetAccountsTotalBills;
import timber.log.Timber;

import static tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_KEY_SMS_TOGGLE_ON;
import static tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_KEY_SMS_TOGGLE_OFF;

public class SupplierProfilePresenter extends BasePresenter<SupplierProfileContract.View>
        implements SupplierProfileContract.Presenter {
    private final GetSupplier getSupplier;
    private final String supplierId;
    private final UpdateSupplier updateSupplier;
    private final IImageLoader imageLoader;
    private final Context context;
    private final KeyValService keyValService;
    private final GetActiveBusinessImpl getActiveBusiness;
    private final IsPasswordSet isPasswordSet;
    private final Tracker tracker;
    private final Lazy<SchedulerProvider> schedulerProvider;
    private boolean isEditMobile;
    private Supplier supplier;
    private boolean isProfilePicChanged;
    private String smsLanguage = LocaleManager.LANGUAGE_ENGLISH;
    private Business business;
    private final Lazy<LocaleManager> localeManager;
    private final Lazy<GetAccountsTotalBills> getAccountsTotalBillsLazy;
    private final Lazy<AbRepository> abLazy;
    private final Lazy<GetMerchantPreference> getMerchantPreference;
    private final Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus;

    @Inject
    public SupplierProfilePresenter(
            @UiThread Scheduler uiScheduler,
            GetSupplier getSupplier,
            @ViewModelParam(MainActivity.ARG_SUPPLIER_ID) String supplierId,
            @ViewModelParam("is_edit_mobile") boolean isEditMobile,
            UpdateSupplier updateSupplier,
            IImageLoader imageLoader,
            GetActiveBusinessImpl getActiveBusiness,
            KeyValService keyValService,
            IsPasswordSet isPasswordSet,
            Context context,
            Tracker tracker,
            Lazy<LocaleManager> localeManager,
            Lazy<GetAccountsTotalBills> getAccountsTotalBillsLazy,
            Lazy<AbRepository> abLazy,
            Lazy<GetMerchantPreference> getMerchantPreference,
            Lazy<SchedulerProvider> schedulerProvider,
            Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus) {
        super(uiScheduler);
        this.getSupplier = getSupplier;
        this.supplierId = supplierId;
        this.updateSupplier = updateSupplier;
        this.isEditMobile = isEditMobile;
        this.imageLoader = imageLoader;
        this.context = context;
        this.getActiveBusiness = getActiveBusiness;
        this.isPasswordSet = isPasswordSet;
        this.keyValService = keyValService;
        this.tracker = tracker;
        this.localeManager = localeManager;
        this.getAccountsTotalBillsLazy = getAccountsTotalBillsLazy;
        this.abLazy = abLazy;
        this.getMerchantPreference = getMerchantPreference;
        this.merchantPrefSyncStatus = merchantPrefSyncStatus;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    protected void loadData() {
        addTask(
                getSupplier
                        .executeObservable(supplierId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                supplier -> {
                                    this.supplier = supplier;
                                    ifAttached(view -> view.setName(supplier.getName()));

                                    ifAttached(view -> view.setMobile(supplier.getMobile()));

                                    ifAttached(view -> view.setAddress(supplier.getAddress()));

                                    ifAttached(view -> view.setSupplier(supplier));

                                    ifAttached(
                                            view ->
                                                    view.setBlockField(
                                                            supplier.getState()
                                                                    == Supplier.BLOCKED));

                                    if (supplier.getLang() != null
                                            && !supplier.getLang().isEmpty()) {
                                        smsLanguage = supplier.getLang();
                                    } else {
                                        // load merchant default lang
                                        smsLanguage = localeManager.get().getLanguage();
                                    }

                                    ifAttached(
                                            view ->
                                                    view.setSupplierPref(
                                                            smsLanguage,
                                                            supplier.getTxnAlertEnabled()));

                                    if (isEditMobile) {
                                        ifAttached(view -> view.showMobileEditBox());
                                        isEditMobile = false;
                                    }

                                    if (supplier.getProfileImage() != null) {

                                        addTask(
                                                imageLoader
                                                        .load(supplier.getProfileImage())
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
                getActiveBusiness
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribe(
                                business -> this.business = business,
                                throwable -> ifAttached(MVP.View::showError)));
        addTask(
                getAccountsTotalBillsLazy
                        .get()
                        .execute(supplierId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                result -> {
                                    ifAttached(
                                            view ->
                                                    view.setUnseenAndTotalBillsCount(
                                                            result.getTotalCount(),
                                                            result.getUnseenCount()));
                                },
                                throwable -> ifAttached(view -> view.showError())));
        addTask(
                abLazy.get()
                        .isFeatureEnabled(AbFeatures.BILL_MANAGER, false, null)
                        .observeOn(uiScheduler)
                        .subscribe(
                                result -> ifAttached(view -> view.isBillFeatureEnabled(result)),
                                throwable -> ifAttached(MVP.View::showError)));
        addTask(
                abLazy.get()
                        .isFeatureEnabled(AbFeatures.NEW_ON_BILL_ICON, false, null)
                        .observeOn(uiScheduler)
                        .subscribe(
                                result -> ifAttached(view -> view.isNewBillFeatureEnabled(result)),
                                throwable -> ifAttached(MVP.View::showError)));
    }

    @SuppressLint("CheckResult")
    @Override
    public void saveName(String description) {
        if (description != null && description.length() < 1) {
            ifAttached(SupplierProfileContract.View::displayInvalidNameError);
            return;
        } else if (description != null && description.equals(supplier.getName())) {
            ifAttached(view -> view.setName(supplier.getName()));
            ifAttached(SupplierProfileContract.View::hideNameLoading);
            return;
        }
        ifAttached(SupplierProfileContract.View::showNameLoading);

        addTask(
                updateSupplier
                        .execute(
                                supplierId,
                                description,
                                supplier.getMobile(),
                                supplier.getAddress(),
                                null,
                                supplier.getTxnAlertEnabled(),
                                supplier.getLang(),
                                supplier.getRegistered(),
                                supplier.getDeleted(),
                                supplier.getCreateTime(),
                                supplier.getBalance(),
                                false,
                                false,
                                Supplier.ACTIVE,
                                false,
                                supplier.getRestrictContactSync())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    tracker.trackUpdateProfileV1(
                                            PropertyValue.SUPPLIER,
                                            PropertyValue.NAME,
                                            supplierId);
                                    ifAttached(SupplierProfileContract.View::hideNameLoading);
                                    ifAttached(view -> view.setName(supplier.getName()));
                                },
                                e -> {
                                    Timber.w(e, "update supplier failed");
                                    ifAttached(SupplierProfileContract.View::hideNameLoading);
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
    public void saveAddress(String address) {
        if (address != null && address.length() < 1) {
            ifAttached(SupplierProfileContract.View::displayInvalidAddressError);
            return;
        }
        if (address != null && address.equals(supplier.getAddress())) {
            ifAttached(view -> view.setAddress(supplier.getAddress()));
            return;
        }
        ifAttached(SupplierProfileContract.View::showAddressLoading);
        addTask(
                updateSupplier
                        .execute(
                                supplierId,
                                supplier.getName(),
                                supplier.getMobile(),
                                address,
                                null,
                                supplier.getTxnAlertEnabled(),
                                supplier.getLang(),
                                supplier.getRegistered(),
                                supplier.getDeleted(),
                                supplier.getCreateTime(),
                                supplier.getBalance(),
                                false,
                                false,
                                Supplier.ACTIVE,
                                false,
                                supplier.getRestrictContactSync())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (address == null || address.isEmpty()) {
                                        tracker.trackUpdateProfileV7(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.ADDRESS,
                                                supplierId,
                                                PropertyValue.TRUE);
                                    } else {
                                        tracker.trackUpdateProfileV1(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.ADDRESS,
                                                supplierId);
                                    }

                                    ifAttached(SupplierProfileContract.View::hideAddressLoading);
                                    ifAttached(view -> view.setAddress(supplier.getAddress()));
                                },
                                e -> {
                                    Timber.w(e, "update supplier failed");
                                    ifAttached(SupplierProfileContract.View::hideAddressLoading);
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

        addTask(
                updateSupplier
                        .execute(
                                supplierId,
                                supplier.getName(),
                                supplier.getMobile(),
                                supplier.getAddress(),
                                profileImage.getAbsolutePath(),
                                supplier.getTxnAlertEnabled(),
                                supplier.getLang(),
                                supplier.getRegistered(),
                                supplier.getDeleted(),
                                supplier.getCreateTime(),
                                supplier.getBalance(),
                                false,
                                false,
                                Supplier.ACTIVE,
                                false,
                                supplier.getRestrictContactSync())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isProfilePicChanged = true;
                                    if (isCameraImage) {
                                        tracker.trackUpdateProfileV5(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.PHOTO,
                                                PropertyValue.CAMERA,
                                                supplierId);
                                    } else {
                                        tracker.trackUpdateProfileV5(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.PHOTO,
                                                PropertyValue.GALLERY,
                                                supplierId);
                                    }
                                    ifAttached(view -> view.displayProfileImageFile(profileImage));
                                },
                                e -> {
                                    Timber.w(e, "update profile pic failed");
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
    public void saveMobile(String mobile) {
        String parseMobile = MobileUtils.parseMobile(mobile);
        if (parseMobile != null && parseMobile.length() != 10) {
            ifAttached(view -> view.displayInvalidMobileError());
            return;
        }
        ifAttached(view -> view.showMobileLoading());
        addTask(
                updateSupplier
                        .execute(
                                supplierId,
                                supplier.getName(),
                                parseMobile,
                                supplier.getAddress(),
                                null,
                                supplier.getTxnAlertEnabled(),
                                supplier.getLang(),
                                supplier.getRegistered(),
                                supplier.getDeleted(),
                                supplier.getCreateTime(),
                                supplier.getBalance(),
                                false,
                                true,
                                Supplier.ACTIVE,
                                false,
                                supplier.getRestrictContactSync())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (mobile == null || mobile.isEmpty()) {
                                        tracker.trackUpdateProfileV7(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.MOBILE,
                                                supplierId,
                                                PropertyValue.TRUE);
                                    } else {
                                        tracker.trackUpdateProfileV1(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.MOBILE,
                                                supplierId);
                                    }

                                    ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                    ifAttached(view -> view.setMobile(parseMobile));
                                },
                                e -> {
                                    if (e instanceof SupplierCreditServerErrors.MobileConflict) {
                                        tracker.trackError(
                                                "supplier_profile",
                                                PropertyValue.MOBILE_COFLICT,
                                                "",
                                                PropertyValue.SUPPLIER);
                                        ifAttached(
                                                view ->
                                                        view.onMobileConflict(
                                                                ((SupplierCreditServerErrors
                                                                                        .MobileConflict)
                                                                                e)
                                                                        .getSupplier()));
                                    } else if (e
                                            instanceof
                                            SupplierCreditServerErrors.ActiveCyclicAccount) {
                                        tracker.trackError(
                                                "supplier_profile",
                                                PropertyValue.CYCLIC_ACCOUNT,
                                                "",
                                                PropertyValue.SUPPLIER);
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                view ->
                                                        view.showActiveCyclicAccount(
                                                                ((SupplierCreditServerErrors
                                                                                        .ActiveCyclicAccount)
                                                                                e)
                                                                        .getInfo()));
                                    } else if (e
                                            instanceof
                                            SupplierCreditServerErrors.DeletedCyclicAccount) {
                                        tracker.trackError(
                                                "supplier_profile",
                                                PropertyValue.CYCLIC_ACCOUNT,
                                                "",
                                                PropertyValue.SUPPLIER);
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                view ->
                                                        view.showActiveCyclicAccount(
                                                                ((SupplierCreditServerErrors
                                                                                        .DeletedCyclicAccount)
                                                                                e)
                                                                        .getInfo()));
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void onSmsLanguageClicked() {
        ifAttached(view -> view.showSmsLangPopup(smsLanguage, supplierId));
    }

    @Override
    public void updateSupplierTxAlertLanguage(String language, String updatedLang) {
        addTask(
                updateSupplier
                        .execute(
                                supplierId,
                                supplier.getName(),
                                supplier.getMobile(),
                                supplier.getAddress(),
                                null,
                                supplier.getTxnAlertEnabled(),
                                updatedLang,
                                supplier.getRegistered(),
                                supplier.getDeleted(),
                                supplier.getCreateTime(),
                                supplier.getBalance(),
                                false,
                                false,
                                Supplier.ACTIVE,
                                false,
                                supplier.getRestrictContactSync())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (!updatedLang.equals(language)) {
                                        tracker.trackUpdateProfileV3(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.SMS_LANG,
                                                updatedLang,
                                                supplierId);
                                    } else { // if same language selected we consider it as default
                                        tracker.trackUpdateProfileV4(
                                                PropertyValue.SUPPLIER,
                                                PropertyValue.SMS_LANG,
                                                updatedLang,
                                                language,
                                                supplierId);
                                    }
                                },
                                e -> {
                                    Timber.i("language update failes");
                                    if (e instanceof SupplierCreditServerErrors.MobileConflict) {
                                        ifAttached(
                                                view ->
                                                        view.onMobileConflict(
                                                                ((SupplierCreditServerErrors
                                                                                        .MobileConflict)
                                                                                e)
                                                                        .getSupplier()));
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void txSmsSwitchChanged(boolean checked, boolean isFromPopup) {
        if (!isInitialSmsSwitchOff() && !isFromPopup) {

            keyValService
                    .put(PREF_BUSINESS_KEY_SMS_TOGGLE_OFF, String.valueOf(true), new Scope.Business(business.getId()))
                    .subscribeOn(ThreadUtils.INSTANCE.database())
                    .blockingAwait();
            ifAttached(view -> view.showSmsInitialPopup(checked));
        } else if (!isInitialSmsSwitchOn() && !isFromPopup) {

            keyValService
                    .put(PREF_BUSINESS_KEY_SMS_TOGGLE_ON, String.valueOf(true), new Scope.Business(business.getId()))
                    .subscribeOn(ThreadUtils.INSTANCE.database())
                    .blockingAwait();
            ifAttached(view -> view.showSmsInitialPopup(checked));
        } else {

            addTask(
                    updateSupplier
                            .execute(
                                    supplierId,
                                    supplier.getName(),
                                    supplier.getMobile(),
                                    supplier.getAddress(),
                                    null,
                                    checked,
                                    supplier.getLang(),
                                    supplier.getRegistered(),
                                    supplier.getDeleted(),
                                    supplier.getCreateTime(),
                                    supplier.getBalance(),
                                    true,
                                    false,
                                    Supplier.ACTIVE,
                                    false,
                                    supplier.getRestrictContactSync())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () ->
                                            tracker.trackUpdateProfileV3(
                                                    PropertyValue.SUPPLIER,
                                                    PropertyValue.SMS_SETTINS,
                                                    String.valueOf(checked),
                                                    supplierId),
                                    e -> {
                                        if (e
                                                instanceof
                                                SupplierCreditServerErrors.MobileConflict) {

                                            ifAttached(
                                                    view ->
                                                            view.onMobileConflict(
                                                                    ((SupplierCreditServerErrors
                                                                                            .MobileConflict)
                                                                                    e)
                                                                            .getSupplier()));
                                        } else if (isInternetIssue(e)) {
                                            ifAttached(
                                                    SupplierProfileContract.View
                                                            ::hideMobileLoading);
                                            ifAttached(
                                                    BaseContracts.Online.View
                                                            ::showNoInternetMessage);
                                        } else {
                                            ifAttached(
                                                    SupplierProfileContract.View
                                                            ::hideMobileLoading);
                                            ifAttached(MVP.View::showError);
                                        }
                                    }));
        }
    }

    @Override
    public void onReminderModeSelected(String mode) {}

    @Override
    public void blockTransaction(int state) {
        if (state == Supplier.BLOCKED) {
            tracker.trackBlockRelation(
                    Event.BLOCK_RELATION, PropertyValue.SUPPLIER, supplierId);
        } else {
            tracker.trackUnBlockRelation(
                    Event.UNBLOCK_RELATION,
                    PropertyValue.SUPPLIER,
                    supplierId,
                    "Supplier Profile");
        }
        ifAttached(SupplierProfileContract.View::showMobileLoading);

        if (ContextExtensions.isConnectedToInternet(context)) {
            updateBlockedState(state);
        } else {
            ifAttached(SupplierProfileContract.View::hideMobileLoading);
            ifAttached(BaseContracts.Online.View::showNoInternetMessage);
        }
    }

    private void updateBlockedState(int state) {
        addTask(
                updateSupplier
                        .execute(
                                supplierId,
                                supplier.getName(),
                                supplier.getMobile(),
                                supplier.getAddress(),
                                null,
                                supplier.getTxnAlertEnabled(),
                                supplier.getLang(),
                                supplier.getRegistered(),
                                supplier.getDeleted(),
                                supplier.getCreateTime(),
                                supplier.getBalance(),
                                true,
                                false,
                                state,
                                true,
                                supplier.getRestrictContactSync())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    ifAttached(
                                            view -> view.setBlockField(state == Supplier.BLOCKED));
                                    ifAttached(SupplierProfileContract.View::goToSupplierScreen);
                                },
                                e -> {
                                    if (e instanceof SupplierCreditServerErrors.MobileConflict) {
                                        ifAttached(
                                                view ->
                                                        view.onMobileConflict(
                                                                ((SupplierCreditServerErrors
                                                                                        .MobileConflict)
                                                                                e)
                                                                        .getSupplier()));
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(
                                                view ->
                                                        view.setBlockField(
                                                                supplier.getState()
                                                                        == Supplier.BLOCKED));
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(
                                                view ->
                                                        view.setBlockField(
                                                                supplier.getState()
                                                                        == Supplier.BLOCKED));
                                        ifAttached(SupplierProfileContract.View::hideMobileLoading);
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void onBlockClicked() {
        if (supplier.getState() == Supplier.BLOCKED) {
            ifAttached(
                    view ->
                            view.showBlockRelationShipDialog(
                                    BlockRelationShipDialogFragment.Companion.getTYPE_UNBLOCK(),
                                    supplier.getName(),
                                    supplier.getMobile(),
                                    supplier.getProfileImage()));
            tracker.trackUnBlockRelation(
                    Event.UNBLOCK_RELATION_CLICKED,
                    PropertyValue.SUPPLIER,
                    supplierId,
                    "Supplier Profile");
        } else {
            ifAttached(
                    view ->
                            view.showBlockRelationShipDialog(
                                    BlockRelationShipDialogFragment.Companion.getTYPE_BLOCK(),
                                    supplier.getName(),
                                    supplier.getMobile(),
                                    supplier.getProfileImage()));
            tracker.trackBlockRelation(
                    Event.BLOCK_RELATION_CLICKED,
                    PropertyValue.SUPPLIER,
                    supplierId
            );
        }
    }

    @Override
    public void onInternetRestored() {
        loadData();
    }

    @Override
    public void onDeleteClicked() {
        if (supplier.getRegistered() && supplier.getAddTransactionRestricted()) {
            ifAttached(view -> view.showTransactionRestrictionDeletion(supplier));
        } else {
            ifAttached(SupplierProfileContract.View::showDeletePasswordLoader);
            addTask(
                    isPasswordSet
                            .execute()
                            .observeOn(uiScheduler)
                            .subscribeOn(schedulerProvider.get().api())
                            .subscribe(
                                    (it) -> {
                                        ifAttached(
                                                SupplierProfileContract.View
                                                        ::hideDeletePasswordLoader);
                                        if (it) {
                                            checkIsSyncPrefDone();
                                        } else {
                                            tracker.track(AnalyticsEvents.DELETE_SUPPLIER_SCREEN);
                                            ifAttached(
                                                    view ->
                                                            view.goToResetPasswordScreen(
                                                                    business.getMobile()));
                                        }
                                    },
                                    e -> {
                                        ifAttached(
                                                SupplierProfileContract.View
                                                        ::hideDeletePasswordLoader);
                                        if (isInternetIssue(e)) {
                                            ifAttached(
                                                    SupplierProfileContract.View
                                                            ::hideMobileLoading);
                                            ifAttached(
                                                    BaseContracts.Online.View
                                                            ::showNoInternetMessage);
                                        } else {
                                            ifAttached(
                                                    SupplierProfileContract.View
                                                            ::hideMobileLoading);
                                            ifAttached(MVP.View::showError);
                                        }
                                    }));
        }
    }

    private void checkIsSyncPrefDone() {
        addTask(
                merchantPrefSyncStatus
                        .get()
                        .checkMerchantPrefSync()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    if (it) {
                                        checkIsFourDigitPin();
                                    } else {
                                        syncMerchantPref();
                                    }
                                },
                                e -> {
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    private void syncMerchantPref() {
        addTask(
                merchantPrefSyncStatus
                        .get()
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                this::checkIsFourDigitPin,
                                e -> {
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    private void checkIsFourDigitPin() {
        addTask(
                getMerchantPreference
                        .get()
                        .execute(PreferenceKey.FOUR_DIGIT_PIN)
                        .firstOrError()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    if (Boolean.parseBoolean(it)) {
                                        ifAttached(view -> view.gotoDeleteScreen(supplierId));
                                    } else {
                                        ifAttached(
                                                SupplierProfileContract.View::showUpdatePinDialog);
                                    }
                                },
                                e -> {
                                    ifAttached(
                                            SupplierProfileContract.View::hideDeletePasswordLoader);
                                    if (isInternetIssue(e)) {
                                        ifAttached(view -> view.hideMobileLoading());
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.hideMobileLoading());
                                        ifAttached(view -> view.showError());
                                    }
                                }));
    }

    @Override
    public void onCameraClicked() {
        ifAttached(view -> view.openCamera());
    }

    @Override
    public void onGalleryClicked() {
        ifAttached(view -> view.openGallery());
    }

    @Override
    public void onEditMobileClicked() {
        if (supplier != null && supplier.getState() != Supplier.BLOCKED) {
            ifAttached(
                    view -> {
                        view.setMobile(supplier.getMobile());
                        view.showMobileEditBox();
                    });
        } else {
            ifAttached(
                    view -> {
                        view.setMobile(null);
                        view.showMobileEditBox();
                    });
        }
    }

    @Override
    public void onEditNameClicked() {
        if (supplier != null) {
            ifAttached(
                    view -> {
                        view.setName(supplier.getName());
                    });
        }
    }

    @Override
    public void onEditAddressClicked() {
        ifAttached(
                view -> {
                    view.setAddress(supplier.getAddress());
                });
    }

    @Override
    public void onAuthenticationRestored() {
        loadData();
    }

    private Boolean isInitialSmsSwitchOff() {
        return supplier.getTxnAlertEnabled();
    }

    private Boolean isInitialSmsSwitchOn() {
        return supplier.getTxnAlertEnabled();
    }
}
