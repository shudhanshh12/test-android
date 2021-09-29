package in.okcredit.ui.whatsapp;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.provider.ContactsContract;
import dagger.Lazy;
import in.okcredit.R;
import in.okcredit.di.UiThread;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.EventProperties;
import in.okcredit.analytics.PropertyKey;
import in.okcredit.backend._offline.usecase.SetMerchantPreference;
import in.okcredit.individual.contract.PreferenceKey;
import in.okcredit.merchant.usecase.GetActiveBusinessImpl;
import in.okcredit.shared._base_v2.BasePresenter;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import java.util.ArrayList;
import javax.inject.Inject;
import tech.okcredit.android.base.utils.ThreadUtils;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import timber.log.Timber;

public class WhatsAppPresenter extends BasePresenter<WhatsAppContract.View>
        implements WhatsAppContract.Presenter {

    private final CompositeDisposable tasks;
    private WhatsAppContract.View view;
    private final SetMerchantPreference setMerchantPreference;
    private final GetActiveBusinessImpl getActiveBusiness;
    private final Boolean isFromHelp;
    private final Context context;
    private final Lazy<GetWhatsAppNumber> getWhatsAppNumber;

    @Inject
    public WhatsAppPresenter(
            @UiThread Scheduler uiScheduler,
            SetMerchantPreference setMerchantPreference,
            GetActiveBusinessImpl getActiveBusiness,
            Context context,
            Lazy<GetWhatsAppNumber> getWhatsAppNumber,
            @ViewModelParam("is_from_help") Boolean isFromHelp) {
        super(uiScheduler);
        tasks = new CompositeDisposable();
        this.context = context;
        this.isFromHelp = isFromHelp;
        this.getActiveBusiness = getActiveBusiness;
        this.setMerchantPreference = setMerchantPreference;
        this.getWhatsAppNumber = getWhatsAppNumber;
    }

    @Override
    public void attachView(WhatsAppContract.View view) {
        this.view = view;

        tasks.add(
                getActiveBusiness
                        .execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                business -> {
                                    if (view == null) {
                                        return;
                                    }

                                    if (business.getMobile() != null) {
                                        view.setMobile(business.getMobile());
                                    }

                                    if (isFromHelp) {
                                        view.hideDisableButton();
                                    }
                                },
                                throwable -> {}));
    }

    @Override
    public void detachView() {
        view = null;
        tasks.clear();
    }

    @Override
    public void onWhatsAppEnableClicked() {
        Analytics.track(
                AnalyticsEvents.WHATSAPP_SCREEN_ACTION,
                EventProperties.create()
                        .with("whatsapp_enable", true)
                        .with(PropertyKey.SOURCE, isFromHelp ? "help" : "register"));
        tasks.add(
                setMerchantPreference
                        .execute(PreferenceKey.WHATSAPP, String.valueOf(true), false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(
                                new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        if (view != null) {
                                            if (isFromHelp) {
                                                view.goToHelpScreen();
                                            } else {
                                                view.goToHomeScreen();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (view != null) {
                                            if (isFromHelp) {
                                                view.goToHelpScreen();
                                            } else {
                                                view.goToHomeScreen();
                                            }
                                        }
                                    }
                                }));
    }

    @Override
    public void onWhatsAppDisableClicked() {
        Analytics.track(
                AnalyticsEvents.WHATSAPP_SCREEN_ACTION,
                EventProperties.create()
                        .with("whatsapp_enable", false)
                        .with(PropertyKey.SOURCE, isFromHelp ? "help" : "register"));
        tasks.add(
                setMerchantPreference
                        .execute(PreferenceKey.WHATSAPP, String.valueOf(false), false)
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.goToHomeScreen();
                                    }
                                },
                                throwable -> {
                                    if (view != null) {
                                        view.goToHomeScreen();
                                    }
                                }));
    }

    @Override
    public void onWhatsappUsClicked(boolean addContact) {
        if (addContact) {
            Disposable failed_to_add_contact =
                    addContact()
                            .observeOn(uiScheduler)
                            .subscribe(
                                    () -> {
                                        if (view != null) {
                                            view.openWhatsapp(getWhatsAppNumber.get().execute());
                                        }
                                    },
                                    throwable -> {
                                        Timber.e(throwable, "failed to add contact");

                                        if (view != null) {
                                            view.openWhatsapp(getWhatsAppNumber.get().execute());
                                        }
                                    });

            addTask(failed_to_add_contact);
        } else {
            if (view != null) {
                view.openWhatsapp(getWhatsAppNumber.get().execute());
            }
        }
    }

    @Override
    public void onInternetRestored() {}

    @Override
    public void onAuthenticationRestored() {}

    public Completable addContact() {
        return Completable.fromAction(
                        () -> {
                            String displayName = context.getString(R.string.application_name);
                            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                            ops.add(
                                    ContentProviderOperation.newInsert(
                                                    ContactsContract.RawContacts.CONTENT_URI)
                                            .withValue(
                                                    ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                            .withValue(
                                                    ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                            .build());

                            // ------------------------------------------------------ Names
                            ops.add(
                                    ContentProviderOperation.newInsert(
                                                    ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(
                                                    ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(
                                                    ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.StructuredName
                                                            .CONTENT_ITEM_TYPE)
                                            .withValue(
                                                    ContactsContract.CommonDataKinds.StructuredName
                                                            .DISPLAY_NAME,
                                                    displayName)
                                            .build());

                            // ------------------------------------------------------ Mobile Number
                            ops.add(
                                    ContentProviderOperation.newInsert(
                                                    ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(
                                                    ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(
                                                    ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone
                                                            .CONTENT_ITEM_TYPE)
                                            .withValue(
                                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                    getWhatsAppNumber.get().execute())
                                            .withValue(
                                                    ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone
                                                            .TYPE_MOBILE)
                                            .build());

                            // Asking the Contact provider to create a new contact
                            try {
                                context.getContentResolver()
                                        .applyBatch(ContactsContract.AUTHORITY, ops);
                            } catch (Exception e) {
                                Timber.e(e, "Failed to add contact");
                            }
                        })
                .subscribeOn(ThreadUtils.INSTANCE.newThread());
    }
}
