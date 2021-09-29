package in.okcredit.notification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.core.app.TaskStackBuilder;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.airbnb.deeplinkdispatch.DeepLinkHandler;
import com.airbnb.deeplinkdispatch.DeepLinkUri;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.AndroidInjection;
import in.okcredit.BuildConfig;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.EventProperties;
import in.okcredit.analytics.PropertyKey;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.backend.contract.DeepLinkUrl;
import in.okcredit.frontend.ui.MainActivity;
import in.okcredit.navigation.NavigationActivity;
import tech.okcredit.android.communication.NotificationData;
import tech.okcredit.android.communication.NotificationUtils;
import tech.okcredit.android.communication.analytics.CommunicationTracker;
import tech.okcredit.android.referral.analytics.ReferralEventTracker;
import tech.okcredit.home.ui.activity.HomeActivity;
import timber.log.Timber;

import static in.okcredit.backend.contract.DeepLinkUrl.SUPPLIER_ONLINE_PAYMENT;
import static in.okcredit.backend.contract.DeepLinkUrl.SUPPLIER_ONLINE_PAYMENT_V2;
import static tech.okcredit.android.communication.NotificationData.KEY_NOTIFICATION_INTENT_EXTRA;

@Keep
@DeepLinkHandler({AppDeepLinkModule.class, LibraryDeepLinkModule.class})
public class DeepLinkActivity extends Activity {
    public static final String ACTION_DEEP_LINK_COMPLEX = "deep_link_complex";

    @Inject
    Lazy<CommunicationTracker> communicationTracker;

    @Inject
    Lazy<NotificationUtils> notificationUtils;

    @Inject
    Lazy<DeeplinkActivityBusinessHandler> deeplinkActivityBusinessHandler;

