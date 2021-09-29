package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_DENIED
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_SHOWN
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences

class CheckOnlineEducationToShowTest {

    private val rxSharedPreference: DefaultPreferences = mock()

    private val collectionRepository: CollectionRepository = mock()

    private val checkOnlineEducationToShow =
        CheckOnlineEducationToShow({ rxSharedPreference }, { collectionRepository })

    @Test
    fun `should not show if collection is not activated`() {

        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_SHOWN), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_DENIED), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        val testObserver = checkOnlineEducationToShow.execute().test()

        Truth.assertThat(testObserver.values().first() == false)
        testObserver.dispose()
    }

    @Test
    fun `should not show if education already shown`() {
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_SHOWN), any(), anyOrNull()))
            .thenReturn(flowOf(true))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_DENIED), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        val testObserver = checkOnlineEducationToShow.execute().test()

        Truth.assertThat(testObserver.values().first() == false)
        testObserver.dispose()
    }

    @Test
    fun `should not show if education already denied`() {
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_SHOWN), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_DENIED), any(), anyOrNull()))
            .thenReturn(flowOf(true))

        val testObserver = checkOnlineEducationToShow.execute().test()

        Truth.assertThat(testObserver.values().first() == false)
        testObserver.dispose()
    }

    @Test
    fun `should show if collection is not denied and not shown`() {
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_SHOWN), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        whenever(rxSharedPreference.getBoolean(eq(IS_ONLINE_COLLECTION_EDUCATION_DENIED), any(), anyOrNull()))
            .thenReturn(flowOf(false))

        val testObserver = checkOnlineEducationToShow.execute().test()

        Truth.assertThat(testObserver.values().first() == true)
        testObserver.dispose()
    }
}
