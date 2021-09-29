package tech.okcredit.android.referral

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralHelper
import `in`.okcredit.referral.contract.ReferralHelper.REFERRAL_LINK
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ReferralInfo
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import android.content.Context
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.utils.FileUtils
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.server.ReferralServer
import tech.okcredit.android.referral.store.ReferralLocalSource
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ReferralRepositoryImpl @Inject constructor(
    private val localSource: Lazy<ReferralLocalSource>,
    private val server: Lazy<ReferralServer>,
    private val referralEventTracker: Lazy<ReferralEventTracker>,
    private val fileUtils: Lazy<FileUtils>,
    private val context: Lazy<Context>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val getReferralVersionImpl: Lazy<GetReferralVersionImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : ReferralRepository {

    companion object {
        const val TAG = "<<<<ReferralSDK"
    }

    private fun clearIdentity(): Completable = localSource.get().clearIdentity().doOnComplete {
        Timber.d("$TAG Identity Cleared")
    }

    private fun clearFileCache() =
        Completable.fromAction {
            File(context.get().getExternalFilesDir(null), ReferralHelper.LOCAL_FOLDER_NAME).deleteRecursively()
        }

    override fun signOut(): Completable = clearIdentity().andThen(clearFileCache())

    override fun getReferralInfo(): Observable<ReferralInfo> =
        getActiveBusinessId.get().execute()
            .flatMapObservable { localSource.get().getReferralInfo() }.doOnNext {
                Timber.d("$TAG ReferralInfo emitted from store")
            }

    override fun getQualifiedForJourney(businessId: String): Single<Boolean> {
        return localSource.get().getQualifiedForJourney().firstOrError()
            .flatMap {
                if (it == Int.MIN_VALUE) {
                    checkQualificationJourney(businessId).onErrorComplete()
                        .andThen(localSource.get().getQualifiedForJourney().firstOrError())
                } else {
                    Single.just(it)
                }
            }
            .map { it > 0 }
    }

    override fun sync(businessId: String): Completable {
        return server.get().getReferralInfo(businessId)
            .doAfterSuccess {
                Timber.d("$TAG ReferralInfo Api Success maxAmount=${it.maxAmount} referralPrice=${it.referralPrice}")
            }
            .flatMapCompletable {
                localSource.get().setReferralInfo(it)
            }
            .andThen(rxCompletable { syncReferralContent(businessId) })
            .doOnComplete {
                Timber.d("$TAG ReferralInfo Latest Value Stored")
            }
    }

    override fun checkQualificationJourney(businessId: String): Completable {
        return server.get().checkJourneyQualification(businessId)
            .doOnSuccess {
                Timber.d("$TAG Referral journey qualification Success $it")
            }.doOnError {
                Timber.e("$TAG Referral journey qualification status Error")
            }.flatMapCompletable {
                referralEventTracker.get().trackReferredUserQualification(it)
                localSource.get().setQualifiedForJourney(it)
            }.doOnComplete {
                Timber.d("$TAG Referral journey qualification stored")
            }.doOnError {
                Timber.e("$TAG Referral journey qualification save failed")
            }
    }

    override fun isReferralInAppDisplayed(): Single<Boolean> {
        return localSource.get().isReferralInAppDisplayed()
    }

    override fun setReferralInAppPreference(shown: Boolean): Completable {
        return localSource.get().setsReferralInAppAsPreference(shown)
    }

    override suspend fun getTargetedUsers(businessId: String): List<TargetedUser> {
        return try {
            val targetedUsers = server.get().getTargetedUsers(businessId)
            localSource.get().setTargetedUsers(targetedUsers)
            targetedUsers
        } catch (e: Exception) {
            localSource.get().getTargetedUsers()
        }
    }

    override suspend fun getShareContent(
        targetedUserId: String?,
        businessId: String,
    ): ReferralApiMessages.GetShareContentResponse {
        return try {
            server.get().getShareContent(targetedUserId, businessId)
        } catch (e: Exception) {
            ReferralApiMessages.GetShareContentResponse(null, getGenericShareContent())
        }
    }

    override suspend fun setGenericShareContent(response: ReferralApiMessages.GetShareContentResponse) {
        localSource.get().setGenericShareContent(response)
    }

    override suspend fun getGenericShareContent() = localSource.get().getGenericShareContent()

    override suspend fun getReferralTargets(businessId: String): List<ReferralTargetBanner> {
        return try {
            server.get().getReferralTarget(businessId).also { localSource.get().setReferralTarget(it) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            localSource.get().getReferralTarget()
        }
    }

    override suspend fun syncShareAppImage() {
        withContext(dispatcherProvider.get().io()) {
            val localFile = FileUtils.getLocalFile(
                context.get(),
                ReferralHelper.LOCAL_FOLDER_NAME,
                getReferralVersionImpl.get().getShareAppImageName()
            )
            fileUtils.get().getImageUriFromRemote(
                file = localFile,
                localFolderName = ReferralHelper.LOCAL_FILE_NAME,
                localFileName = getReferralVersionImpl.get().getShareAppImageName(),
                remoteUrl = getReferralVersionImpl.get().getShareAppImagePath()
            ).await()
        }
    }

    override suspend fun setTargetBannerCloseTime(currentTimeMillis: Long) =
        localSource.get().setTargetBannerClosedAt(currentTimeMillis)

    override suspend fun getTargetBannerCloseTime() = localSource.get().getTargetBannerClosedAt()

    override suspend fun setTransactionInitiatedTime(currentTimeMillis: Long) =
        localSource.get().setTransactionInitiatedTime(currentTimeMillis)

    override suspend fun getTransactionInitiatedTime() = localSource.get().getTransactionInitiatedTime()

    override suspend fun shouldShowShareNudge(): Boolean {
        return localSource.get().shouldShowShareNudge()
    }

    override suspend fun setShareNudge(shouldShowNudge: Boolean) {
        localSource.get().setShareNudge(shouldShowNudge)
    }

    override suspend fun syncReferralContent(businessId: String) {
        val response = server.get().getShareContent(businessId = businessId)
        setGenericShareContent(response)
        withContext(dispatcherProvider.get().io()) {
            val localFile = FileUtils.getLocalFile(
                context.get(),
                ReferralHelper.LOCAL_FOLDER_NAME, ReferralHelper.LOCAL_FILE_NAME
            )
            fileUtils.get().getImageUriFromRemote(
                file = localFile,
                localFolderName = ReferralHelper.LOCAL_FOLDER_NAME,
                localFileName = ReferralHelper.LOCAL_FILE_NAME,
                remoteUrl = response.genericContent.imageUrl
            ).await()
        }
    }

    override fun saveReferralLink(referralLink: String): Completable {
        return localSource.get().setReferralLink(referralLink)
    }

    override fun getReferralLink(businessId: String): Observable<String> {
        return localSource.get().getReferralLinkOrEmpty()
            .take(1)
            .filter { it.isNotEmpty() }
            .switchIfEmpty(
                server.get().getReferralLink(businessId)
                    .flatMapObservable {
                        localSource.get().setReferralLink(it).andThen(Observable.just(it))
                    }
            )
            .onErrorResumeNext(Observable.just(REFERRAL_LINK))
    }

    override fun syncReferralLink(businessId: String): Completable {
        return server.get().getReferralLink(businessId)
            .flatMapCompletable {
                localSource.get().setReferralLink(it)
            }
    }
}
