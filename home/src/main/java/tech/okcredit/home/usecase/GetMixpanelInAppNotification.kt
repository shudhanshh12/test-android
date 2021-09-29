package tech.okcredit.home.usecase

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue.REMINDER_SETTING
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.service.in_app_notification.MixPanelInAppNotificationTypes
import `in`.okcredit.backend.service.in_app_notification.MixPanelInAppNotificationTypes.CALCULATOR_EDUCATION
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.shared.data.DbUploadWorker
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import com.mixpanel.android.mpmetrics.InAppNotification
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.json
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.home.widgets.filter_option.data.FilterOptionRepository
import timber.log.Timber
import javax.inject.Inject

@Suppress("UNREACHABLE_CODE")
@Reusable
class GetMixpanelInAppNotification @Inject constructor(
    private val mixpanelAPI: Lazy<MixpanelAPI>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val context: Lazy<Context>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val tracker: Lazy<Tracker>,
    private val filterOptionRepository: Lazy<FilterOptionRepository>,
    private val referralAPi: Lazy<ReferralRepository>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val customerRepository: Lazy<CustomerRepository>,
    private val dbUploadWorker: Lazy<DbUploadWorker>,
) : UseCase<Unit, InAppNotification?> {

    override fun execute(req: Unit): Observable<Result<InAppNotification?>> {
        val notification = mixpanelAPI.get().people.notificationIfAvailable
        Timber.d("<<<<MixPanelNotification Started")
        return UseCase.wrapObservable(
            return if (notification == null) {
                Timber.d("<<<<MixPanelNotification Ended")
                Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            } else {
                getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                    handleMixPanelNotification(notification, businessId)
                }
            }
        )
    }

    private fun handleMixPanelNotification(
        notification: InAppNotification,
        businessId: String,
    ): Observable<Result<InAppNotification?>> {
        val notificationBody = notification.body
        Timber.d("<<<<MixPanelNotification Body: %s", notificationBody)

        mixpanelAPI.get().people.trackNotificationSeen(notification)
        if (MixPanelInAppNotificationTypes.homeShownMixPanelInAppNotificationTypes().contains(notificationBody) ||
            notification.type != InAppNotification.Type.MINI
        ) {
            return Observable.just(Result.Success(notification))
        }
        when (notificationBody) {
            MixPanelInAppNotificationTypes.TUTORIAL_LIVE_SALES -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "live_sales")
                )

                return setBoolean(RxSharedPrefValues.SHOULD_SHOW_LIVE_SALES_TUTORIAL, true, Scope.Individual)
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.ADD_SUPPLIER -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "add_supplier")
                )

                setBoolean(RxSharedPrefValues.SHOULD_SHOW_ADD_SUPPLIER_TAB_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SUPPLIER_TAB -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "supplier_tab")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_SUPPLIER_TAB_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.FIRST_SUPPLIER -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "first_supplier")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_FIRST_SUPPLIER_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SUPPLIER_TAKE_GIVE_CREDIT -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "supplier_take_give_credit")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_TAKE_CREDIT_PAYMENT_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.TUTORIAL_NOTE_CUSTOMER -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "note")
                )

                setBoolean(RxSharedPrefValues.SHOULD_SHOW_NOTE_TUTORIAL, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.START_LOGGING_7_DAYS, MixPanelInAppNotificationTypes.START_LOGGING_24_HR -> {
                return transactionRepo.get().listDirtyTransactions(null, businessId).firstOrError().flatMap { txns ->
                    customerRepo.get().listCustomers(businessId).firstOrError().flatMap { customers ->
                        var timeTillLogging = 0L
                        if (notificationBody == MixPanelInAppNotificationTypes.START_LOGGING_7_DAYS) {
                            timeTillLogging = DateTime.now().plusDays(7).millis
                        } else if (notificationBody == MixPanelInAppNotificationTypes.START_LOGGING_24_HR) {
                            timeTillLogging = DateTime.now().plusHours(24).millis
                        }

                        tracker.get().trackDebug("Remote_Logging")
                        rxCompletable {
                            rxSharedPreference.get()
                                .set(RxSharedPrefValues.LOGGING_END_TIME, timeTillLogging, Scope.Individual)
                        }.doOnComplete {
                            Timber.d("<<<<LogTime Saving: %d", timeTillLogging)
                            LogUtils.startRemoteLogging(
                                context.get(),
                                businessId,
                                txns.json(),
                                customers.json()
                            )
                        }.andThen(Single.just(""))
                    }
                }.ignoreElement()
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.GIVE_DISCOUNT_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "give_discount")
                )

                setBoolean(RxSharedPrefValues.GIVE_DISCOUNT_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.CUSTOMER_MENU_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "customer_menu")
                )

                setBoolean(RxSharedPrefValues.CUSTOMER_MENU_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.FIRST_EXPENSE_EDUCATION_1 -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "first_expense_education_1")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_FIRST_EXPENSE_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.FIRST_EXPENSE_EDUCATION_2 -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "first_expense_education_2")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_FIRST_EXPENSE_EDUCATION_2, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.CALENDAR_PERMISSION -> {
                setBoolean(RxSharedPrefValues.CALENDAR_PERMISSION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.FILTER_EDUCATION_V2 -> {
                return filterOptionRepository.get().setFilterEducationPreference(shown = true)
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.COLLECTION_DATE_EDUCATION -> {
                setBoolean(RxSharedPrefValues.COLLECTION_DATE_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.REMIND_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, REMINDER_SETTING)
                )
                setBoolean(RxSharedPrefValues.REMIND_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.FIRST_SALE_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "first_sale_education")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_FIRST_SALE_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.CHAT_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "chat_education")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_CHAT_TUTORIAL, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.BILL_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "bill_education")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_BILL_TUTORIAL, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_REFERRAL_IN_APP_BOTTOMSHEET -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "home_share_and_earn_bottomsheet")
                )
                return referralAPi.get().setReferralInAppPreference(true)
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.SHOW_HOME_DASHBOARD_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "home_dashboard_education")
                )
                setBoolean(RxSharedPrefValues.SHOULD_SHOW_HOME_DASHBOARD_EDUCATION, true, Scope.Individual)
                    .subscribeOn(ThreadUtils.database()).subscribe()
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_ADD_OKCREDIT_CONTACT_BOTTOMSHEET -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "add_okcredit_contact_bottomsheet")
                )
                return rxCompletable { contactsRepository.get().setContactInAppDisplayed(true) }
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.SHOW_QR_FIRST_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "show_qr_first_education")
                )
                return collectionRepository.get().resetQrEducation()
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.SHOW_COLLECTION_NUDGE_ON_CUSTOMER_SCREEN -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "show_collection_nudge_on_customer_screen")
                )
                resetTxnCntForCollectionNudgeOnCustomerScr(businessId)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_COLLECTION_NUDGE_ON_SET_DUE_DATE -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "show_collection_nudge_on_set_due_date")
                )
                resetTxnCntForCollectionNudgeOnSetDueDate(businessId)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_COLLECTION_NUDGE_ON_DUE_DATE_CROSSED -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "show_collection_nudge_on_due_date_crossed")
                )
                resetTxnCntForCollectionNudgeOnDueDateCrossed(businessId)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_ONLINE_COLLECTION_POPUP -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(
                        PropertyKey.TYPE, MixPanelInAppNotificationTypes.SHOW_ONLINE_COLLECTION_POPUP
                    )
                )
                return setBoolean(CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_SHOWN, false, Scope.Individual)
                    .subscribeOn(ThreadUtils.database())
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.CALCULATOR_EDUCATION -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(
                        PropertyKey.TYPE, CALCULATOR_EDUCATION
                    )
                )
                return rxCompletable { customerRepository.get().setShowCalculatorEducation(true) }
                    .andThen(Observable.just(Result.Failure(Exception("No Mixpanel Notification"))))
            }
            MixPanelInAppNotificationTypes.SHOW_COMPLETE_KYC -> {
                triggerCompleteKycDialog()
                mixpanelAPI.get().people.trackNotificationSeen(notification)
                Timber.d("<<<<MixPanelNotification Body %s saved", notificationBody)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_RISK_KYC -> {
                triggerKycRiskDialog()
                mixpanelAPI.get().people.trackNotificationSeen(notification)
                Timber.d("<<<<MixPanelNotification Body %s saved", notificationBody)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_KYC_BANNER -> {
                triggerKycBanner()
                mixpanelAPI.get().people.trackNotificationSeen(notification)
                Timber.d("<<<<MixPanelNotification Body %s saved", notificationBody)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.SHOW_KYC_STATUS -> {
                triggerKycStatusDialog()
                mixpanelAPI.get().people.trackNotificationSeen(notification)
                Timber.d("<<<<MixPanelNotification Body %s saved", notificationBody)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            MixPanelInAppNotificationTypes.UPLOAD_DB_FILES -> {
                Analytics.track(
                    AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
                    EventProperties.create().with(PropertyKey.TYPE, "upload_db_files")
                )
                invokeDbFilesUploader()
                mixpanelAPI.get().people.trackNotificationSeen(notification)
                Timber.d("<<<<MixPanelNotification Body %s saved", notificationBody)
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
            else -> {
                return Observable.just(Result.Failure(Exception("No Mixpanel Notification")))
            }
        }
    }

    private fun setBoolean(key: String, value: Boolean, scope: Scope): Completable {
        return rxCompletable { rxSharedPreference.get().set(key, value, scope) }
    }

    private fun resetTxnCntForCollectionNudgeOnCustomerScr(businessId: String) {
        transactionRepo.get().allTransactionsCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .flatMapCompletable {
                rxCompletable { customerRepository.get().setTxnCntForCollectionNudgeOnCustomerScr(it, businessId) }
            }
            .subscribe()
    }

    private fun resetTxnCntForCollectionNudgeOnSetDueDate(businessId: String) {
        transactionRepo.get().allTransactionsCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .flatMapCompletable {
                rxCompletable { customerRepository.get().setTxnCntForCollectionNudgeOnSetDueDate(it, businessId) }
            }
            .subscribe()
    }

    private fun resetTxnCntForCollectionNudgeOnDueDateCrossed(businessId: String) {
        transactionRepo.get().allTransactionsCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .flatMapCompletable {
                rxCompletable { customerRepository.get().setTxnCntForCollectionNudgeOnDueDateCrossed(it, businessId) }
            }
            .subscribe()
    }

    private fun triggerCompleteKycDialog() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
            EventProperties.create().with(PropertyKey.TYPE, "show_complete_kyc")
                .with("_campaign_id", "start_kyc")
        )
        setBoolean(RxSharedPrefValues.SHOULD_SHOW_COMPLETE_KYC, true, Scope.Individual)
            .subscribeOn(ThreadUtils.database()).subscribe()
    }

    private fun triggerKycStatusDialog() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
            EventProperties.create().with(PropertyKey.TYPE, "show_kyc_status")
                .with("_campaign_id", "kyc_verification")
        )
        setBoolean(RxSharedPrefValues.SHOULD_SHOW_KYC_STATUS, true, Scope.Individual)
            .subscribeOn(ThreadUtils.database()).subscribe()
    }

    private fun triggerKycRiskDialog() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
            EventProperties.create().with(PropertyKey.TYPE, "show_risk_kyc")
                .with("_campaign_id", "limit_reached")
        )
        setBoolean(RxSharedPrefValues.SHOULD_SHOW_RISK_KYC, true, Scope.Individual)
            .subscribeOn(ThreadUtils.database()).subscribe()
    }

    private fun triggerKycBanner() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_TO_BE_DISPLAYED,
            EventProperties.create().with(PropertyKey.TYPE, "show_kyc_banner")
        )
        setBoolean(RxSharedPrefValues.SHOULD_SHOW_KYC_BANNER, true, Scope.Individual)
            .subscribeOn(ThreadUtils.database()).subscribe()
    }

    private fun invokeDbFilesUploader() = dbUploadWorker.get().schedule()
}
