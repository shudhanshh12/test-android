package tech.okcredit.android.ab

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import tech.okcredit.android.ab.sdk.ABExperimentsVariants
import tech.okcredit.android.ab.store.AbLocalSource
import tech.okcredit.android.ab.usecase.AcknowledgeExperiment
import tech.okcredit.android.ab.usecase.SyncAbProfile
import tech.okcredit.android.ab.workers.ClearAbData
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.workmanager.OkcWorkManager

class AbRepositoryTest {

    private val localSource: AbLocalSource = mock()
    private val okcWorkManager: OkcWorkManager = mock()
    private val syncAbProfile: SyncAbProfile = mock()
    private val acknowledgeExperiment: AcknowledgeExperiment = mock()
    private val localeManager: LocaleManager = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val clearAbData: ClearAbData = mock()
    lateinit var abRepository: AbRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(getActiveBusinessId.thisOrActiveBusinessId(anyOrNull())).thenReturn(Single.just(TestData.BUSINESS_ID))

        abRepository = AbRepositoryImpl(
            { localSource },
            { syncAbProfile },
            { acknowledgeExperiment },
            { okcWorkManager },
            { localeManager },
            { getActiveBusinessId },
            { clearAbData }
        )
    }

    @Test
    fun `should return feature enable for enable features`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isFeatureEnabled(TestData.FEATURE_1).test()

        // expectations
        assertThat(testObserver.values().first() == true).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `should return feature disable for disabled features`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isFeatureEnabled(TestData.FEATURE_2).test()

        // expectations
        assertThat(testObserver.values().first() == false).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `should return feature disable for unknown features`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isFeatureEnabled("unknown").test()

        // expectations
        assertThat(testObserver.values().first() == false).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `isFeatureEnabled() should not return any value on un auth user`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isFeatureEnabled(TestData.FEATURE_1, true).test()

        // expectations
        assertThat(testObserver.values().isEmpty()).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `isFeatureEnabled() should return error on get store profile is failing`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.error(TestData.ERROR))

        val testObserver = abRepository.isFeatureEnabled(TestData.FEATURE_1).test()

        // expectations
        assertThat(testObserver.errors().first() == TestData.ERROR).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `enabledFeatures() should return enabled features`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.enabledFeatures().test()

        // expectations
        assertThat(testObserver.values().first() == listOf(TestData.FEATURE_1, TestData.FEATURE_3)).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `enabledFeatures() should return error on get store profile is failing`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.error(TestData.ERROR))
        val testObserver = abRepository.enabledFeatures().test()

        // expectations
        assertThat(testObserver.errors().first() == TestData.ERROR).isTrue()
        testObserver.dispose()
    }

    /************************* Experiments **********************/

    @Test
    fun `isExperimentEnabled() should return true for enabled experiments`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isExperimentEnabled(TestData.EXP_1).test()

        // expectations
        assertThat(testObserver.values().first() == true).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `isExperimentEnabled() should return false for unknown experiments`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isExperimentEnabled("unknown").test()

        // expectations
        assertThat(testObserver.values().first() == false).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `isExperimentEnabled() return true for enabled experiments for un auth user`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isExperimentEnabled(TestData.EXP_1).test()

        // expectations
        assertThat(testObserver.values().first() == true).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `isExperimentEnabled() should return any value on error about identity`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.isExperimentEnabled(TestData.EXP_1).test()

        // expectations
        assertThat(testObserver.values().first()).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `isExperimentEnabled() should return error on get store profile is failing`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.error(TestData.ERROR))
        val testObserver = abRepository.isExperimentEnabled(TestData.EXP_1).test()

        // expectations
        assertThat(testObserver.errors().first() == TestData.ERROR).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `getProfileSingle() should return profile values from store`() {
        `when`(localSource.getProfileSingle(TestData.BUSINESS_ID)).thenReturn(Single.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.getProfile(TestData.BUSINESS_ID).test()

        // expectations
        assertThat(testObserver.values().first() == TestData.TEST_AB_PROFILE).isTrue()
        verify(localSource).getProfileSingle(TestData.BUSINESS_ID)
        testObserver.dispose()
    }

    @Test
    fun `getProfileSingle() should return error on if get profile is failing`() {
        `when`(localSource.getProfileSingle(TestData.BUSINESS_ID)).thenReturn(Single.error(TestData.ERROR))
        val testObserver = abRepository.getProfile(TestData.BUSINESS_ID).test()

        // expectations
        assertThat(testObserver.errors().first() == TestData.ERROR).isTrue()
        testObserver.dispose()
    }

    /************************* Activation And Add Txn Experiments **********************/

    @Test
    fun `getUiExperimentActivationAndAddTxnVariant() should return variant if present`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        val testObserver = abRepository.getUiExperimentActivationAndAddTxnVariant().test()

        // expectations
        assertThat(
            testObserver.values().first() == TestData.EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN_VARIANT
        ).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `getUiExperimentActivationAndAddTxnVariant() should return default variant if not present`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE_WITHOUT_EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN))
        val testObserver = abRepository.getUiExperimentActivationAndAddTxnVariant().test()

        // expectations
        assertThat(
            testObserver.values()
                .first() == ABExperimentsVariants.UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN_VARIANTS.DEFAULT.value
        ).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `getUiExperimentActivationAndAddTxnVariant() should return error if get profile is failing`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.error(TestData.ERROR))

        val testObserver = abRepository.getUiExperimentActivationAndAddTxnVariant().test()

        // expectations
        assertThat(
            testObserver.values()
                .isEmpty()
        ).isTrue()
        testObserver.dispose()
    }

    /************************* Experiment **********************/

    @Test
    fun `getExperimentVariant should return right variant for a experiment`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))

        val testObserver = abRepository.getExperimentVariant(TestData.EXP_1, TestData.BUSINESS_ID).test()

        // expectations
        assertThat(testObserver.values().first() == TestData.VAR_1).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `getExperimentVariant should return if getProfile() fails`() {
        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.error(TestData.ERROR))

        val testObserver = abRepository.getExperimentVariant(TestData.EXP_1, TestData.BUSINESS_ID).test()

        // expectations
        assertThat(testObserver.errors()[0] == TestData.ERROR).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `startLanguageExperiment() should call recordExperimentStarted() if resource id is present in experiment for english language`() {
        val stringId = "add_credit"

        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE_WITH_LANG_EXP))
        `when`(localSource.startedExperiments(TestData.BUSINESS_ID)).thenReturn(Observable.just(listOf()))
        `when`(
            localSource.recordExperimentStarted(
                TestData.EXP_LANGUAGE,
                TestData.BUSINESS_ID
            )
        ).thenReturn(Completable.complete())
        `when`(localeManager.getLanguage()).thenReturn(TestData.LANG_EN)

        abRepository.startLanguageExperiment(stringId, TestData.BUSINESS_ID)

        // expectations
        verify(localSource).recordExperimentStarted(TestData.EXP_LANGUAGE, TestData.BUSINESS_ID)
    }

    @Test
    fun `startLanguageExperiment() should call recordExperimentStarted() if resource id is present in experiment for malayalam language`() {
        val stringId = "add_credit"

        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE_WITH_LANG_EXP))
        `when`(localSource.startedExperiments(TestData.BUSINESS_ID)).thenReturn(Observable.just(listOf()))
        `when`(
            localSource.recordExperimentStarted(
                TestData.EXP_LANGUAGE,
                TestData.BUSINESS_ID
            )
        ).thenReturn(Completable.complete())
        `when`(localeManager.getLanguage()).thenReturn(TestData.LANG_ML)

        abRepository.startLanguageExperiment(stringId, TestData.BUSINESS_ID)

        // expectations
        verify(localSource, never()).recordExperimentStarted(TestData.EXP_LANGUAGE, TestData.BUSINESS_ID)
    }

    @Test
    fun `startLanguageExperiment() should call recordExperimentStarted() if resource id is not present in experiment`() {
        val stringId = "add_credit"

        `when`(localSource.getProfile(TestData.BUSINESS_ID)).thenReturn(Observable.just(TestData.TEST_AB_PROFILE))
        `when`(localSource.startedExperiments(TestData.BUSINESS_ID)).thenReturn(Observable.just(listOf()))
        `when`(
            localSource.recordExperimentStarted(
                TestData.EXP_LANGUAGE,
                TestData.BUSINESS_ID
            )
        ).thenReturn(Completable.complete())

        abRepository.startLanguageExperiment(stringId, TestData.BUSINESS_ID)

        // expectations
        verify(localSource, never()).recordExperimentStarted(TestData.EXP_LANGUAGE, TestData.BUSINESS_ID)
    }
}
