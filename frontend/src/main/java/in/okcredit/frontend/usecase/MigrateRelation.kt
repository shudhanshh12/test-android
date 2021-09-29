package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MigrateRelation @Inject constructor(
    private val postSupplierMigration: PostSupplierMigration,
    private val remoteSource: BackendRemoteSource,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<MigrateRelation.Request, String> {

    private var body: String = ""
    override fun execute(req: Request): Observable<Result<String>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            migrateRelation(req, businessId)
        }
    }

    private fun migrateRelation(req: Request, businessId: String): Observable<Result<String>> {
        return UseCase.wrapObservable(
            remoteSource.migrate(
                req.merchantId,
                req.accountId,
                req.destiantion,
                businessId
            ).doAfterSuccess {
                body = it.account.id
                req.porgressSubject.onNext(10)
            }.flatMapObservable {
                postSupplierMigration.execute(
                    PostSupplierMigration.Request(
                        req.accountId,
                        it.account.id,
                        req.porgressSubject
                    )
                ).doOnComplete {
                    req.porgressSubject.onNext(90)
                }.flatMap {
                    Observable.timer(2, TimeUnit.SECONDS).doOnComplete {
                        req.porgressSubject.onNext(100)
                    }
                }
            }.map {
                body
            }
        )
    }

    data class Request(
        val merchantId: String,
        val accountId: String,
        val destiantion: Int,
        val porgressSubject: PublishSubject<Int>,
    )
}
