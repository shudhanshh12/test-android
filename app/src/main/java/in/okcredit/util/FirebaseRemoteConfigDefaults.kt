package `in`.okcredit.util

import `in`.okcredit.BuildConfig
import `in`.okcredit.backend._offline.usecase.reports_v2.GetReportV2UrlWithTimeout.Companion.FRC_SHARE_REPORT_MAX_POLLING_TIME_IN_SECONDS_KEY
import `in`.okcredit.backend.worker.HomeDataSyncWorkerImpl.Companion.FRC_KEY_HOME_DATA_SYNC_WORKER_RATE_LIMIT_HOURS
import `in`.okcredit.backend.worker.NonActiveBusinessesDataSyncWorkerImpl.Companion.FRC_KEY_NON_ACTIVE_BUSINESSES_DATA_SYNC_WORKER_RATE_LIMIT_HOURS
import `in`.okcredit.backend.worker.NonActiveBusinessesDataSyncWorkerImpl.Companion.FRC_NON_ACTIVE_BUSINESSES_DATA_SYNCER_ENABLED
import `in`.okcredit.backend.worker.PeriodicDataSyncWorkerImpl.Companion.FRC_KEY_PERIODIC_DATA_SYNC_WORKER_RATE_LIMIT_HOURS
import `in`.okcredit.merchant.core.sync.SyncCustomerCommands.Companion.MAX_COUNT_PER_REQUEST_FOR_SYNC_CUSTOMER_KEY
import `in`.okcredit.merchant.core.sync.SyncCustomerCommands.Companion.MAX_RETRY_COUNT_OF_SYNC_CUSTOMER_KEY
import `in`.okcredit.merchant.customer_ui.ui.payment.ShowExpandedQrInAddPayment.Companion.FRC_MAX_EXPANDED_QR_SHOWN_COUNT
import `in`.okcredit.merchant.usecase.CreateBusiness.Companion.FRC_MAX_NUMBER_OF_BUSINESSES
import `in`.okcredit.onboarding.language.LanguageSelectionViewModel.Companion.IS_SOCIAL_VALIDATION_ENABLED
import `in`.okcredit.shared.base.BaseScreen.Companion.DEVICE_MEMORY_TRACKING_SAMPLING
import `in`.okcredit.ui._utils.TrackDeviceInfo.Companion.FILE_STORAGE_TRACKING_SAMPLING
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionViewModel.Companion.FRC_KEY_OKPL_AUDIO_SAMPLE_MAXIMUM_DURATION_MILLIS
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionViewModel.Companion.FRC_KEY_OKPL_AUDIO_SAMPLE_MINIMUM_DURATION_MILLIS
import `in`.okcredit.voice_first.usecase.VoiceRecorder.Companion.FRC_KEY_VOICE_COLLECTION_SAMPLE_RATE
import merchant.okcredit.accounting.contract.model.FRC_HElP_CHAT_NUMBER_PAYMENT
import merchant.okcredit.accounting.contract.model.FRC_HElP_NUMBER_PAYMENT
import merchant.okcredit.accounting.contract.model.FRC_PAYMENT_24X7
import tech.okcredit.android.base.workmanager.RateLimit.Companion.FRC_KEY_NON_CRITICAL_DATA_WORKER_RATE_LIMIT_HOURS
import tech.okcredit.base.network.NetworkModule.Companion.NETWORK_INSTRUMENTATION_SAMPLING
import tech.okcredit.feature_help.contract.HelpConstants
import tech.okcredit.okstream.OkStreamServiceImpl.Companion.OKSTREAM_FLAG_KEY
import tech.okcredit.web.utils.WebViewUtils
import javax.inject.Inject

class FirebaseRemoteConfigDefaults @Inject constructor() {

    companion object {
        val DEFAULT_FIREBASE_REMOTE_SYNC_INTERVAL = if (BuildConfig.DEBUG) 60L else 3600L
    }

