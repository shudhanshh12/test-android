package tech.okcredit.android.referral.utils

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.language.LocaleManager

class ReferralHelperTest {
    private val ab: AbRepository = mock()
    private val localeManager: LocaleManager = mock()
    private val referralHelper = GetReferralVersionImpl(Lazy { ab }, Lazy { localeManager })

//    @Test
//    fun `when Feature & Experiment enable should return variant V1`() {
//        whenever(ab.isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.isExperimentEnabled(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.getExperimentVariant(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(ReferralVersion.V1.name))
//
//        val testObserver = referralHelper.getReferralVersion().test()
//        testObserver.assertResult(ReferralVersion.fromValue(ReferralVersion.V1.name))
//
//        verify(ab).isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD)
//        verify(ab).getExperimentVariant(ReferralHelper.EXPERIMENT_NAME)
//
//        testObserver.dispose()
//    }
//
//
//    @Test
//    fun `when Feature & Experiment enable should return variant V2`() {
//        whenever(ab.isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.isExperimentEnabled(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.getExperimentVariant(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(ReferralVersion.V2.name))
//
//        val testObserver = referralHelper.getReferralVersion().test()
//        testObserver.assertResult(ReferralVersion.fromValue(ReferralVersion.V2.name))
//        verify(ab).isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD)
//        verify(ab).getExperimentVariant(ReferralHelper.EXPERIMENT_NAME)
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `when Feature & Experiment enable should return variant V0`() {
//        whenever(ab.isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.isExperimentEnabled(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.getExperimentVariant(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(ReferralVersion.V0.name))
//
//        val testObserver = referralHelper.getReferralVersion().test()
//        testObserver.assertResult(ReferralVersion.fromValue(ReferralVersion.V0.name))
//        verify(ab).isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD)
//        verify(ab).getExperimentVariant(ReferralHelper.EXPERIMENT_NAME)
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `when Feature enabled & Experiment not enable should return variant V2`() {
//        whenever(ab.isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD))
//            .thenReturn(Observable.just(true))
//
//        whenever(ab.isExperimentEnabled(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(false))
//
//        whenever(ab.getExperimentVariant(ReferralHelper.EXPERIMENT_NAME))
//            .thenReturn(Observable.just(ReferralVersion.V2.name))
//
//        val testObserver = referralHelper.getReferralVersion().test()
//        testObserver.assertResult(ReferralVersion.fromValue(ReferralVersion.V2.name))
//        verify(ab).isFeatureEnabled(ReferralHelper.FEATURE_REFERRAL_REWARD)
//        verify(ab, times(0)).getExperimentVariant(ReferralHelper.EXPERIMENT_NAME)
//        testObserver.dispose()
//    }

    @Test
    fun `should return image correct name`() {
        whenever(localeManager.getLanguage()).thenReturn("hi")

        val result = referralHelper.getShareAppImageName()

        assertThat(result).isEqualTo("share_app_image_hi.jpg")
    }

    @Test
    fun `should return hindi image when hindi language is selected`() {
        whenever(localeManager.isHindiLocale()).thenReturn(true)

        val result = referralHelper.getShareAppImagePath()

        assertThat(result).isEqualTo("https://storage.googleapis.com/okcredit-referral-images/images/android_images/Hindi%20Info.png")
    }

    @Test
    fun `should return english image when english language is selected`() {
        whenever(localeManager.isEnglishLocale()).thenReturn(true)

        val result = referralHelper.getShareAppImagePath()

        assertThat(result).isEqualTo("https://storage.googleapis.com/okcredit-referral-images/images/android_images/English%20Info.png")
    }

    @Test
    fun `should return marathi image when marathi language is selected`() {
        whenever(localeManager.isMarathiLocale()).thenReturn(true)

        val result = referralHelper.getShareAppImagePath()

        assertThat(result).isEqualTo("https://storage.googleapis.com/okcredit-referral-images/images/android_images/Marathi%20Info.png")
    }

    @Test
    fun `should return malayalam image when malayalam language is selected`() {
        whenever(localeManager.isMalayalamLocale()).thenReturn(true)

        val result = referralHelper.getShareAppImagePath()

        assertThat(result).isEqualTo("https://s3.ap-south-1.amazonaws.com/okcredit.app-assets/share_okc_ml.png")
    }

    @Test
    fun `should return image english image when language other than hindi, marathi, enlgish and malayalam is selected`() {
        whenever(localeManager.isMalayalamLocale()).thenReturn(false)
        whenever(localeManager.isMarathiLocale()).thenReturn(false)
        whenever(localeManager.isHindiLocale()).thenReturn(false)
        whenever(localeManager.isEnglishLocale()).thenReturn(false)

        val result = referralHelper.getShareAppImagePath()

        assertThat(result).isEqualTo("https://storage.googleapis.com/okcredit-referral-images/images/android_images/English%20Info.png")
    }
}
