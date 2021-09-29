package tech.okcredit.home.widgets.quick_add_card

import `in`.okcredit.shared.usecase.UseCase
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.home.widgets.quick_add_card.usecase.HideQuickAddCard

class RemoveQuickAddCardViewModelTest {
    private val hideQuickAddCard: HideQuickAddCard = mock()
    private lateinit var viewModel: RemoveQuickAddCardViewModel

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        viewModel = RemoveQuickAddCardViewModel { hideQuickAddCard }
    }

    @Test
    fun `should emit dismiss event when remove quick add card intent is attached`() {
        whenever(hideQuickAddCard.execute()).thenReturn(
            UseCase.wrapObservable(Observable.just(Unit))
        )

        // when
        val viewEventObserver = viewModel.viewEvent().test()

        viewModel.attachIntents(
            Observable.just(RemoveQuickAddCardIntent.RemoveQuickAddCard)
        )

        Truth.assertThat(
            viewEventObserver.values().last()
        ).isEqualTo(RemoveQuickAddCardViewEvent.Dismiss)

        viewEventObserver.dispose()
    }
}