    @DeepLink({
            DeepLinkUrl.HOME,
            DeepLinkUrl.WELCOME,
            DeepLinkUrl.ACCOUNT,
            DeepLinkUrl.ACCOUNT_STATEMENT,
            DeepLinkUrl.SECURITY,
            DeepLinkUrl.APP_LOCK,
            DeepLinkUrl.PAYMENT_PASSWORD,
            DeepLinkUrl.PROFILE,
            DeepLinkUrl.LANGUAGE,
            DeepLinkUrl.SHARE,
            DeepLinkUrl.SHARE_AND_EARN,
            DeepLinkUrl.PRIVACY,
            DeepLinkUrl.RATING,
            DeepLinkUrl.LIVESALE_SCREEN,
            DeepLinkUrl.CUSTOMER,
            DeepLinkUrl.SUPPLIER, // done
            DeepLinkUrl.CUSTOMER_EDIT,
            DeepLinkUrl.SUPPLIER_EDIT, // done
            DeepLinkUrl.CUSTOMER_STATEMENT,
            DeepLinkUrl.SUPPLIER_STATEMENT, // done
            DeepLinkUrl.CUSTOMER_STATEMENT_REMINDER,
            DeepLinkUrl.CUSTOMER_DELETE,
            DeepLinkUrl.SUPPLIER_DELETE, // done
            DeepLinkUrl.BUSINESS_CARD,
            DeepLinkUrl.BUSINESS_LOCATION,
            DeepLinkUrl.BUSINESS_LOCATION_V2,
            DeepLinkUrl.BUSINESS_CATEGORY,
            DeepLinkUrl.BUSINESS_CATEGORY_V2,
            DeepLinkUrl.BUSINESS_TYPE,
            DeepLinkUrl.BUSINESS_TYPE_V2,
            DeepLinkUrl.BACKUP,
            DeepLinkUrl.WHATSAPP_REG_SUCCESS,
            DeepLinkUrl.WHATSAPP_REG_ERROR_TRY_AGAIN,
            DeepLinkUrl.WHATSAPP_REG_ERROR_MOBILE_MISMATCH,
            DeepLinkUrl.COLLECTION_SCREEN_DETAIL_SCREEN,
            DeepLinkUrl.DUE_CUSTOMER,
            DeepLinkUrl.DUE_SUPPLIER,
            DeepLinkUrl.COLLECTION_EDIT_UPI,

            DeepLinkUrl.HOME_V2,
            DeepLinkUrl.WELCOME_V2,
            DeepLinkUrl.ACCOUNT_V2,
            DeepLinkUrl.ACCOUNT_STATEMENT_V2,
            DeepLinkUrl.SECURITY_V2,
            DeepLinkUrl.APP_LOCK_V2,
            DeepLinkUrl.PAYMENT_PASSWORD_V2,
            DeepLinkUrl.PROFILE_V2,
            DeepLinkUrl.LANGUAGE_V2,
            DeepLinkUrl.SHARE_V2,
            DeepLinkUrl.SHARE_AND_EARN_V2,
            DeepLinkUrl.PRIVACY_V2,
            DeepLinkUrl.RATING_V2,
            DeepLinkUrl.LIVESALE_SCREEN_V2,
            DeepLinkUrl.CUSTOMER_V2,
            DeepLinkUrl.SUPPLIER_V2, // done
            DeepLinkUrl.CUSTOMER_EDIT_V2,
            DeepLinkUrl.SUPPLIER_EDIT_V2, // done
            DeepLinkUrl.CUSTOMER_STATEMENT_V2,
            DeepLinkUrl.SUPPLIER_STATEMENT_V2, // done
            DeepLinkUrl.HELP_V2,
            DeepLinkUrl.HELP_SECTION_V2,
            DeepLinkUrl.HELP_SUB_SECTION_V2,
            DeepLinkUrl.CUSTOMER_STATEMENT_REMINDER_V2,
            DeepLinkUrl.CUSTOMER_DELETE_V2,
            DeepLinkUrl.SUPPLIER_DELETE_V2, // done
            DeepLinkUrl.BUSINESS_CARD_V2,
            DeepLinkUrl.BACKUP_V2,
            DeepLinkUrl.WHATSAPP_REG_SUCCESS_V2,
            DeepLinkUrl.WHATSAPP_REG_ERROR_TRY_AGAIN_V2,
            DeepLinkUrl.WHATSAPP_REG_ERROR_MOBILE_MISMATCH_V2,
            DeepLinkUrl.COLLECTION_SCREEN_DETAIL_SCREEN_V2,
            DeepLinkUrl.DUE_CUSTOMER_V2,
            DeepLinkUrl.DUE_SUPPLIER_V2,
            DeepLinkUrl.COLLECTION_EDIT_UPI_V2,

            DeepLinkUrl.REWARDS_SCREEN,
            DeepLinkUrl.REWARDS_SCREEN_V2,
            DeepLinkUrl.UPI_SCREEN,
            DeepLinkUrl.UPI_SCREEN_V2,
            DeepLinkUrl.HOME_ADD_CUSTOMER,
            DeepLinkUrl.HOME_ADD_CUSTOMER_V2,
            DeepLinkUrl.HOME_ONE_TAP_COLLECTION_POPUP,
            DeepLinkUrl.HOME_ONE_TAP_COLLECTION_POPUP_V2,
            DeepLinkUrl.HOME_IN_APP_REVIEW,
            DeepLinkUrl.HOME_IN_APP_REVIEW_V2,
            DeepLinkUrl.HOME_FILTER_DUE_TODAY,
            DeepLinkUrl.COLLECTION_ADOPTION_SCREEN,
            DeepLinkUrl.COLLECTION_ADOPTION_SCREEN_V2,
            DeepLinkUrl.MERCHANT_COLLECTION_SCREEN,
            DeepLinkUrl.MERCHANT_COLLECTION_SCREEN_V2,
            DeepLinkUrl.COLLECTION_ADOPTION_SCREEN_CONFIGURABLE,
            DeepLinkUrl.COLLECTION_ADOPTION_SCREEN_CONFIGURABLE_V2,
            DeepLinkUrl.MERCHANT_DESTINATION_SCREEN,
            DeepLinkUrl.MERCHANT_DESTINATION_SCREEN_V2,
            DeepLinkUrl.REFERRAL,
            DeepLinkUrl.REFERRAL_V2,
            DeepLinkUrl.CHANGE_LANGUAGE_V2,
            DeepLinkUrl.WEB_EXPERIMENT,
            DeepLinkUrl.LOAD_WEB,
            DeepLinkUrl.CATEGORIES,
            DeepLinkUrl.CATEGORIES_V2,
            DeepLinkUrl.HELP_NEW,
            DeepLinkUrl.HELP_NEW_V2,
            DeepLinkUrl.HELP_INSTRUCTION,
            DeepLinkUrl.HELP_INSTRUCTION_V2,
            DeepLinkUrl.EXPENSE_MANAGER,
            DeepLinkUrl.EXPENSE_MANAGER_V2,
            DeepLinkUrl.CASH_SALES,
            DeepLinkUrl.CASH_SALES_V2,
            DeepLinkUrl.ACCOUNT_CHAT,
            DeepLinkUrl.ACCOUNT_CHAT_V2,
            DeepLinkUrl.BILL_MANAGEMENT,
            DeepLinkUrl.BILL_MANAGEMENT_V2,
            DeepLinkUrl.ADD_EXPENSE,
            DeepLinkUrl.ADD_EXPENSE_V2,
            DeepLinkUrl.ADD_SALE,
            DeepLinkUrl.ADD_SALE_V2,
            DeepLinkUrl.LENDING_SME,
            DeepLinkUrl.LENDING_SME_V2,

            DeepLinkUrl.MANUAL_CHAT,
            DeepLinkUrl.MANUAL_CHAT_V2,
            DeepLinkUrl.COLLECTION_DEFAULTER_LIST,
            DeepLinkUrl.COLLECTION_DEFAULTER_LIST_V2,
            DeepLinkUrl.CASH_COUNTER,
            DeepLinkUrl.CASH_COUNTER_V2,

            DeepLinkUrl.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN,
            DeepLinkUrl.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_V2,
            DeepLinkUrl.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_FROM_REFERRAL,
            DeepLinkUrl.ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_FROM_REFERRAL_V2,
            DeepLinkUrl.SALES_BILL,
            DeepLinkUrl.SALES_BILL_V2,
            DeepLinkUrl.USER_MIGRATION_UPLOAD_PDF,
            DeepLinkUrl.USER_MIGRATION_UPLOAD_PDF_V2,
            DeepLinkUrl.QR_CODE_SCREEN,
            DeepLinkUrl.QR_CODE_SCREEN_V2,
            DeepLinkUrl.MERCHANT_BULK_REMINDER,
            DeepLinkUrl.MERCHANT_BULK_REMINDER_V2,
            SUPPLIER_ONLINE_PAYMENT,
            SUPPLIER_ONLINE_PAYMENT_V2,
            DeepLinkUrl.SAVE_OKCREDIT_CONTACT,
            DeepLinkUrl.SAVE_OKCREDIT_CONTACT_V2,
            DeepLinkUrl.IPL_SCREEN,
            DeepLinkUrl.IPL_SCREEN_V2,
            DeepLinkUrl.IPL_WEEKLY_DRAW,
            DeepLinkUrl.IPL_WEEKLY_DRAW_V2,
            DeepLinkUrl.IPL_LEADER_BOARD,
            DeepLinkUrl.IPL_LEADER_BOARD_V2,
            DeepLinkUrl.IPL_GAME_SCREEN,
            DeepLinkUrl.IPL_GAME_SCREEN_V2,
            DeepLinkUrl.PSP_UPI_APPROVE_COLLECT,
            DeepLinkUrl.PSP_UPI_APPROVE_COLLECT_V2,
            DeepLinkUrl.CUSTOMER_ONLINE_PAYMENT,
            DeepLinkUrl.CUSTOMER_ONLINE_PAYMENT_V2,
            DeepLinkUrl.REFERRAL_COLLECTION_ADOPTION_SCREEN,
            DeepLinkUrl.REFERRAL_COLLECTION_EDUCATION_SCREEN,
            DeepLinkUrl.REFERRAL_COLLECTION_LIST_SCREEN,
            DeepLinkUrl.OKPL_VOICE_COLLECTION_SCREEN,
            DeepLinkUrl.OKPL_VOICE_COLLECTION_SCREEN_V2,
            DeepLinkUrl.STAFF_COLLECTION_LINK_SCREEN,
            DeepLinkUrl.STAFF_COLLECTION_LINK_SCREEN_V2,
            DeepLinkUrl.HOME_BULK_ADD_BY_VOICE,
            DeepLinkUrl.HOME_BULK_ADD_BY_VOICE_V2,
            DeepLinkUrl.HOME_BULK_REMINDER,
            DeepLinkUrl.HOME_BULK_REMINDER_V2,

            DeepLinkUrl.HOME_CUSTOMER_PROFILE,
            DeepLinkUrl.HOME_CUSTOMER_PROFILE_V2,

            DeepLinkUrl.BUSINESS_HEALTH_DASHBOARD,
            DeepLinkUrl.BUSINESS_HEALTH_DASHBOARD_V2
    })

