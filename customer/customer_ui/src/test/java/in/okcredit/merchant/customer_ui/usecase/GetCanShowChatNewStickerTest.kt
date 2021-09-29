package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetCanShowChatNewStickerTest {

    private lateinit var testScheduler: TestScheduler
    private val ab: AbRepository = mock()

    private val getCanShowChatNewSticker = GetCanShowChatNewSticker(Lazy { ab })

    @Before
    @Throws(Exception::class)
    fun setUp() {
        testScheduler = TestScheduler()
        RxJavaPlugins.reset()
    }

    @After
    fun tearDown() = RxJavaPlugins.reset()

    @Test
    fun `Should return false if experiment is disabled`() {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        whenever(ab.isExperimentEnabled("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just(false)
        )

        whenever(ab.getExperimentVariant("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just("")
        )

        val testObserver = getCanShowChatNewSticker.execute(Unit).map {
            when (it) {
                is Result.Success -> it.value
                else -> false
            }
        }.test()
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, false)
        testObserver.assertValueAt(1, false)
        testObserver.dispose()
    }

    @Test
    fun `Should return false if experiment is enabled and variant is default`() {

        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        whenever(ab.isExperimentEnabled("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just(true)
        )

        whenever(ab.getExperimentVariant("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just("")
        )

        val testObserver = getCanShowChatNewSticker.execute(Unit).map {
            when (it) {
                is Result.Success -> it.value
                else -> false
            }
        }.test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, false)
        testObserver.assertValueAt(1, false)
        testObserver.dispose()
    }

    @Test
    fun `Should return true if experiment is enabled and variant is show`() {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        whenever(ab.isExperimentEnabled("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just(true)
        )

        whenever(ab.getExperimentVariant("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just("show")
        )

        val testObserver = getCanShowChatNewSticker.execute(Unit).map {
            when (it) {
                is Result.Success -> it.value
                else -> false
            }
        }.test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, false)
        testObserver.assertValueAt(1, true)
        testObserver.dispose()
    }

    @Test
    fun `Should return false if experiment is enabled and variant is dont show`() {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        whenever(ab.isExperimentEnabled("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just(true)
        )

        whenever(ab.getExperimentVariant("ui_experiment-all-chat_toolbar_new_label")).thenReturn(
            Observable.just("dont_show")
        )

        val testObserver = getCanShowChatNewSticker.execute(Unit).map {
            when (it) {
                is Result.Success -> it.value
                else -> false
            }
        }.test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, false)
        testObserver.assertValueAt(1, false)
        testObserver.dispose()
    }
}
