package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.*
import android.util.Patterns
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class UpdateBusinessImpl @Inject constructor(
    private val businessApi: BusinessRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UpdateBusiness {

    override fun execute(req: Request): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            updateBusiness(req, businessId)
        }
    }

    private fun updateBusiness(req: Request, businessId: String): Completable {
        when (req.inputType) {
            BusinessConstants.BUSINESS_NAME -> {
                return businessApi.updateBusiness(
                    UpdateBusinessRequest.UpdateBusinessName(req.updatedValue!!),
                    businessId
                )
            }

            BusinessConstants.EMAIL -> {
                return validateEmail(req.updatedValue!!)
                    .flatMapCompletable {
                        businessApi.updateBusiness(UpdateBusinessRequest.UpdateEmail(req.updatedValue!!), businessId)
                    }
            }

            BusinessConstants.ADDRESS -> {
                return businessApi.updateBusiness(
                    UpdateBusinessRequest.UpdateAddress(
                        req.address?.first!!,
                        req.address?.second!!,
                        req.address?.third!!
                    ),
                    businessId
                )
            }

            BusinessConstants.ABOUT -> {
                return businessApi.updateBusiness(UpdateBusinessRequest.UpdateAbout(req.updatedValue!!), businessId)
            }

            BusinessConstants.PERSON_NAME -> {
                return businessApi.updateBusiness(UpdateBusinessRequest.UpdateName(req.updatedValue!!), businessId)
            }

            BusinessConstants.PROFILE_IMAGE -> {
                return businessApi.updateBusiness(
                    UpdateBusinessRequest.UpdateProfileImage(req.updatedValue!!),
                    businessId
                )
            }

            BusinessConstants.CATEGORY -> {
                return businessApi.updateBusiness(
                    UpdateBusinessRequest.UpdateCategory(
                        req.category?.first,
                        req.category?.second
                    ),
                    businessId
                )
            }

            BusinessConstants.BUSINESS_TYPE -> {
                return businessApi.updateBusiness(
                    UpdateBusinessRequest.UpdateBusinessType(req.businessType?.id!!),
                    businessId
                )
            }

            BusinessConstants.OTHER_CATEGORY -> {
                return if (req.category?.second.isNullOrBlank()) { // if enter category name is empty , that means  user wants to remove category
                    businessApi.updateBusiness(UpdateBusinessRequest.UpdateCategory(null, null), businessId)
                } else {
                    businessApi.updateBusiness(
                        UpdateBusinessRequest.UpdateCategory(
                            req.category?.first,
                            req.category?.second
                        ),
                        businessId
                    )
                }
            }

            else -> {
                return businessApi.updateBusiness(
                    UpdateBusinessRequest.UpdateBusinessName(req.updatedValue!!),
                    businessId
                )
            }
        }
    }
}

internal fun validateEmail(email: String): Single<String> {
    return when {
        email.isEmpty() -> Single.just("")
        isValidEmail(email).not() -> Single.error<String>(BusinessErrors.InvalidEmail())
        else -> Single.just("")
    }
}

fun isValidEmail(text: String) =
    Patterns.EMAIL_ADDRESS.matcher(text).matches() && text.isNotNullOrBlank()