    public static TaskStackBuilder intentForTaskStackBuilderMethods(
            Context context, Bundle extras) {
        String deepLinkUrl = Objects.requireNonNull(extras.getString(DeepLink.URI));
        Timber.i("<<deepLinkUrl=%s", deepLinkUrl);
        deepLinkUrl = trackDeepLinkAndRemoveParams(deepLinkUrl);
        if (deepLinkUrl.contains("okcredit://")) {
            if (BuildConfig.DEBUG) {
                deepLinkUrl = deepLinkUrl.replace("okcredit://", "https://staging.okcredit.app/");
            } else {
                deepLinkUrl = deepLinkUrl.replace("okcredit://", "https://okcredit.app/");
            }
        }

        Timber.i("<<deepLinkUrl=%s", deepLinkUrl);

        if (deepLinkUrl.equals(DeepLinkUrl.WELCOME)) {
            context.startActivity(MainActivity.startingIntentForWelcomeLanguageSelectionScreen(context));
            return TaskStackBuilder.create(context);
        }

        if (extras.getString(DeepLink.URI) == null) {
            return TaskStackBuilder.create(context);
        }

        switch (deepLinkUrl) {
            case DeepLinkUrl.ACCOUNT:
                Analytics.track(
                        AnalyticsEvents.VIEW_ACCOUNT,
                        EventProperties.create().with(PropertyKey.SOURCE, "notification"));
                break;
            case DeepLinkUrl.REFERRAL:
            case DeepLinkUrl.SHARE_AND_EARN:
                EventProperties.create()
                        .with(PropertyKey.SCREEN, PropertyValue.DEEPLINK)
                        .with(PropertyKey.VERSION, ReferralEventTracker.VERSION);
                Analytics.track(
                        ReferralEventTracker.VIEW_REFERRAL,
                        EventProperties.create().with(PropertyKey.SCREEN, PropertyValue.DEEPLINK));
                break;
        }

        ResolveIntentsFromDeeplink resolveIntentsFromDeeplink = new ResolveIntentsFromDeeplink(context);
        List<Intent> deeplinkIntents = resolveIntentsFromDeeplink.execute(deepLinkUrl, extras);
        // do not add home activity again if returned intent contains home activity itself
        if (deeplinkIntents.size() == 1 && Objects.equals(deeplinkIntents.get(0).getComponent().getClassName(),
                HomeActivity.class.getName())) {
            context.startActivity(deeplinkIntents.get(0));
        } else {
            List<Intent> intents = new ArrayList(2);
            intents.add(NavigationActivity.homeScreenIntent(context));
            intents.addAll(deeplinkIntents);
            context.startActivities(intents.toArray(new Intent[intents.size()]));
        }

        return TaskStackBuilder.create(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        traceNotificationData();

        deeplinkActivityBusinessHandler.get().checkActiveBusinessAndDispatchDeeplink(this);
    }

    protected void dispatchDeeplink() {
        String defaultUrl = DeepLinkUrl.HOME;
        DeepLinkUri deepLinkUri = DeepLinkUri.parse(defaultUrl);
        if (new AppDeepLinkModuleRegistry().idxMatch(deepLinkUri) != null) {
            DeepLinkDelegate deepLinkDelegate =
                    new DeepLinkDelegate(
                            new AppDeepLinkModuleRegistry(), new LibraryDeepLinkModuleRegistry());
            deepLinkDelegate.dispatchFrom(this);
        } else {
            NavigationActivity.navigateToLauncherScreen(this);
        }
        finish();
    }

    private void traceNotificationData() {
        String dataString = null;
        if (getIntent().getExtras() != null) {
            dataString = getIntent().getExtras().getString(KEY_NOTIFICATION_INTENT_EXTRA);
        }
        if (dataString != null) {
            NotificationData data = NotificationData.Companion.from(dataString);

            communicationTracker
                    .get()
                    .trackNotificationClicked(
                            data.getPrimaryAction(),
                            data.getCampaignId(),
                            data.getSubCampaignId(),
                            data.getSegment());

            notificationUtils.get().clearEmptySummeryNotifications();
        }
    }

    private static String trackDeepLinkAndRemoveParams(String deepLink) {
        try {
            String primaryAction = deepLink.split("\\?")[0];

            Uri uri = Uri.parse(deepLink);
            Set<String> params = uri.getQueryParameterNames();
            EventProperties eventProperties = EventProperties.create();
            eventProperties.with("primaryAction", primaryAction);
            eventProperties.with("IsDeferred", false);

            for (String key : params) {
                List<String> value = uri.getQueryParameters(key);
                if (value != null && !value.isEmpty()) {
                    eventProperties.with(key, value.get(0));
                }
            }
            Timber.d("Attribution facebook %s 1", params.toString());
            Analytics.track(AnalyticsEvents.VIEW_DEEPLINK, eventProperties);
            return primaryAction;
        } catch (Exception e) {
            return DeepLinkUrl.HOME;
        }
    }
}
