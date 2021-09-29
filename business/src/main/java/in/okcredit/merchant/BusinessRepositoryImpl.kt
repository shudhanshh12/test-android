package `in`.okcredit.merchant

import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.*
import `in`.okcredit.merchant.server.BusinessRemoteServer
import `in`.okcredit.merchant.server.internal.ApiMessages
import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.merchant.store.database.DbEntityMapper.toDbBusiness
import `in`.okcredit.merchant.store.sharedprefs.BusinessPreferences
import `in`.okcredit.merchant.usecase.BusinessSyncer
import `in`.okcredit.merchant.usecase.GetActiveBusinessIdImpl
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.crashlytics.RecordException
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BusinessRepositoryImpl @Inject constructor(
    private val localSource: Lazy<BusinessLocalSource>,
    private val remoteServer: Lazy<BusinessRemoteServer>,
    private val syncer: Lazy<BusinessSyncer>,
    private val imageUploader: Lazy<IUploadFile>,
    private val businessPreference: Lazy<BusinessPreferences>,
) : BusinessRepository {

    override fun checkNewNumberExists(mobile: String, businessId: String): Single<NumberCheckResponse> {
        return remoteServer.get().checkNewNumber(mobile, businessId)
    }

    override fun scheduleSyncBusiness(businessId: String): Completable {
        return syncer.get().scheduleSyncBusiness(businessId)
    }

    override fun executeSyncBusiness(businessId: String): Completable {
        return syncer.get().executeSyncBusiness(businessId)
    }

    override fun scheduleSyncBusinessCategoriesAndBusinessTypes(businessId: String): Completable {
        return syncer.get().scheduleSyncBusinessCategoriesAndBusinessTypes(businessId)
    }

    fun getBusiness(businessId: String): Observable<Business> {
        return localSource.get().getBusiness(businessId)
    }

    override fun getBusinessList(): Observable<List<Business>> {
        return localSource.get().getBusinessList()
    }

    override fun getCategories(): Observable<List<Category>> {
        return localSource.get().getCategories()
            .flatMap {
                return@flatMap Observable.just(it)
            }
            .doOnError {
                RecordException.recordException(it)
                Timber.i(it)
            }
    }

    override fun getBusinessTypes(): Observable<List<BusinessType>> {
        return localSource.get().getBusinessTypes()
            .flatMap {
                return@flatMap Observable.just(it)
            }
            .doOnError {
                RecordException.recordException(it)
                Timber.i(it)
            }
    }

    private fun updateBusinessName(businessName: String, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = businessName,
                            create_time = local.createdAt
                        )
                    ),
                    update_display_name = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateBusinessNameOnboarding(businessName: String, businessId: String): Completable {

        return remoteServer.get().getBusiness(businessId)
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = businessName,
                            create_time = local.createdAt
                        )
                    ),
                    update_display_name = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(
                remoteServer.get().getBusiness(businessId)
                    .flatMapCompletable {
                        localSource.get().saveBusiness(it)
                    }
            )
    }

    private fun updatePersonName(personName: String, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = local.name,
                            create_time = local.createdAt
                        ),
                        contact_name = personName
                    ),
                    update_contact_name = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateProfileImage(profileImage: String, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val fileSingle = if (profileImage.isEmpty()) {
                    Single.just("")
                } else {
                    val receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID().toString() + ".jpg"
                    imageUploader.get().schedule(IUploadFile.CUSTOMER_PHOTO, receiptUrl, profileImage)
                        .andThen(
                            Single.just(receiptUrl)
                        )
                }
                fileSingle
                    .flatMapCompletable {
                        val apiRequest = ApiMessages.UpdateBusinessRequest(
                            business_user_id = local.id,
                            business_user = ApiMessages.GetBusinessResponseWrapper(
                                user = ApiMessages.BusinessUser(
                                    id = local.id,
                                    mobile = local.mobile,
                                    display_name = local.name,
                                    profile_image = it,
                                    create_time = local.createdAt
                                )
                            ),
                            update_profile_image = true
                        )
                        remoteServer.get().updateBusiness(apiRequest, businessId)
                    }
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateEmail(email: String, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                // if user updating the same email , the we get 'email already exit error' from server
                // to manage this case, we don't send to server
                if (email == local.email) {
                    Completable.complete()
                } else {
                    val apiRequest = ApiMessages.UpdateBusinessRequest(
                        business_user_id = local.id,
                        business_user = ApiMessages.GetBusinessResponseWrapper(
                            user = ApiMessages.BusinessUser(
                                id = local.id,
                                mobile = local.mobile,
                                display_name = local.name,
                                email = email,
                                create_time = local.createdAt
                            )
                        ),
                        update_email = true
                    )
                    remoteServer.get().updateBusiness(apiRequest, businessId)
                }
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateCategory(categoryId: String?, categoryName: String?, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = local.name,
                            create_time = local.createdAt
                        ),
                        business_category = ApiMessages.Category(
                            id = categoryId,
                            name = categoryName
                        )
                    ),
                    update_category = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateAbout(about: String, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = local.name,
                            create_time = local.createdAt,
                            about = about
                        ),
                    ),
                    update_about = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateBusinessType(businessTypeId: String, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = local.name,
                            create_time = local.createdAt
                        ),
                        business_type = ApiMessages.Business(id = businessTypeId)
                    ),
                    update_business_type = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    private fun updateAddress(address: String, latitude: Double, longitude: Double, businessId: String): Completable {
        return localSource.get().getBusiness(businessId)
            .firstOrError()
            .flatMapCompletable { local ->
                val apiRequest = ApiMessages.UpdateBusinessRequest(
                    business_user_id = local.id,
                    business_user = ApiMessages.GetBusinessResponseWrapper(
                        user = ApiMessages.BusinessUser(
                            id = local.id,
                            mobile = local.mobile,
                            display_name = local.name,
                            create_time = local.createdAt,
                            address = ApiMessages.Address(
                                text = address,
                                address_latitude = latitude,
                                address_longitude = longitude,
                            )
                        ),
                    ),
                    update_address = true
                )
                remoteServer.get().updateBusiness(apiRequest, businessId)
            }
            .andThen(syncer.get().executeSyncBusiness(businessId))
    }

    override fun updateBusiness(updateBusinessRequest: UpdateBusinessRequest, businessId: String): Completable {
        return when (updateBusinessRequest) {
            is UpdateBusinessRequest.UpdateBusinessName -> updateBusinessName(
                updateBusinessRequest.businessName,
                businessId
            )
            is UpdateBusinessRequest.UpdateBusinessNameOnboarding -> updateBusinessNameOnboarding(
                updateBusinessRequest.businessName,
                businessId
            )
            is UpdateBusinessRequest.UpdateName -> updatePersonName(updateBusinessRequest.personName, businessId)
            is UpdateBusinessRequest.UpdateProfileImage -> updateProfileImage(
                updateBusinessRequest.profileImage,
                businessId
            )
            is UpdateBusinessRequest.UpdateCategory -> updateCategory(
                updateBusinessRequest.categoryId,
                updateBusinessRequest.categoryName,
                businessId
            )
            is UpdateBusinessRequest.UpdateEmail -> updateEmail(updateBusinessRequest.email, businessId)
            is UpdateBusinessRequest.UpdateAddress -> updateAddress(
                updateBusinessRequest.address,
                updateBusinessRequest.latitude,
                updateBusinessRequest.longitude,
                businessId
            )
            is UpdateBusinessRequest.UpdateAbout -> updateAbout(updateBusinessRequest.about, businessId)
            is UpdateBusinessRequest.UpdateBusinessType -> updateBusinessType(
                updateBusinessRequest.businessTypeId,
                businessId
            )
        }
    }

    override fun clearLocalData(): Completable {
        return localSource.get().deleteBusinessTable()
            .andThen(clearDefaultBusinessIdCache())
            .andThen(clearBusinessSharedPreferences())
    }

    private fun clearBusinessSharedPreferences(): Completable {
        return rxCompletable { businessPreference.get().clear() }
    }

    private fun clearDefaultBusinessIdCache() = rxCompletable {
        GetActiveBusinessIdImpl.clearCache()
    }

    override fun refreshLanguageInCategories(businessId: String): Completable {
        return syncer.get().scheduleSyncBusiness(businessId)
            .andThen(syncer.get().scheduleSyncBusinessCategoriesAndBusinessTypes(businessId))
    }

    override suspend fun isMerchantActivated(): Boolean {
        return localSource.get().getBusinessActivated()
            .takeIf { it }
            ?: suspend {
                try {
                    remoteServer.get().isMerchantActivated().isActivated.also {
                        localSource.get().setBusinessActivated(it)
                    }
                } catch (e: Exception) {
                    false // store.getMerchantActivated() will always be false
                }
            }.invoke()
    }

    fun getBusinessIdList(): Flow<List<String>> {
        return localSource.get().getBusinessIdList()
    }

    suspend fun createBusiness(name: String, businessId: String): Business {
        return remoteServer.get().createBusiness(name, businessId)
    }

    fun saveBusiness(business: Business): Completable {
        return localSource.get().saveBusiness(business.toDbBusiness())
    }
}
