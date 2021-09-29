package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import `in`.okcredit.fileupload.usecase.IUploadAudioSampleFile
import `in`.okcredit.home.HomePreferences
import android.net.Uri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class CollectVoiceSamplesFromNotesTest {

    private val abRepository = mock<AbRepository>()
    private val preference = mock<HomePreferences>()
    private val firebaseRemoteConfig = mock<FirebaseRemoteConfig>()
    private val uploadAudioSampleFile = mock<IUploadAudioSampleFile>()
    private val collectVoiceSamplesFromNotes = CollectVoiceSamplesFromNotes(
        { abRepository }, { preference }, { firebaseRemoteConfig }, { uploadAudioSampleFile }
    )

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `shouldCollectVoiceSamplesFromNotes feature disabled should return false`() {
        // Given
        whenever(abRepository.isFeatureEnabled("collect_voice_samples_from_notes"))
            .thenReturn(Observable.just(false))

        // When
        val testObserver = collectVoiceSamplesFromNotes.shouldCollectVoiceSamplesFromNotes().test()

        // Then
        testObserver.assertValue(false)
        verify(abRepository).isFeatureEnabled("collect_voice_samples_from_notes")
    }

    @Test
    fun `shouldCollectVoiceSamplesFromNotes feature enabled and count lt 6 should return true`() {
        // Given
        whenever(abRepository.isFeatureEnabled("collect_voice_samples_from_notes"))
            .thenReturn(Observable.just(true))
        whenever(firebaseRemoteConfig.getLong("voice_samples_from_notes_max_count")).thenReturn(6)
        whenever(preference.getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull()))
            .thenReturn(flowOf(4))

        // When
        val testObserver = collectVoiceSamplesFromNotes.shouldCollectVoiceSamplesFromNotes().test()

        // Then
        testObserver.assertValue(true)
        verify(abRepository).isFeatureEnabled("collect_voice_samples_from_notes")
        verify(firebaseRemoteConfig).getLong("voice_samples_from_notes_max_count")
        verify(preference).getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull())
    }

    @Test
    fun `shouldCollectVoiceSamplesFromNotes feature enabled and count = 6 should return false`() {
        // Given
        whenever(abRepository.isFeatureEnabled("collect_voice_samples_from_notes"))
            .thenReturn(Observable.just(true))
        whenever(firebaseRemoteConfig.getLong("voice_samples_from_notes_max_count")).thenReturn(6)
        whenever(preference.getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull()))
            .thenReturn(flowOf(6))

        // When
        val testObserver = collectVoiceSamplesFromNotes.shouldCollectVoiceSamplesFromNotes().test()

        // Then
        testObserver.assertValue(false)
        verify(abRepository).isFeatureEnabled("collect_voice_samples_from_notes")
        verify(firebaseRemoteConfig).getLong("voice_samples_from_notes_max_count")
        verify(preference).getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull())
    }

    @Test
    fun `shouldCollectVoiceSamplesFromNotes feature enabled and count gt 6 should return false`() {
        // Given
        whenever(abRepository.isFeatureEnabled("collect_voice_samples_from_notes"))
            .thenReturn(Observable.just(true))
        whenever(firebaseRemoteConfig.getLong("voice_samples_from_notes_max_count")).thenReturn(6)
        whenever(preference.getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull()))
            .thenReturn(flowOf(8))

        // When
        val testObserver = collectVoiceSamplesFromNotes.shouldCollectVoiceSamplesFromNotes().test()

        // Then
        testObserver.assertValue(false)
        verify(abRepository).isFeatureEnabled("collect_voice_samples_from_notes")
        verify(firebaseRemoteConfig).getLong("voice_samples_from_notes_max_count")
        verify(preference).getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull())
    }

    @Test
    fun `incrementVoiceSampleCollectedCount should increment count`() {
        runBlocking {
            // When
            val testObserver = collectVoiceSamplesFromNotes.incrementVoiceSampleCollectedCount().test()

            // Then
            testObserver.assertComplete()
            verify(preference).increment(eq("voice_samples_from_notes_collected_count"), any())
        }
    }

    @Test
    fun `optOut should set count to 100`() {
        runBlocking {
            // When
            val testObserver = collectVoiceSamplesFromNotes.optOut().test()

            // Then
            testObserver.assertComplete()
            verify(preference).set(eq("voice_samples_from_notes_collected_count"), eq(100), any())
        }
    }

    @Test
    fun `scheduleUpload should schedule on uploadAudioSampleFile`() {
        runBlocking {
            // Given
            val uri = mock<Uri>()
            val path = "sample-path"
            val transcribedText = "sample-transcribedText"
            val noteText = "sample-noteText"
            val transactionId = "sample-transactionId"
            val sampleCount = 1
            whenever(uploadAudioSampleFile.createLocalFile(eq(uri), any())).thenReturn(Single.just(path))
            whenever(uploadAudioSampleFile.schedule(any(), eq(path), any())).thenReturn(Completable.complete())
            whenever(preference.getInt(eq("voice_samples_from_notes_collected_count"), any(), anyOrNull()))
                .thenReturn(flowOf(sampleCount))

            // When
            val testObserver =
                collectVoiceSamplesFromNotes.scheduleUpload(uri, transcribedText, noteText, transactionId).test()

            // Then
            testObserver.assertComplete()
            verify(uploadAudioSampleFile).createLocalFile(eq(uri), any())
            verify(uploadAudioSampleFile).schedule(any(), eq(path), any())
            verify(preference).increment(eq("voice_samples_from_notes_collected_count"), any())
        }
    }
}
