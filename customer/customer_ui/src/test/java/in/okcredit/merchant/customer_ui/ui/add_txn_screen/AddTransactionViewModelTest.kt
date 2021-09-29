package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.backend._offline.usecase.AddTransaction
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.TestData.TRANSACTION1
import `in`.okcredit.merchant.customer_ui.TestViewModel
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionContract.AddTxnViewEvent
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionContract.Intent
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.CollectVoiceSamplesFromNotes
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.GetCalculatorEducationVisibility
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.GetNoteTutorialVisibility
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.ShouldShowTransactionSecurityEducation
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase.UpdateCalculatorEducationVisibility
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.models.RoboflowState
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.HideAddBillToolTip
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.RoboflowCanShowAddBillTooltip
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.RoboflowUploadReceipt
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.SetAmountAmended
import `in`.okcredit.merchant.customer_ui.usecase.IsSupplierCreditEnabledCustomer
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import org.junit.Assert
import org.junit.Test
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.base.network.ApiError
import tech.okcredit.base.network.NetworkError
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.contract.MerchantPrefSyncStatus
import tech.okcredit.user_migration.contract.models.AmountBox
import tech.okcredit.user_migration.contract.models.PredictedData
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionViewModelTest :
    TestViewModel<AddTransactionContract.State, AddTransactionContract.PartialState, AddTxnViewEvent>() {

    private val initialState = AddTransactionContract.State()
    private val customerId: String = "1233"
    private val txnTypeByDagger: Int = Transaction.CREDIT
    private val roboflowExperimentEnabled: Boolean = false
    private val amountByDagger: Long = 0L
    private val getCustomer: GetCustomer = mock()
    private val isPasswordSet: IsPasswordSet = mock()
    private val addTransaction: AddTransaction = mock()
    private val getMerchantPreference: GetMerchantPreference = mock()
    private val showTransactionSecurityEducation: ShouldShowTransactionSecurityEducation = mock()
    private val getAddNoteTutorialVisibility: GetNoteTutorialVisibility = mock()
    private val tracker: CustomerEventTracker = mock()
    private val merchantPrefSyncStatus: MerchantPrefSyncStatus = mock()
    private val hideAddBillToolTip: HideAddBillToolTip = mock()
    private val roboflowUploadReceipt: RoboflowUploadReceipt = mock()
    private val roboflowCanShowAddBillTooltip: RoboflowCanShowAddBillTooltip = mock()
    private val roboflowSetAmountAmended: SetAmountAmended = mock()
    private val getConnectionStatus: GetConnectionStatus = mock()
    private val roboflowTracker: RoboflowEventTracker = mock()
    private val collectVoiceSamplesFromNotes: CollectVoiceSamplesFromNotes = mock()

    private val getCalculatorEducationVisibility: GetCalculatorEducationVisibility = mock()
    private val updateCalculatorEducationVisibility: UpdateCalculatorEducationVisibility = mock()
    private val isSupplierCreditEnabledCustomer: IsSupplierCreditEnabledCustomer = mock()

    override fun createViewModel() = AddTransactionViewModel(
        initialState = { initialState },
        customerId = customerId,
        txnTypeByDagger = txnTypeByDagger,
        roboflowFeatureEnabled = roboflowExperimentEnabled,
        amountByDagger = amountByDagger,
        getCustomer = { getCustomer },
        isPasswordSet = { isPasswordSet },
        addTransaction = { addTransaction },
        getMerchantPreference = { getMerchantPreference },
        showTransactionSecurityEducation = { showTransactionSecurityEducation },
        getAddNoteTutorialVisibility = { getAddNoteTutorialVisibility },
        tracker = { tracker },
        merchantPrefSyncStatus = { merchantPrefSyncStatus },
        hideAddBillToolTip = { hideAddBillToolTip },
        roboflowCanShowAddBillTooltip = { roboflowCanShowAddBillTooltip },
        roboflowUploadReceipt = { roboflowUploadReceipt },
        roboflowSetAmountAmended = { roboflowSetAmountAmended },
        getConnectionStatus = { getConnectionStatus },
        roboflowTracker = { roboflowTracker },
        getCalculatorEducationVisibility = { getCalculatorEducationVisibility },
        updateCalculatorEducationVisibility = { updateCalculatorEducationVisibility },
        collectVoiceSamplesFromNotes = { collectVoiceSamplesFromNotes },
        isSupplierCreditEnabledCustomer = { isSupplierCreditEnabledCustomer }
    )

    override fun initDependencies() {
        super.initDependencies()
        whenever(getCustomer.executeObservable(customerId)).thenReturn(Observable.just(Result.Success(TestData.CUSTOMER)))
    }

    @Test
    fun `password enable test`() {
        whenever(getMerchantPreference.execute(PreferenceKey.PAYMENT_PASSWORD)).thenReturn(
            Observable.just(
                "true"
            )
        )
        pushIntent(Intent.CheckForPasswordEnable)
        verify(getMerchantPreference).execute(PreferenceKey.PAYMENT_PASSWORD)
    }

    @Test
    fun `four digit pin test true`() {
        whenever(getMerchantPreference.execute(PreferenceKey.FOUR_DIGIT_PIN)).thenReturn(Observable.just("true"))
        pushIntent(Intent.CheckForFourDigitPin)
        verify(getMerchantPreference).execute(PreferenceKey.FOUR_DIGIT_PIN)
        assertLastValue { it.isFourDigitPin }
    }

    @Test
    fun `four digit pin test false`() {
        whenever(getMerchantPreference.execute(PreferenceKey.FOUR_DIGIT_PIN)).thenReturn(Observable.just("false"))
        pushIntent(Intent.CheckForFourDigitPin)
        verify(getMerchantPreference).execute(PreferenceKey.FOUR_DIGIT_PIN)
        assertLastValue { !it.isFourDigitPin }
    }

    @Test
    fun `note tutorial visibility test`() {
        whenever(getAddNoteTutorialVisibility.execute()).thenReturn(Single.just(true))
        pushIntent(Intent.CheckForNoteTutorial)
        verify(getAddNoteTutorialVisibility).execute()
    }

    @Test
    fun `voice sample collection enabled test true`() {
        whenever(collectVoiceSamplesFromNotes.shouldCollectVoiceSamplesFromNotes()).thenReturn(Observable.just(true))
        pushIntent(Intent.CheckForCollectVoiceSamplesFromNotes)
        verify(collectVoiceSamplesFromNotes).shouldCollectVoiceSamplesFromNotes()
        assertLastValue { it.canCollectVoiceSamplesFromNotes }
    }

    @Test
    fun `voice sample collection enabled test false`() {
        whenever(collectVoiceSamplesFromNotes.shouldCollectVoiceSamplesFromNotes()).thenReturn(Observable.just(false))
        pushIntent(Intent.CheckForCollectVoiceSamplesFromNotes)
        verify(collectVoiceSamplesFromNotes).shouldCollectVoiceSamplesFromNotes()
        assertLastValue { !it.canCollectVoiceSamplesFromNotes }
    }

    @Test
    fun `check for password set success`() {
        whenever(isPasswordSet.execute()).thenReturn(Single.just(true))
        val state = lastState()
        pushIntent(Intent.CheckForPasswordSet)
        verify(isPasswordSet).execute()
        Assert.assertTrue(state == lastState())
    }

    @Test
    fun `check for password set network failure`() {
        whenever(isPasswordSet.execute()).thenReturn(Single.error(NetworkError(cause = IOException(""))))
        pushIntent(Intent.CheckForPasswordSet)
        verify(isPasswordSet).execute()
    }

    @Test
    fun `check for password set other failure`() {
        whenever(isPasswordSet.execute()).thenReturn(Single.error(ApiError(code = 400)))
        pushIntent(Intent.CheckForPasswordSet)
        verify(isPasswordSet).execute()
        Assert.assertTrue(
            lastViewEvent() is AddTxnViewEvent.ShowError &&
                (lastViewEvent() as AddTxnViewEvent.ShowError).error == R.string.err_default_with_error_code &&
                (lastViewEvent() as AddTxnViewEvent.ShowError).errorCode == 400
        )
    }

    @Test
    fun `SyncMerchantPref intent should set the merchantPrefSynced state to true`() {
        whenever(merchantPrefSyncStatus.execute()).thenReturn(Completable.complete())
        whenever(getMerchantPreference.execute(PreferenceKey.FOUR_DIGIT_PIN)).thenReturn(Observable.just("true"))

        pushIntent(Intent.SyncMerchantPref)

        assertLastValue { it.merchantPrefSync }
    }

    @Test
    fun `merchantPrefSyncStatus should set the merchantPrefSync as true`() {
        whenever(merchantPrefSyncStatus.checkMerchantPrefSync()).thenReturn(Single.just(true))

        pushIntent(Intent.CheckMerchantPrefSync)

        assertLastValue { it.merchantPrefSync }
    }

    @Test
    fun `merchantPrefSyncStatus should set the merchantPrefSync as false`() {
        whenever(merchantPrefSyncStatus.checkMerchantPrefSync()).thenReturn(Single.just(false))

        pushIntent(Intent.CheckMerchantPrefSync)

        assertLastValue { !it.merchantPrefSync }
    }

    @Test
    fun `CustomerProfileClicked should emit GoToCustomerProfile viewEvent`() {
        val currentState = lastState()

        pushIntent(Intent.CustomerProfileClicked)

        Assert.assertTrue(
            lastViewEvent() is AddTxnViewEvent.GoToCustomerProfile
        )

        // asserting state should not be changed
        Assert.assertTrue(
            lastState() == currentState
        )
    }

    @Test
    fun `select bill date clicked with system calendar`() {

        val currentState = lastState()

        pushIntent(Intent.SelectBillDateClicked)

        Assert.assertTrue(
            (lastViewEvent() is AddTxnViewEvent.ShowCalendar)
        )

        // asserting state should not be changed
        Assert.assertTrue(lastState() == currentState)
    }

    @Test
    fun `add bill image clicked`() {
        val currentState = lastState()

        pushIntent(Intent.AddBillImageClicked)

        Assert.assertTrue(
            (lastViewEvent() is AddTxnViewEvent.SelectBillImage)
        )
        // asserting state should not be changed
        Assert.assertTrue(lastState() == currentState)
    }

    @Test
    fun `UpdatePin intent should emit updatePinDialog viewEvent`() {
        val currentState = lastState()

        pushIntent(Intent.UpdatePin)

        Assert.assertTrue(
            (lastViewEvent() is AddTxnViewEvent.ShowUpdatePinDialog)
        )
        // asserting state should not be changed
        Assert.assertTrue(lastState() == currentState)
    }

    @Test
    fun `CheckForFourPinPassword intent should emit GoToEnterPassword viewEvent when merchant sync pref and four is true`() {
        val currentState = lastState()

        pushIntent(Intent.CheckFourPinPasswordSet(merchantPrefSync = true, isFourDigitPin = true))

        Assert.assertTrue(
            (lastViewEvent() is AddTxnViewEvent.GoToEnterPassword)
        )
        // asserting state should not be changed
        Assert.assertTrue(lastState() == currentState)
    }

    @Test
    fun `calculator data added`() {
        val fakeAmount = 1245L
        pushIntent(
            Intent.CalculatorData(
                amountCalculation = fakeAmount.toString(),
                amount = fakeAmount
            )
        )

        assertLastValue {
            it.amount == fakeAmount
        }
        assertLastValue {
            it.amountCalculation == fakeAmount.toString()
        }
    }

    @Test
    fun `CustomerProfileClicked intent should emit GoToCustomerProfile viewEvent`() {
        val currentState = lastState()

        pushIntent(Intent.CustomerProfileClicked)

        Assert.assertTrue(
            (lastViewEvent() is AddTxnViewEvent.GoToCustomerProfile)
        )
        // asserting state should not be changed
        Assert.assertTrue(lastState() == currentState)
    }

    @Test
    fun `BillDateSelected intent should update the state`() {
        val fakeCalender = Calendar.getInstance()
        val currentState = lastState()

        pushIntent(Intent.BillDateSelected(fakeCalender))

        assertLastValue {
            it.billDate == SimpleDateFormat.getDateInstance(DateFormat.MEDIUM).format(fakeCalender.time)
        }
    }

    @Test
    fun `EditAmount intent should set the roboflowState as EditAmount`() {
        pushIntent(Intent.EditAmount)

        // asserting state should not be changed
        assertLastValue {
            it.roboflowState == RoboflowState.EditAmount
        }
    }

    @Test
    fun `EnterAmountManuallyCancelUploadReceipt intent should set the roboflowState as EnterAmountManuallyCancelUploadReceipt`() {
        pushIntent(Intent.EnterAmountManuallyCancelUploadReceipt)

        // asserting state should not be changed
        assertLastValue {
            it.roboflowState == RoboflowState.EnterAmountManuallyCancelUploadReceipt
        }
    }

    @Test
    fun `RoboflowSetAmountAmended intent should call roboflowSetAmountAmended use case`() {
        val currentState = lastState()
        val fakeCustomerId = "1234"
        val fakeTransaction = TRANSACTION1
        val fakeNewAmount = 123L

        whenever(roboflowSetAmountAmended.execute(fakeCustomerId, fakeTransaction.id, fakeNewAmount)).thenReturn(
            Completable.complete()
        )

        pushIntent(Intent.RoboflowSetAmountAmended(fakeCustomerId, fakeNewAmount, fakeTransaction))

        verify(roboflowSetAmountAmended).execute(fakeCustomerId, fakeTransaction.id, fakeNewAmount)

        // asserting state should not be changed
        Assert.assertTrue(
            lastState() == currentState
        )
    }

    @Test
    fun `RoboflowUploadReceipt intent should call getConnectionStatus use case if internet is available`() {
        val currentState = lastState()
        val fakeCaptureImage = CapturedImage(
            file = File("fakeFile")
        )

        whenever(getConnectionStatus.executeUnwrapped()).thenReturn(Observable.just(true))
        whenever(roboflowUploadReceipt.execute(fakeCaptureImage)).thenReturn(Single.just(fakePredictedData))

        pushIntent(Intent.RoboflowUploadReceipt(fakeCaptureImage))

        verify(roboflowUploadReceipt).execute(fakeCaptureImage)
        verify(getConnectionStatus).executeUnwrapped()

        // asserting state should not be changed
        Truth.assertThat(lastState()).isEqualTo(
            currentState.copy(
                amount = fakeAmountBox.amount,
                canPredictedAmount = false,
                predictedAmount = fakeAmountBox.amount,
                amountCalculation = (fakeAmountBox.amount / 100).toString(),
                roboflowState = RoboflowState.RoboflowFetchSuccess(
                    fakePredictedData.width,
                    fakePredictedData.height,
                    fakePredictedData.amountBox
                )
            )
        )
    }

    @Test
    fun `RoboflowUploadReceipt intent shouldn't call roboflowUploadReceipt use case if internet is not available`() {
        val fakeCaptureImage = CapturedImage(
            file = File("fakeFile")
        )

        whenever(getConnectionStatus.executeUnwrapped()).thenReturn(Observable.just(false))
        whenever(roboflowUploadReceipt.execute(fakeCaptureImage)).thenReturn(Single.just(fakePredictedData))

        pushIntent(Intent.RoboflowUploadReceipt(fakeCaptureImage))

        verify(roboflowUploadReceipt, times(0)).execute(fakeCaptureImage)
        verify(getConnectionStatus).executeUnwrapped()

        // asserting state should not be changed
        assertLastValue { it.txType == 1 }
        assertLastValue { it.roboflowState == RoboflowState.InternetNotAvailable }
    }

    @Test
    fun `calculator variant should send view event from getCalculatorEducationVisibility`() {
        whenever(getCalculatorEducationVisibility.execute()).thenReturn(Observable.just(true))
        pushIntent(Intent.CheckCalculatorEducation)
        verify(getCalculatorEducationVisibility).execute()

        assertLastViewEvent<AddTxnViewEvent.ShowCalculatorEducation>()
    }

    @Test
    fun `update calculator education prefs when it is dismissed`() {
        whenever(updateCalculatorEducationVisibility.execute()).thenReturn(Completable.complete())
        pushIntent(Intent.CalculatorEducationDismissed)

        verify(updateCalculatorEducationVisibility).execute()
    }

    @Test
    fun `start speech recognition should fire correct view event`() {
        whenever(collectVoiceSamplesFromNotes.getSpeechRecognitionIntent()).thenReturn(android.content.Intent())
        pushIntent(Intent.StartSpeechRecognition)

        verify(collectVoiceSamplesFromNotes).getSpeechRecognitionIntent()

        assertLastViewEvent<AddTxnViewEvent.StartSpeechRecognition>()
    }

    companion object {
        private val fakeAmountBox = AmountBox(
            amount = 25000L,
            boxCoordinateX1 = 0,
            boxCoordinateX2 = 0,
            boxCoordinateY1 = 0,
            boxCoordinateY2 = 0
        )

        private val fakePredictedData = PredictedData(
            fakeAmountBox,
            width = 100,
            height = 200,
            fileName = "",
            fileObjectId = ""
        )
    }
}
