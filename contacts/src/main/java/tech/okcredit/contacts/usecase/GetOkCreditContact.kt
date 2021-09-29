package tech.okcredit.contacts.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.data.OkCrediContact
import javax.inject.Inject

class GetOkCreditContact @Inject constructor(
    private val remoteSource: Lazy<ContactsRemoteSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, OkCrediContact> {

    override fun execute(req: Unit): Observable<Result<OkCrediContact>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                remoteSource.get().getOkCreditContact(businessId)
                    .map {
                        OkCrediContact(exist = false, name = it.okc_name, number = it.okc_number)
                    }
            }
        )
    }
}
