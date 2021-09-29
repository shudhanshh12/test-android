package tech.okcredit.home.widgets.quick_add_card.usecase

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.home.widgets.quick_add_card.data.QuickAddCardRepository

class GetQuickAddCardToolTipVisibilityTest {
    private val quickAddCardRepository: QuickAddCardRepository = mock()
    private val getQuickAddCardToolTipVisibility =
        GetQuickAddCardToolTipVisibility(Lazy { quickAddCardRepository })

    @Test
    fun `Usecase should return True when isToolTipShown shared preference is false`() {
        // Given
        whenever(quickAddCardRepository.isToolTipShown()).thenReturn(Observable.just(false))

        // When
        val testObserver =
            getQuickAddCardToolTipVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase should return false when isToolTipShown shared preference is true`() {
        // Given
        whenever(quickAddCardRepository.isToolTipShown()).thenReturn(Observable.just(true))

        // When
        val testObserver =
            getQuickAddCardToolTipVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }
}
