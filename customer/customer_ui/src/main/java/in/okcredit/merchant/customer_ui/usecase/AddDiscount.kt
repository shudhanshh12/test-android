package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.android.auth.usecases.VerifyPassword
import java.util.*
import javax.inject.Inject

class AddDiscount @Inject constructor(
    private val verifyPassword: VerifyPassword,
    private val remoteSource: BackendRemoteSource,
    private val syncTransactionsImpl: SyncTransactionsImpl,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<AddDiscount.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                addDiscountAfterVerification(req, businessId)
            }
        )
    }

    private fun addDiscountAfterVerification(
        req: Request,
        businessId: String,
    ): Completable {
        var authCheck = Completable.complete()
        if (req.isVerifyPasswordRequired) {
            authCheck = verifyPassword.execute(req.password)
        }
        val requestId = UUID.randomUUID().toString()
        return authCheck
            .andThen(
                remoteSource.createDiscount(
                    req.customerId,
                    requestId,
                    req.amountv2,
                    req.note,
                    businessId
                )
            )
            .andThen(syncTransactionsImpl.execute("add_discount", null, false, businessId))
    }

    data class Request(
        val customerId: String,
        val amountv2: Long,
        val note: String?,
        val password: String?,
        val isVerifyPasswordRequired: Boolean,
    )
}
