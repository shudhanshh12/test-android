package `in`.okcredit.collection_ui.ui.referral.education

import `in`.okcredit.collection.contract.ReferralEducationPreference
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.ui.TestViewModel
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.utils.ScreenName
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.feature_help.contract.GetContextualHelpIds

class ReferralEducationViewModelTest :
    TestViewModel<ReferralEducationContract.State, ReferralEducationContract.PartialState, ReferralEducationContract.ViewEvents>() {
    private val referralEducationPreference: ReferralEducationPreference = mock()
    private val getContextualHelpIds: GetContextualHelpIds = mock()
    private val collectionTracker: CollectionTracker = mock()
    private val initialState: ReferralEducationContract.State = mock()

    override fun createViewModel(): BaseViewModel<ReferralEducationContract.State, ReferralEducationContract.PartialState, ReferralEducationContract.ViewEvents> {
        return ReferralEducationViewModel(
            initialState,
            { referralEducationPreference },
            { getContextualHelpIds },
            { collectionTracker },
        )
    }

    @Test
    fun `increase pref targeted_referral_education_shown when page loads first time`() {

        whenever(referralEducationPreference.setReferralEducationShown())
            .thenReturn(Completable.complete())

        pushIntent(ReferralEducationContract.Intent.Load)

        verify(referralEducationPreference).setReferralEducationShown()
    }

    @Test
    fun `go to referral list screen when click on invite button`() {

        pushIntent(ReferralEducationContract.Intent.InviteClicked)

        assertLastViewEvent(ReferralEducationContract.ViewEvents.GotoReferralListScreen)
    }

    @Test
    fun `help clicked open help screen`() {
        val ids = listOf<String>()
        whenever(getContextualHelpIds.execute(ScreenName.CollectionTargetedReferralScreen.value))
            .thenReturn(Observable.just(ids))

        pushIntent(ReferralEducationContract.Intent.HelpClicked, awaitCount = 2)

        verify(getContextualHelpIds).execute(ScreenName.CollectionTargetedReferralScreen.value)

        assertLastViewEvent(ReferralEducationContract.ViewEvents.HelpClicked(ids))
    }
}
