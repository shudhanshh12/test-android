package `in`.okcredit.merchant.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.contract.SetActiveBusinessId
import `in`.okcredit.voice_first.contract.ResetDraftTransactions
import android.app.Activity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.account_chat_contract.SignOutFirebaseAndRemoveChatListener
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.app_contract.LegacyNavigator
import java.lang.ref.WeakReference

class SwitchBusinessTest {
    private val setActiveBusinessId: SetActiveBusinessId = mock()
    private val tracker: Tracker = mock()
    private val legacyNavigator: LegacyNavigator = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val signOutFirebaseAndRemoveChatListener: SignOutFirebaseAndRemoveChatListener = mock()
    private val resetDraftTransactions: ResetDraftTransactions = mock()

    private val switchBusiness = SwitchBusiness(
        { setActiveBusinessId },
        { tracker },
        { legacyNavigator },
        { dispatcherProvider },
        { signOutFirebaseAndRemoveChatListener },
        { resetDraftTransactions }
    )
    private val businessId = "businessId"
    private val businessName = "businessName"

    @Before
    fun setup() {
        whenever(setActiveBusinessId.execute(any())).thenReturn(Completable.complete())
        whenever(dispatcherProvider.main()).thenReturn(Dispatchers.Unconfined)
        whenever(signOutFirebaseAndRemoveChatListener.execute()).thenReturn(Completable.complete())
    }

    @Test
    fun `given activity not passed should set active business id`() {
        runBlocking {
            switchBusiness.execute(businessId, businessName)

            verify(tracker).setIdentity(businessId, false)
            verify(setActiveBusinessId).execute(businessId)
            verify(legacyNavigator, times(0)).goToHome(any())
        }
    }

    @Test
    fun `given activity passed should set active business id and go to homescreen`() {
        runBlocking {
            val activity = mock<Activity>()
            val weakActivity = WeakReference(activity)

            switchBusiness.execute(businessId, businessName, weakActivity)

            verify(tracker).setIdentity(businessId, false)
            verify(setActiveBusinessId).execute(businessId)
            verify(legacyNavigator).goToHome(activity)
        }
    }
}
