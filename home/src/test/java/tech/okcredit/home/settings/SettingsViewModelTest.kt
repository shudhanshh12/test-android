package tech.okcredit.home.settings

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.backend.contract.Signout
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.PreferenceKey.FOUR_DIGIT_PIN
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.payment.contract.usecase.IsPspUpiFeatureEnabled
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.base.network.NetworkError
import tech.okcredit.contract.MerchantPrefSyncStatus
import tech.okcredit.home.TestData
import tech.okcredit.home.TestViewModel
import tech.okcredit.home.ui.settings.SettingsClicks
import tech.okcredit.home.ui.settings.SettingsContract
import tech.okcredit.home.ui.settings.SettingsViewModel
import tech.okcredit.home.ui.settings.usecase.ActiveLanguage
import tech.okcredit.home.ui.settings.usecase.CheckAppLock
import tech.okcredit.home.ui.settings.usecase.SetFingerprintLockStatus

class SettingsViewModelTest :
    TestViewModel<SettingsContract.State, SettingsContract.PartialState, SettingsContract.ViewEvent>() {

    private val checkNetworkHealth: GetConnectionStatus = mock()
    private val activeLanguage: ActiveLanguage = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val checkAppLock: CheckAppLock = mock()
    private val isPasswordSet: IsPasswordSet = mock()
    private val getMerchantPreference: GetMerchantPreference = mock()
    private val signout: Signout = mock()
    private val setFingerprintLockStatus: SetFingerprintLockStatus = mock()
    private val merchantPrefSyncStatus: MerchantPrefSyncStatus = mock()
    private val initialState = SettingsContract.State()
    private val isPspUpiFeatureEnabled: IsPspUpiFeatureEnabled = mock()

    override fun initDependencies() {
        super.initDependencies()
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `load intent should get required data`() {

        whenever(isPspUpiFeatureEnabled.execute()).thenReturn(Observable.just(true))
        whenever(checkNetworkHealth.execute()).thenReturn(Observable.just(Result.Success(true)))
        whenever(activeLanguage.execute()).thenReturn(Observable.just(1))
        whenever(merchantPrefSyncStatus.checkFingerPrintAvailability()).thenReturn(Observable.just(true))
        whenever(merchantPrefSyncStatus.checkFingerPrintEnable()).thenReturn(Observable.just(true))

        pushIntent(SettingsContract.Intent.Load)

        assertLastValue {
            it.isPspUpiFeatureEnabled && it.isLoading && it.activeLangugeStringId == 1 && it.isFingerPrintLockVisible && it.isFingerPrintEnabled
        }
    }

    @Test
    fun `load resume intent`() {

        whenever(checkAppLock.execute()).thenReturn(Observable.just(Pair(true, "CUSTOM_APP_LOCK")))
        whenever(checkNetworkHealth.execute()).thenReturn(Observable.just(Result.Success(true)))
        whenever(merchantPrefSyncStatus.checkMerchantPrefSync()).thenReturn(Single.just(true))
        whenever(getMerchantPreference.execute(FOUR_DIGIT_PIN)).thenReturn(Observable.just("true"))
        whenever(getMerchantPreference.execute(PreferenceKey.PAYMENT_PASSWORD)).thenReturn(Observable.just("true"))
        whenever(isPasswordSet.execute()).thenReturn(Single.just(true))

        pushIntent(SettingsContract.Intent.Resume)

        assertLastValue {
            it.isPaymentPasswordEnabled && it.appLockType == "CUSTOM_APP_LOCK" && !it.networkError && it.isMerchantPrefSync &&
                it.isFourDigitPinSet && it.isPaymentPasswordEnabled && it.isSetPassword
        }
    }

    @Test
    fun `when load syncMerchantPref is success`() {

        whenever(merchantPrefSyncStatus.execute()).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.SyncMerchantPref(SettingsClicks.FINGERPRINT_CLICK))

        assertLastValue {
            it.isMerchantPrefSync
        }
        assertLastViewEvent(SettingsContract.ViewEvent.SyncDone(SettingsClicks.FINGERPRINT_CLICK))
    }

    @Test
    fun `when load syncMerchantPref throws network error`() {

        whenever(merchantPrefSyncStatus.execute())
            .thenReturn(Completable.error(NetworkError("error", Throwable("network_error"))))

        pushIntent(SettingsContract.Intent.SyncMerchantPref(SettingsClicks.FINGERPRINT_CLICK))

        assertLastValue {
            it.networkError
        }
    }

    @Test
    fun `when load syncMerchantPref throws other error`() {

        whenever(merchantPrefSyncStatus.execute())
            .thenReturn(Completable.error(Exception("Some Error")))

        pushIntent(SettingsContract.Intent.SyncMerchantPref(SettingsClicks.FINGERPRINT_CLICK))

        assertLastValue {
            it.error
        }
    }

    @Test
    fun `when checkIsFourDigitPin returns true`() {

        whenever(getMerchantPreference.execute(FOUR_DIGIT_PIN)).thenReturn(Observable.just("true"))

        pushIntent(SettingsContract.Intent.CheckIsFourDigit(SettingsClicks.FINGERPRINT_CLICK))

        assertLastValue {
            it.isFourDigitPinSet
        }

        assertLastViewEvent(SettingsContract.ViewEvent.CheckFourDigitPinDone(SettingsClicks.FINGERPRINT_CLICK, true))
    }

    @Test
    fun `when load checkIsFourDigitPin throws network error`() {

        whenever(getMerchantPreference.execute(FOUR_DIGIT_PIN))
            .thenReturn(Observable.error(NetworkError("error", Throwable("network_error"))))

        pushIntent(SettingsContract.Intent.CheckIsFourDigit(SettingsClicks.FINGERPRINT_CLICK))

        assertLastValue {
            it.networkError
        }
    }

    @Test
    fun `when load checkIsFourDigitPin throws other error`() {

        whenever(getMerchantPreference.execute(FOUR_DIGIT_PIN))
            .thenReturn(Observable.error(Exception("Some Error")))

        pushIntent(SettingsContract.Intent.CheckIsFourDigit(SettingsClicks.FINGERPRINT_CLICK))

        assertLastValue {
            it.error
        }
    }

    @Test
    fun `load ProfileClick intent `() {

        pushIntent(SettingsContract.Intent.ProfileClick)

        assertLastViewEvent(SettingsContract.ViewEvent.GoToProfileScreen)
    }

    @Test
    fun `load AppLanguageClick intent `() {

        pushIntent(SettingsContract.Intent.AppLanguageClick)

        assertLastViewEvent(SettingsContract.ViewEvent.GoToLanguageScreen)
    }

    @Test
    fun `load AppLockClick intent `() {

        pushIntent(SettingsContract.Intent.AppLockClick)

        assertLastViewEvent(SettingsContract.ViewEvent.OpenAppLock)
    }

    @Test
    fun `load PaymentPasswordClick intent `() {

        pushIntent(SettingsContract.Intent.PaymentPasswordClick)

        assertLastViewEvent(SettingsContract.ViewEvent.GoToPaymentPasswordEnableScreen)
    }

    @Test
    fun `load SignOutFromAllDevices intent`() {

        pushIntent(SettingsContract.Intent.SignOutFromAllDevices)

        assertLastViewEvent(SettingsContract.ViewEvent.OpenLogoutConfirmationDialog)
    }

    @Test
    fun `load SignoutConfirmationClick is success`() {

        whenever(signout.execute(null)).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.SignoutConfirmationClick)

        assertLastValue {
            it.signOut
        }
    }

    @Test
    fun `when UpdatePasswordClick test password not empty`() {
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(TestData.MERCHANT))

        pushIntent(SettingsContract.Intent.UpdatePasswordClick(true))

        assertLastViewEvent(SettingsContract.ViewEvent.GotoResetPasswordScreen(TestData.MERCHANT.mobile, true))
    }

    @Test
    fun `load signOut intent`() {
        whenever(signout.execute(null)).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.SignOut)

        assertLastValue {
            it.signOut
        }
    }

    @Test
    fun `load ChangeNumberClick intent`() {
        whenever(signout.execute(null)).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.ChangeNumberClick)

        assertLastViewEvent(SettingsContract.ViewEvent.GoToChangeNumberScreen)
    }

    @Test
    fun `load SetNewPin intent`() {
        whenever(signout.execute(null)).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.SetNewPin(SettingsClicks.FINGERPRINT_CLICK))

        assertLastViewEvent(SettingsContract.ViewEvent.GoToSetNewPinScreen(SettingsClicks.FINGERPRINT_CLICK))
    }

    @Test
    fun `load UpdatePin intent`() {
        whenever(signout.execute(null)).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.UpdatePin(SettingsClicks.FINGERPRINT_CLICK))

        assertLastViewEvent(SettingsContract.ViewEvent.ShowUpdatePinDialog(SettingsClicks.FINGERPRINT_CLICK))
    }

    @Test
    fun `load setFingerPrintInSharedPref intent`() {

        whenever(setFingerprintLockStatus.execute(true)).thenReturn(Completable.complete())

        pushIntent(SettingsContract.Intent.SetFingerPrintEnable(true))

        assertLastValue {
            it.isFingerPrintEnabled
        }
    }

    override fun createViewModel(): BaseViewModel<SettingsContract.State, SettingsContract.PartialState, SettingsContract.ViewEvent> {
        return SettingsViewModel(
            initialState = initialState,
            checkNetworkHealth = checkNetworkHealth,
            activeLanguage = { activeLanguage },
            getActiveBusiness = { getActiveBusiness },
            checkAppLock = { checkAppLock },
            isPasswordSet = { isPasswordSet },
            getMerchantPreference = { getMerchantPreference },
            signout = { signout },
            setFingerprintLockStatus = { setFingerprintLockStatus },
            merchantPrefSyncStatus = { merchantPrefSyncStatus },
            isPspUpiFeatureEnabled = { isPspUpiFeatureEnabled }
        )
    }
}
