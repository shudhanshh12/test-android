package tech.okcredit.home.widgets.quick_add_card.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.widgets.quick_add_card.data.QuickAddCardRepository
import tech.okcredit.home.widgets.quick_add_card.utils.QuickAddCardHelper

class GetQuickAddCardVisibilityTest {
    private val ab: AbRepository = mock()
    private val quickAddCardRepository: QuickAddCardRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"
    private val getQuickAddCardVisibility =
        GetQuickAddCardVisibility({ ab }, { quickAddCardRepository }, { getActiveBusinessId })

    @Before
    fun setup() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `Usecase Should Return true when ab is enabled and quickAddCard Shares Preference is true`() {
        // Given
        whenever(ab.isFeatureEnabled(QuickAddCardHelper.FEATURE_KEY)).thenReturn(Observable.just(true))
        whenever(quickAddCardRepository.shouldShowQuickAddCard(businessId)).thenReturn(Observable.just(true))

        // When
        val testObserver =
            getQuickAddCardVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when ab is not enabled and quickAddCard Shares Preference is false`() {
        // Given
        whenever(ab.isFeatureEnabled(QuickAddCardHelper.FEATURE_KEY)).thenReturn(Observable.just(false))
        whenever(quickAddCardRepository.shouldShowQuickAddCard(businessId)).thenReturn(Observable.just(false))

        // When
        val testObserver =
            getQuickAddCardVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when ab is not enabled`() {
        // Given
        whenever(ab.isFeatureEnabled(QuickAddCardHelper.FEATURE_KEY)).thenReturn(Observable.just(false))
        whenever(quickAddCardRepository.shouldShowQuickAddCard(businessId)).thenReturn(Observable.just(true))

        // When
        val testObserver =
            getQuickAddCardVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }
}
