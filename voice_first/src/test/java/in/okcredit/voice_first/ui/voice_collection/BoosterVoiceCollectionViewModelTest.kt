package `in`.okcredit.voice_first.ui.voice_collection

import `in`.okcredit.fileupload.usecase.IUploadAudioSampleFile
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker
import `in`.okcredit.voice_first.usecase.GetVoiceBoosterText
import `in`.okcredit.voice_first.usecase.SubmitVoiceBoosterText
import `in`.okcredit.voice_first.usecase.VoiceRecorder
import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.permission.Permission
import java.util.concurrent.TimeUnit

class BoosterVoiceCollectionViewModelTest {

    private val voiceRecorder: VoiceRecorder = mock()
    private val context: Context = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val uploadAudioSampleFile: IUploadAudioSampleFile = mock()
    private val tracker: BoosterVoiceCollectionTracker = mock()
    private val getVoiceBoosterText: GetVoiceBoosterText = mock()
    private val submitVoiceBoosterText: SubmitVoiceBoosterText = mock()
    private lateinit var viewModel: BoosterVoiceCollectionViewModel
    private lateinit var testScheduler: TestScheduler
    lateinit var stateObserver: TestObserver<BoosterVoiceCollectionContract.State>
    lateinit var viewEventObserver: TestObserver<BoosterVoiceCollectionContract.ViewEvent>

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined

        viewModel = BoosterVoiceCollectionViewModel(
            { BoosterVoiceCollectionContract.State("") },
            { voiceRecorder },
            { context },
            { firebaseRemoteConfig },
            { uploadAudioSampleFile },
            { tracker },
            { getVoiceBoosterText },
            { submitVoiceBoosterText },
        )

        stateObserver = viewModel.state().test()
        viewEventObserver = viewModel.viewEvent().test()
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        stateObserver.dispose()
        viewEventObserver.dispose()
    }

//    @Test
//    fun `when Load intent is attached then recorderState should be GREETING`() {
//        runBlocking {
//            val voiceBoosterText = "text"
//            whenever(getVoiceBoosterText.execute()).thenReturn(voiceBoosterText)
//
//            // When
//            viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.Load))
//            testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
//
//            // Then
//            Assert.assertEquals(BoosterVoiceCollectionContract.RecorderState.GREETING,
//                stateObserver.values().last().recorderState)
//            verify(getVoiceBoosterText).execute()
//            assertEquals(voiceBoosterText, stateObserver.values().last().voiceMessageInput)
//        }
//    }

    @Test
    fun `when GreetingEducationCompleted intent is attached then recorderState should be IDLE`() {
        // When
        viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.GreetingEducationCompleted))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        Assert.assertEquals(
            BoosterVoiceCollectionContract.RecorderState.IDLE,
            stateObserver.values().last().recorderState
        )
    }

    @Test
    fun `when StartRecording intent is attached and record audio permission is not granted then emit RequestAudioPermission view event`() {
        // Given
        mockkObject(Permission)
        every { Permission.isRecordAudioPermissionAlreadyGranted(context) } returns false

        // When
        viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.StartRecording))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        viewEventObserver.assertValue(BoosterVoiceCollectionContract.ViewEvent.RequestAudioPermission)
    }

    @Test
    fun `when StartRecording intent is attached and record audio permission is granted then recorderState should be RECORDING`() {
        // Given
        mockkObject(Permission)
        every { Permission.isRecordAudioPermissionAlreadyGranted(context) } returns true

        // When
        viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.StartRecording))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        verify(voiceRecorder).startRecordingIntoFile(any())
        Assert.assertEquals(
            BoosterVoiceCollectionContract.RecorderState.RECORDING,
            stateObserver.values().last().recorderState
        )
    }

    @Test
    fun `when StopRecording intent is attached with duration = 1000 then recorderState should be INVALID`() {
        // Given
        whenever(firebaseRemoteConfig.getLong("okpl_audio_sample_minimum_duration_millis")).thenReturn(3000)

        // When
        viewModel.filePath = "filepath"
        viewModel.attachIntents(
            Observable.just(
                BoosterVoiceCollectionContract.Intent.StopRecording(
                    1000,
                    "sample-text"
                )
            )
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        Assert.assertEquals(
            BoosterVoiceCollectionContract.RecorderState.INVALID,
            stateObserver.values().last().recorderState
        )
    }

    @Test
    fun `when StopRecording intent is attached with duration = 4000 then recorderState should be COMPLETED`() {
        // Given
        whenever(firebaseRemoteConfig.getLong("okpl_audio_sample_minimum_duration_millis")).thenReturn(3000)
        whenever(uploadAudioSampleFile.schedule(any(), any(), any())).thenReturn(Completable.complete())

        // When
        viewModel.filePath = "filepath"
        viewModel.attachIntents(
            Observable.just(
                BoosterVoiceCollectionContract.Intent.StopRecording(
                    4000,
                    "sample-text"
                )
            )
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        verify(uploadAudioSampleFile).schedule(any(), any(), any())
        Assert.assertEquals(
            BoosterVoiceCollectionContract.RecorderState.COMPLETED,
            stateObserver.values().last().recorderState
        )
    }

    @Test
    fun `when CancelRecording intent is attached then recorderState should be IDLE`() {
        // When
        viewModel.filePath = "filepath"
        viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.CancelRecording))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        Assert.assertEquals(
            BoosterVoiceCollectionContract.RecorderState.IDLE,
            stateObserver.values().last().recorderState
        )
    }

    @Test
    fun `when TaskCompleted intent is attached then emit TaskCompleted view event`() {
        // When
        viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.TaskCompleted))
        testScheduler.advanceTimeBy(33, TimeUnit.SECONDS)

        // Then
        viewEventObserver.assertValue(`in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract.ViewEvent.TaskCompleted)
    }

    @Test
    fun `when RetryRecording intent is attached then recorderState should be IDLE`() {
        // When
        viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.RetryRecording))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // Then
        Assert.assertEquals(
            BoosterVoiceCollectionContract.RecorderState.IDLE,
            stateObserver.values().last().recorderState
        )
    }

    @Test
    fun `when submitting voice booster text`() {
        runBlocking {
            // When
            whenever(submitVoiceBoosterText.execute()).thenReturn(true)

            viewModel.attachIntents(Observable.just(BoosterVoiceCollectionContract.Intent.RecordSubmit))
            testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

            // Then
            verify(submitVoiceBoosterText).execute()
        }
    }
}
