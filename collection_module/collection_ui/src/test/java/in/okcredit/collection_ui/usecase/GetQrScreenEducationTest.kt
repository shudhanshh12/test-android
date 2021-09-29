package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException

class GetQrScreenEducationTest {

    private val ab: AbRepository = mock()
    private val collectionRepository: CollectionRepository = mock()

    private val getQrScreenEducation: GetQrScreenEducation =
        GetQrScreenEducation({ ab }, collectionRepository)

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit
    }

    @Test
    fun `execute() should return send education tap target type`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.SaveSend, QrCodeContract.EducationType.TapTarget)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TAP_TARGET
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return online collection education tap target type`() {
        val response =
            GetQrScreenEducation.Response(
                QrCodeContract.Education.OnlineCollection,
                QrCodeContract.EducationType.TapTarget
            )

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TAP_TARGET
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return menu education tap target type`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.Menu, QrCodeContract.EducationType.TapTarget)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TAP_TARGET
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return send education tool tip type`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.SaveSend, QrCodeContract.EducationType.ToolTip)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TOOLTIP
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return online collection education tool tip type`() {
        val response =
            GetQrScreenEducation.Response(
                QrCodeContract.Education.OnlineCollection,
                QrCodeContract.EducationType.ToolTip
            )

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TOOLTIP
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return menu education tool tip type`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.Menu, QrCodeContract.EducationType.ToolTip)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TOOLTIP
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return none when variant mismatch`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.None, QrCodeContract.EducationType.TapTarget)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                "mismatch"
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `getNextEducation() should return online collection education if qr first enabled`() {
        val response =
            GetQrScreenEducation.Response(
                QrCodeContract.Education.OnlineCollection,
                QrCodeContract.EducationType.ToolTip
            )
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)

        Truth.assertThat(getQrScreenEducation.getNextEducation(QrCodeContract.EducationType.ToolTip) == response)
    }

    @Test
    fun `getNextEducation() should return menu education if qr first disabled`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.Menu, QrCodeContract.EducationType.ToolTip)
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)

        Truth.assertThat(getQrScreenEducation.getNextEducation(QrCodeContract.EducationType.ToolTip) == response)
    }

    /*@Test
    fun `execute() should return return menu education if feature is disabled and send education already shown`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.Menu, QrCodeContract.EducationType.TapTarget)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TAP_TARGET
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }
*/
    @Test
    fun `execute() should return return none if experiment is disabled`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.None, QrCodeContract.EducationType.TapTarget)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(false))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TAP_TARGET
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(false)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(false)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }

    @Test
    fun `execute() should return return none if all educations are already shown`() {
        val response =
            GetQrScreenEducation.Response(QrCodeContract.Education.None, QrCodeContract.EducationType.TapTarget)

        Truth.assertThat(GetQrScreenEducation.EXPT_NAME == "postlogin_android-all-qr_first_education")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TAP_TARGET == "tap_and_target")
        Truth.assertThat(GetQrScreenEducation.VARIANT_TOOLTIP == "tool_tip")

        whenever(ab.isExperimentEnabled(GetQrScreenEducation.EXPT_NAME)).thenReturn(Observable.just(false))
        whenever(ab.getExperimentVariant(GetQrScreenEducation.EXPT_NAME)).thenReturn(
            Observable.just(
                GetQrScreenEducation.VARIANT_TAP_TARGET
            )
        )
        whenever(collectionRepository.canShowQrEducation()).thenReturn(Single.just(true))
        whenever(collectionRepository.isQrOnlineCollectionEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrSaveSendEducationShown()).thenReturn(true)
        whenever(collectionRepository.isQrMenuEducationShown()).thenReturn(true)

        val testObserver = getQrScreenEducation.execute(Unit).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        testObserver.dispose()
    }
}
