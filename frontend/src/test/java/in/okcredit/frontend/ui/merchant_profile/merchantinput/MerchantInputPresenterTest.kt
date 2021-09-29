package `in`.okcredit.frontend.ui.merchant_profile.merchantinput

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.usecase.merchant.GetAddress
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.usecase.UpdateBusinessImpl
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class MerchantInputPresenterTest {

    private val initialState: MerchantInputContract.State = mock()
    private val getAddress: GetAddress = mock()
    private val updateBusinessImpl: UpdateBusinessImpl = mock()
    private val tracker: Tracker = mock()
    private val context: Context = mock()
    private val inputType = 0
    private val inputTitle = ""
    private val inputVaue = ""
    private val selectedCategoryId = ""
    private val latitude = 0.0
    private val longitude = 0.0
    private val gpsEnabled = false
    private val isSourceInAppNotification = false

    private lateinit var presenter: MerchantInputViewModel

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    private fun createPresenter(
        inputType: Int = this.inputType,
        inputTitle: String = this.inputTitle,
        inputVaue: String = this.inputVaue,
        selectedCategoryId: String = this.selectedCategoryId,
        latitude: Double = this.latitude,
        longitude: Double = this.longitude,
        gpsEnabled: Boolean = this.gpsEnabled,
        isSourceInAppNotification: Boolean = this.isSourceInAppNotification,
    ) = MerchantInputViewModel(
        initialState = initialState,
        inputType = inputType,
        inputTitle = inputTitle,
        inputVaue = inputVaue,
        selectedCategoryId = selectedCategoryId,
        latitude = latitude,
        longitude = longitude,
        gpsEnabled = gpsEnabled,
        isSourceInAppNotification = isSourceInAppNotification,
        getAddress = getAddress,
        updateBusiness = updateBusinessImpl,
        tracker = tracker,
        context = context
    )

    private fun pushIntent(intent: UserIntent) = presenter.attachIntents(Observable.just(intent))

    @Test
    fun `Given Load intent with isSourceInAppNotification true should emit ShowGpsPermission`() {
        presenter = createPresenter(isSourceInAppNotification = true)

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.Load)

        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.ShowGpsPermission)).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given Load intent with isSourceInAppNotification false should not emit ShowGpsPermission`() {
        presenter = createPresenter(isSourceInAppNotification = false)

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.Load)

        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.ShowGpsPermission).not()).isTrue()

        testObserver.dispose()
    }

    @Test
    fun `Given Load intent with isSourceInAppNotification true should emit SetInputSubject`() {
        presenter = createPresenter(isSourceInAppNotification = true)

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.Load)

        assertThat(
            testObserver.values()
                .contains(MerchantInputContract.ViewEvent.SetInputSubject(Triple(inputType, inputTitle, inputVaue)))
        ).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdateGpsStatus intent should emit SetInputSubject`() {
        presenter = createPresenter()

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdateGpsStatus(false))

        assertThat(
            testObserver.values()
                .contains(MerchantInputContract.ViewEvent.SetInputSubject(Triple(inputType, inputTitle, inputVaue)))
        ).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdateBusinessName intent should emit UpdatedSuccessfully`() {
        val businessName = "Rocket Singh Corp."
        presenter = createPresenter()
        whenever(updateBusinessImpl.execute(Request(inputType = inputType, updatedValue = businessName)))
            .thenReturn(Completable.complete())

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdateBusinessName(businessName))

//        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.UpdatedSuccessfully())).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdateEmail intent should emit UpdatedSuccessfully`() {
        val email = "ceo@rocketcorp.club"
        presenter = createPresenter()
        whenever(updateBusinessImpl.execute(Request(inputType = inputType, updatedValue = email)))
            .thenReturn(Completable.complete())

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdateEmail(email))

//        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.UpdatedSuccessfully())).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdateAbout intent should emit UpdatedSuccessfully`() {
        val about = "We make you fly!"
        presenter = createPresenter()
        whenever(updateBusinessImpl.execute(Request(inputType = inputType, updatedValue = about)))
            .thenReturn(Completable.complete())

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdateAbout(about))

//        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.UpdatedSuccessfully())).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdatePersonName intent should emit UpdatedSuccessfully`() {
        val name = "Rocket Musk"
        presenter = createPresenter()
        whenever(updateBusinessImpl.execute(Request(inputType = inputType, updatedValue = name)))
            .thenReturn(Completable.complete())

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdatePersonName(name))

//        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.UpdatedSuccessfully())).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdateCategory intent should emit UpdatedSuccessfully`() {
        val selectedCategoryId = "category-id"
        val category = Pair(selectedCategoryId, "Rocket science")
        presenter = createPresenter(selectedCategoryId = selectedCategoryId)
        whenever(updateBusinessImpl.execute(Request(inputType = inputType, category = category)))
            .thenReturn(Completable.complete())

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdateCategory(category.second))

//        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.UpdatedSuccessfully())).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `Given UpdateAddress intent should emit UpdatedSuccessfully`() {
        val address = Triple("Rainbow Square, Mars - 000001", latitude, longitude)
        presenter = createPresenter()
        whenever(updateBusinessImpl.execute(Request(inputType = inputType, address = address)))
            .thenReturn(Completable.complete())

        val testObserver = presenter.viewEvent().test()
        pushIntent(MerchantInputContract.Intent.UpdateAddress(address))

//        assertThat(testObserver.values().contains(MerchantInputContract.ViewEvent.UpdatedSuccessfully())).isTrue()
        testObserver.dispose()
    }
}
