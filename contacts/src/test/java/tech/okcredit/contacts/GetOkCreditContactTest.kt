package tech.okcredit.contacts

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.contacts.data.OkCrediContact
import tech.okcredit.contacts.server.OkCreditContactResponse
import tech.okcredit.contacts.usecase.GetOkCreditContact

class GetOkCreditContactTest {
    private val remoteSource: ContactsRemoteSource = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getOkCreditContact = GetOkCreditContact({ remoteSource }, { getActiveBusinessId })

    val okCreditContactResponse = OkCreditContactResponse("okc_name", "okc_number")

    @Test
    fun execute() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(remoteSource.getOkCreditContact(businessId)).thenReturn(Single.just(okCreditContactResponse))
        val result = getOkCreditContact.execute(Unit).subscribeOn(Schedulers.trampoline()).test()
        result.assertValues(
            Result.Progress(),
            Result.Success(OkCrediContact(exist = false, name = "okc_name", number = "okc_number"))
        )
        result.dispose()
    }
}
