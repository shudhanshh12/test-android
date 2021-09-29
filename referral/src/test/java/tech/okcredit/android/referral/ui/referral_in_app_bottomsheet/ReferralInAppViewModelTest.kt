package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet

import `in`.okcredit.shared.base.BaseViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test
import tech.okcredit.android.referral.TestViewModel
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppContract.*
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.usecase.SetReferralInAppShown

class ReferralInAppViewModelTest : TestViewModel<State, PartialState, ViewEvent>() {

    private val mockSetReferralInAppShown: SetReferralInAppShown = mock()

    override fun createViewModel(): BaseViewModel<State, PartialState, ViewEvent> {
        return ReferralInAppViewModel { mockSetReferralInAppShown }
    }

    @Test
    fun `Load Intent Should Call SetReferralInAppShown UseCase`() {

        whenever(mockSetReferralInAppShown.execute()).thenReturn(Completable.complete())

        pushIntent(Intent.Load)

        verify(mockSetReferralInAppShown, times(1)).execute()
    }
}
