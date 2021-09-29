package tech.okcredit.home.widgets.filter_option.usecase

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test
import tech.okcredit.home.widgets.filter_option.data.FilterOptionRepository

class GetFilterEducationVisibilityTest {
    private val enableFilterOptionVisibility: EnableFilterOptionVisibility = mock()
    private val filterOptionRepository: FilterOptionRepository = mock()

    private val getFilterEducationVisibility =
        GetFilterEducationVisibility({ filterOptionRepository }, { enableFilterOptionVisibility })

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `Usecase Should Return false when Filter Option is not Visible`() {
        // Given
        whenever(enableFilterOptionVisibility.canEnabledFilterOption()).thenReturn(Observable.just(false))
        whenever(filterOptionRepository.canShowFilterEducation()).thenReturn(Single.just(false))
        whenever(filterOptionRepository.setFilterEducationPreference(any())).thenReturn(Completable.complete())

        // when
        val testObserver =
            getFilterEducationVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false when Filter Option is Visible`() {
        // Given
        whenever(enableFilterOptionVisibility.canEnabledFilterOption()).thenReturn(Observable.just(true))
        whenever(filterOptionRepository.canShowFilterEducation()).thenReturn(Single.just(false))
        whenever(filterOptionRepository.setFilterEducationPreference(any())).thenReturn(Completable.complete())

        // when
        val testObserver =
            getFilterEducationVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return false for Relogin from Different Number`() {
        // Given
        whenever(enableFilterOptionVisibility.canEnabledFilterOption()).thenReturn(Observable.just(false))
        whenever(filterOptionRepository.canShowFilterEducation()).thenReturn(Single.just(true))
        whenever(filterOptionRepository.setFilterEducationPreference(any())).thenReturn(Completable.complete())

        // when
        val testObserver =
            getFilterEducationVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(false)
        )

        testObserver.dispose()
    }

    @Test
    fun `Usecase Should Return true Filter Education is already Shown`() {
        // Given
        whenever(enableFilterOptionVisibility.canEnabledFilterOption()).thenReturn(Observable.just(true))
        whenever(filterOptionRepository.canShowFilterEducation()).thenReturn(Single.just(true))
        whenever(filterOptionRepository.setFilterEducationPreference(any())).thenReturn(Completable.complete())

        // when
        val testObserver =
            getFilterEducationVisibility.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }
}
