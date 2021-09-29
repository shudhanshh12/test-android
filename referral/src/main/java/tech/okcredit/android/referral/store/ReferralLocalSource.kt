package tech.okcredit.android.referral.store

import `in`.okcredit.referral.contract.ReferralHelper
import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ReferralInfo
import `in`.okcredit.referral.contract.models.ShareContent
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import android.content.Context
import com.f2prateek.rx.preferences2.Preference
import com.google.gson.Gson
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.awaitFirstOrDefault
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.extensions.fromJson
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.referral.R
import javax.inject.Inject

class ReferralLocalSource @Inject constructor(
    private val referralPreferences: Lazy<ReferralPreferences>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val context: Lazy<Context>,
) {

    companion object {
        private const val PREF_INDIVIDUAL_PROFILE = "referral_info"
        private const val PREF_INDIVIDUAL_REFERRAL_LINK = "referral_link"
        private const val PREF_INDIVIDUAL_QUALIFIED_FOR_JOURNEY_V2 = "qualified_for_journey_v2"
        private const val PREF_INDIVIDUAL_REFERRAL_IN_APP_DISPLAYED = "referral_inapp_displayed"
        private const val PREF_INDIVIDUAL_SHARE_CONTENT = "share_content"
        private const val PREF_INDIVIDUAL_TARGETED_USERS = "targeted_users"
        private const val PREF_INDIVIDUAL_REFERRAL_TARGET = "referral_target"
        private const val PREF_INDIVIDUAL_TARGET_BANNER_CLOSED_AT = "target_banner_closed_at"
        private const val PREF_INDIVIDUAL_TRANSACTION_INITIATED_TIME = "transaction_initiated_time"
        private const val PREF_INDIVIDUAL_SHOW_SHARE_SCREEN_NUDGE = "should_show_share_nudge"
    }

    private val gson = Gson()

    private val profileCodec by lazy {
        object : Preference.Converter<ReferralInfo> {
            override fun deserialize(serialized: String): ReferralInfo =
                gson.fromJson(serialized, ReferralInfo::class.java)

            override fun serialize(value: ReferralInfo): String =
                gson.toJson(value)
        }
    }

    fun getReferralInfo(): Observable<ReferralInfo> = referralPreferences.get()
        .getObject(PREF_INDIVIDUAL_PROFILE, Scope.Individual, getDefaultReferralInfo(), profileCodec).asObservable()

    fun setReferralInfo(referralInfo: ReferralInfo): Completable {
        return rxCompletable {
            referralPreferences.get().set(PREF_INDIVIDUAL_PROFILE, referralInfo, Scope.Individual, profileCodec)
        }.subscribeOn(Schedulers.io())
    }

    fun getReferralLink(): Observable<String> = referralPreferences.get()
        .getString(PREF_INDIVIDUAL_REFERRAL_LINK, Scope.Individual, ReferralHelper.REFERRAL_LINK).asObservable()

    fun getReferralLinkOrEmpty(): Observable<String> = referralPreferences.get()
        .getString(PREF_INDIVIDUAL_REFERRAL_LINK, Scope.Individual).asObservable()

    fun setReferralLink(referralLink: String): Completable {
        return rxCompletable {
            referralPreferences.get().set(PREF_INDIVIDUAL_REFERRAL_LINK, referralLink, Scope.Individual)
        }.subscribeOn(Schedulers.io())
    }

    fun clearIdentity(): Completable {
        return rxCompletable {
            referralPreferences.get().remove(PREF_INDIVIDUAL_PROFILE, Scope.Individual)
            referralPreferences.get().remove(PREF_INDIVIDUAL_REFERRAL_LINK, Scope.Individual)
            referralPreferences.get().remove(PREF_INDIVIDUAL_TARGET_BANNER_CLOSED_AT, Scope.Individual)
            referralPreferences.get().remove(PREF_INDIVIDUAL_TRANSACTION_INITIATED_TIME, Scope.Individual)
            referralPreferences.get().remove(PREF_INDIVIDUAL_SHARE_CONTENT, Scope.Individual)
        }.subscribeOn(Schedulers.io())
    }

    fun setQualifiedForJourney(qualified: Int): Completable {
        return rxCompletable {
            referralPreferences.get().set(PREF_INDIVIDUAL_QUALIFIED_FOR_JOURNEY_V2, qualified, Scope.Individual)
        }.subscribeOn(Schedulers.io())
    }

    fun getQualifiedForJourney(): Observable<Int> = referralPreferences.get()
        .getInt(PREF_INDIVIDUAL_QUALIFIED_FOR_JOURNEY_V2, Scope.Individual, Int.MIN_VALUE)
        .asObservable().subscribeOn(Schedulers.io())

    fun isReferralInAppDisplayed(): Single<Boolean> {
        return referralPreferences.get().getBoolean(PREF_INDIVIDUAL_REFERRAL_IN_APP_DISPLAYED, Scope.Individual)
            .asObservable().firstOrError()
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun setsReferralInAppAsPreference(shown: Boolean) = rxCompletable {
        referralPreferences.get().set(PREF_INDIVIDUAL_REFERRAL_IN_APP_DISPLAYED, shown, Scope.Individual)
    }

    private fun getDefaultReferralInfo() = ReferralInfo(
        ReferralHelper.DEFAULT_REFERRAL_PRICE,
        ReferralHelper.DEFAULT_REFERRAL_MAX_PRICE,
        ReferralHelper.DEFAULT_STATUS
    )

    suspend fun setGenericShareContent(response: ReferralApiMessages.GetShareContentResponse) {
        referralPreferences.get().set(PREF_INDIVIDUAL_SHARE_CONTENT, gson.toJson(response.genericContent), Scope.Individual)
    }

    suspend fun getGenericShareContent(): ShareContent {
        return withContext(dispatcherProvider.get().io()) {
            val content = referralPreferences.get().getString(PREF_INDIVIDUAL_SHARE_CONTENT, Scope.Individual).first()
            return@withContext if (content.isBlank()) {
                getDefaultShareContent()
            } else {
                gson.fromJson(content, ShareContent::class.java) ?: getDefaultShareContent()
            }
        }
    }

    private suspend fun getDefaultShareContent(): ShareContent {
        return withContext(dispatcherProvider.get().io()) {
            val referralInfo = getReferralInfo().awaitFirstOrDefault(getDefaultReferralInfo())
            return@withContext ShareContent(
                context.get()
                    .getString(
                        R.string.default_referral_share_content,
                        TempCurrencyUtil.formatV2(referralInfo.referralPrice ?: ReferralHelper.DEFAULT_REFERRAL_PRICE)
                    ),
                ReferralHelper.REFERRAL_IMAGE_URL
            )
        }
    }

    suspend fun setTargetedUsers(targetedUsers: List<TargetedUser>) {
        referralPreferences.get().set(PREF_INDIVIDUAL_TARGETED_USERS, gson.toJson(targetedUsers), Scope.Individual)
    }

    suspend fun getTargetedUsers(): List<TargetedUser> {
        return withContext(dispatcherProvider.get().io()) {
            val json = referralPreferences.get().getString(PREF_INDIVIDUAL_TARGETED_USERS, Scope.Individual).first()
            gson.fromJson<List<TargetedUser>>(json ?: "") ?: emptyList()
        }
    }

    suspend fun setReferralTarget(referralTargetBanner: List<ReferralTargetBanner>) {
        referralPreferences.get().set(PREF_INDIVIDUAL_REFERRAL_TARGET, gson.toJson(referralTargetBanner), Scope.Individual)
    }

    suspend fun getReferralTarget(): List<ReferralTargetBanner> {
        return withContext(dispatcherProvider.get().io()) {
            val json = referralPreferences.get().getString(PREF_INDIVIDUAL_REFERRAL_TARGET, Scope.Individual).first()
            gson.fromJson<List<ReferralTargetBanner>>(json ?: "") ?: emptyList()
        }
    }

    suspend fun setTargetBannerClosedAt(currentTimeMillis: Long) {
        referralPreferences.get().set(PREF_INDIVIDUAL_TARGET_BANNER_CLOSED_AT, currentTimeMillis, Scope.Individual)
    }

    suspend fun getTargetBannerClosedAt(): Long {
        return withContext(dispatcherProvider.get().io()) {
            referralPreferences.get().getLong(PREF_INDIVIDUAL_TARGET_BANNER_CLOSED_AT, Scope.Individual).first()
        }
    }

    suspend fun setTransactionInitiatedTime(currentTimeMillis: Long) {
        referralPreferences.get().set(PREF_INDIVIDUAL_TRANSACTION_INITIATED_TIME, currentTimeMillis, Scope.Individual)
    }

    suspend fun getTransactionInitiatedTime(): Long {
        return withContext(dispatcherProvider.get().io()) {
            referralPreferences.get().getLong(PREF_INDIVIDUAL_TRANSACTION_INITIATED_TIME, Scope.Individual).first()
        }
    }

    suspend fun shouldShowShareNudge(): Boolean {
        return referralPreferences.get().getBoolean(PREF_INDIVIDUAL_SHOW_SHARE_SCREEN_NUDGE, Scope.Individual, true).first()
    }

    suspend fun setShareNudge(shouldShowNudge: Boolean) {
        referralPreferences.get().set(PREF_INDIVIDUAL_SHOW_SHARE_SCREEN_NUDGE, shouldShowNudge, Scope.Individual)
    }
}
