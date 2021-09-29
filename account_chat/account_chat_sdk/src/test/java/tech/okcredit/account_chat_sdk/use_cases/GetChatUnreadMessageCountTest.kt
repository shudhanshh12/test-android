package tech.okcredit.account_chat_sdk.use_cases

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import tech.okcredit.account_chat_contract.STRING_CONSTANTS
import tech.okcredit.account_chat_sdk.ChatProvider

class GetChatUnreadMessageCountTest {

    private val getActiveBusiness: GetActiveBusiness = mock()

    private val chatConnected: ChatProvider = mock()

    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()

    lateinit var getChatUnreadMessageCount: GetChatUnreadMessageCount

    @Before
    fun setup() {
        getChatUnreadMessageCount = spy(GetChatUnreadMessageCount({ getActiveBusiness }, { firebaseRemoteConfig }))
    }

    @Test
    fun `return pair`() {
        Mockito.doReturn(Observable.just(Result.Success<Pair<String, String?>>("1" to "abc")))
            .`when`(getChatUnreadMessageCount).execute(("lkfv"))
        getChatUnreadMessageCount.execute(("lkfv")).test()
            .assertValues(Result.Success<Pair<String, String?>>("1" to "abc"))
    }

    @Test
    fun `return mocked value 1 and abc`() {
        Mockito.doReturn(Observable.just("1" to "abc")).`when`(getChatUnreadMessageCount).getUnreadCountFromFirestore(
            any(),
            any()
        )

        Mockito.doReturn(Observable.just(getDummyMerchant())).`when`(getChatUnreadMessageCount).getDistinctMerchant()

        Mockito.doReturn(Observable.just(true)).`when`(getChatUnreadMessageCount).getChatStatus()

        `when`(getChatUnreadMessageCount.execute("lnv")).thenCallRealMethod()
        Assert.assertNotEquals(getChatUnreadMessageCount, null)
        val nullableDummy: String? = "abc"

        val testObserver = getChatUnreadMessageCount.execute(("lnv")).test()

        testObserver.assertValueAt(0, Result.Progress(0))
        testObserver.assertValueAt(1, Result.Success("1" to nullableDummy))
    }

    @Test
    fun `return no value when filter returns false`() {
        Mockito.doReturn(Observable.just("1" to "abc")).`when`(getChatUnreadMessageCount).getUnreadCountFromFirestore(
            any(),
            any()
        )

        Mockito.doReturn(Observable.just(getDummyMerchant())).`when`(getChatUnreadMessageCount).getDistinctMerchant()

        Mockito.doReturn(Observable.just(false)).whenever(chatConnected).isConnected()

        `when`(getChatUnreadMessageCount.execute("lnv")).thenCallRealMethod()
        Assert.assertNotEquals(getChatUnreadMessageCount, null)

        val testObserver = getChatUnreadMessageCount.execute(("lnv")).test()

        testObserver.assertValueCount(1)
        testObserver.assertValueAt(0, Result.Progress(0))
    }

    @Test
    fun `return only one value when provided by two dummy merchants which are both same `() {
        Mockito.doReturn(Observable.just("1" to "abc")).`when`(getChatUnreadMessageCount).getUnreadCountFromFirestore(
            any(),
            any()
        )

        Mockito.doReturn(Observable.just(getDummyMerchant(), getDummyMerchant())).`when`(getChatUnreadMessageCount)
            .getDistinctMerchant()

        Mockito.doReturn(Observable.just(true)).whenever(chatConnected).isConnected()

        `when`(getChatUnreadMessageCount.execute("lnv")).thenCallRealMethod()
        Assert.assertNotEquals(getChatUnreadMessageCount, null)

        val testObserver = getChatUnreadMessageCount.execute(("lnv")).test()

        testObserver.assertValueCount(1)
        testObserver.assertValueAt(0, Result.Progress(0))
    }

    @Test
    fun `getUnreadCountMap when relation is provided`() {
        val response = hashMapOf<String, Long>("TEST_ACCOUNT_ID" to 1)
        Mockito.doReturn(Observable.just(getDummyMerchant())).`when`(getActiveBusiness).execute()
        Mockito.doReturn(Observable.just(getDummyMerchant())).`when`(getChatUnreadMessageCount).getDistinctMerchant()
        Mockito.doReturn(Observable.just(response)).`when`(getChatUnreadMessageCount)
            .getUnreadCountFromFireStoreForRelation(
                any(),
                any()
            )

        getChatUnreadMessageCount.getUnreadCountForRelation(STRING_CONSTANTS.SELLER).test().apply {
            assertValueAt(0, response)
        }
    }

    private fun getDummyMerchant(): `in`.okcredit.merchant.contract.Business {
        return `in`.okcredit.merchant.contract.Business(
            "vv",
            createdAt = DateTime.now(),
            name = ";kvmfv",
            mobile = "vkmfv"
        )
    }
}