    fun getDefaultsMap() = mapOf(
        "skip_truecaller_onboarding" to false,
        "in_app_notification_count_per_day" to 1,
        "cashback_message_details_cache_ttl_hours" to 1,
        "cashback_reward_request_retry_interval_millis" to 500,
        "cashback_reward_request_timelimit_millis" to 3000,
        "heap_analysis_flag" to false,
        "transaction_processing_limit" to 3000,
        "max_count_per_request_for_sync_txn" to 5,
        "max_retry_count_of_sync_transaction" to 3,
        "app_lock_session_time_in_minutes" to 20,
        "anr_duration" to 4000,
        "sync_raw_sms_worker_interval_in_hours" to 24,
        "max_count_transactions_per_execution" to 50,
        "download_report_interval_in_seconds" to 5,
        "worker_maximum_attempt" to 5,
        "string_experiment_flag" to true,
        "periodic_syncer_flag" to true,
        "contact_syncer_flag" to true,
        "frame_rate_tracking_sampling" to 3,
        "timeout_duration_for_aws" to 45,
        "file_upload_scale_image_bitmap_width_height" to 1000,
        "file_upload_compressed_image_quality" to 100,
        "finbox_api_max_time_limit" to 300,
        "voice_samples_from_notes_max_count" to 6,
        "help_number" to 8296508123L,
        "payables_onboarding_full_roll_out" to false,
        "contact_sync_paging_limit" to 1000,
        "contact_sync_chunk_size" to 500,
        "phonebook_query_batch_size" to 100,
        "contacts_network_worker_interval" to 72,
        OKSTREAM_FLAG_KEY to true,
        "okstream_flag" to false,
        "okstream_keepalive" to 300,
        "okstream_clean_session" to false,
        "okstream_session_expiry_interval" to 2147483647,
        "okstream_reconnect_period" to 1,
        "okstream_max_reconnect_delay" to 120,
        "okstream_tracking_sampling_rate" to 100,
        FRC_KEY_VOICE_COLLECTION_SAMPLE_RATE to 32000,
        FRC_KEY_OKPL_AUDIO_SAMPLE_MINIMUM_DURATION_MILLIS to 3_000,
        FRC_KEY_OKPL_AUDIO_SAMPLE_MAXIMUM_DURATION_MILLIS to 30_000,
        "unread_chat_max_limit_count" to 100,
        FILE_STORAGE_TRACKING_SAMPLING to if (BuildConfig.DEBUG) 100 else 0,
        DEVICE_MEMORY_TRACKING_SAMPLING to if (BuildConfig.DEBUG) 100 else 0,
        FRC_KEY_NON_CRITICAL_DATA_WORKER_RATE_LIMIT_HOURS to 72, // 3 days
        FRC_KEY_HOME_DATA_SYNC_WORKER_RATE_LIMIT_HOURS to 2, // 2 hours
        FRC_KEY_PERIODIC_DATA_SYNC_WORKER_RATE_LIMIT_HOURS to 24, // 1 day
        FRC_KEY_NON_ACTIVE_BUSINESSES_DATA_SYNC_WORKER_RATE_LIMIT_HOURS to 24, // 1 day
        "contextual_note_keywords" to "gpay,g pay,google,phonepe,phone pe,paytm,upi,googlepay",
        MAX_COUNT_PER_REQUEST_FOR_SYNC_CUSTOMER_KEY to 5,
        MAX_RETRY_COUNT_OF_SYNC_CUSTOMER_KEY to 3,
        NETWORK_INSTRUMENTATION_SAMPLING to 1,
        FRC_HElP_NUMBER_PAYMENT to 8296508123L,
        FRC_HElP_CHAT_NUMBER_PAYMENT to 8296508123L,
        FRC_PAYMENT_24X7 to "24x7",
        WebViewUtils.CONFIG_WHITELISTED_DOMAINS to WebViewUtils.DEFAULT_WHITELISTED_DOMAINS,
        "delay_in_pre_network_tool_tip" to 10,
        FRC_NON_ACTIVE_BUSINESSES_DATA_SYNCER_ENABLED to true,
        FRC_MAX_NUMBER_OF_BUSINESSES to 50,
        "bulk_reminder_v2_defaulted_since" to "14",
        "network_error_retry_backoff_delay_millis" to 100,
        "network_error_max_try_count" to 3,
        FRC_SHARE_REPORT_MAX_POLLING_TIME_IN_SECONDS_KEY to 30,
        FRC_MAX_EXPANDED_QR_SHOWN_COUNT to 5,
        IS_SOCIAL_VALIDATION_ENABLED to false,
        HelpConstants.FRC_SUPPORT_NUMBER_KEY to "9916515152",
    )
}
