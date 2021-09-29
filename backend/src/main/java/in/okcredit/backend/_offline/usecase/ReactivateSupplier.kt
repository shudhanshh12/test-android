package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ReactivateSupplier @Inject constructor(
    private val supplierCreditRepository: SupplierCreditRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<ReactivateSupplier.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(

            validateName(req.name)
                .flatMapCompletable {
                    getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                        supplierCreditRepository.reactivateSupplier(req.supplierId, req.name, businessId)
                    }
                }
        )
    }

    data class Request(
        val name: String,
        val supplierId: String
    )

    // If user wish to change the name of supplier while reactivating, then he will provide name . We will validate it
    // or else we will get existing name from db
    // This is just a reactivation , so name is not compulsory , we already have it in db
    private fun validateName(name: String?): Single<String> {
        return when {
            name.isNullOrBlank() -> Single.just(name)
            name.length > 30 -> Single.error<String>(SupplierCreditServerErrors.InvalidName())
            else -> Single.just(name)
        }
    }
}
