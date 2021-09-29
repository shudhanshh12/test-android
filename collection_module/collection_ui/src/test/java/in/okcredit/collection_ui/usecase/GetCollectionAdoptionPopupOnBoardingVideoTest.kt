package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.shared.service.keyval.KeyValService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCollectionAdoptionPopupOnBoardingVideoTest {

    private val keyValService: KeyValService = mock()
    private val getCollectionAdoptionPopupOnBoardingVideo = GetCollectionAdoptionPopupOnBoardingVideo(keyValService)

    @Test
    fun `return video url of Collection adoption v2 onBoarding`() {
        val videoUrl = "{\"ca_education_video_1\":\"https://youtube.com\"}"
        whenever(keyValService.contains(eq("notification.server_version"), any())).thenReturn(Single.just(true))
        whenever(keyValService[eq("notification.server_version"), any()]).thenReturn(Observable.just(videoUrl))

        val testObserver = getCollectionAdoptionPopupOnBoardingVideo.execute().test()

        testObserver.assertValue("https://youtube.com")
        verify(keyValService).contains(eq("notification.server_version"), any())
        verify(keyValService)[eq("notification.server_version"), any()]
        testObserver.dispose()
    }

    @Test
    fun `return exception url or throw exception of Collection adoption v2 onBoarding`() {
        val videoUrl = "{\"caa_education_video_1\":\"https://youtube.com\"}"
        whenever(keyValService.contains(eq("notification.server_version"), any())).thenReturn(Single.just(true))
        whenever(keyValService[eq("notification.server_version"), any()]).thenReturn(Observable.just(videoUrl))

        val testObserver = getCollectionAdoptionPopupOnBoardingVideo.execute().test()

        testObserver.assertError {
            it is CollectionServerErrors.VideoNotFoundException
        }
        verify(keyValService).contains(eq("notification.server_version"), any())
        verify(keyValService).get(eq("notification.server_version"), any())
        testObserver.dispose()
    }
}
