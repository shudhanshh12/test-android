package `in`.okcredit.frontend.ui.add_supplier_transaction

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.backend._offline.usecase.AddSupplierTransaction
import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.backend.analytics.AnalyticsSuperProps
import `in`.okcredit.backend.utils.MathUtils
import `in`.okcredit.backend.utils.StringUtils
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.add_supplier_transaction.AddSupplierTransactionContract.*
import `in`.okcredit.frontend.usecase.IsPasswordSet
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.suppliercredit.use_case.ExperimentCanShowMidCamera
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.contract.MerchantPrefSyncStatus
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class AddSupplierTxnScreenViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(MainActivity.ARG_SUPPLIER_ID) val supplierId: String?,
    @ViewModelParam("transaction_type") val txnType: Int?,
    @ViewModelParam("transaction_amount") val txnAmount: Long,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val addSupplierTransaction: Lazy<AddSupplierTransaction>,
    private val getSupplier: Lazy<GetSupplier>,
    private val getMerchantPreference: Lazy<GetMerchantPreferenceImpl>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val navigator: Lazy<Navigator>,
    private val experimentCanShowMidCamera: Lazy<ExperimentCanShowMidCamera>,
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>,
) : BasePresenter<State, PartialState>(initialState) {

    private var mobile: String? = null
    private var amount: Long = 0
    private var amountCalculation: String = ""
    private var calculatorOperatorsUsed = ""
    private val showInValidAmountPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            // handle `load` screen intent
            intent<Intent.Load>()
                .switchMap { getSupplier.get().execute(if (supplierId.isNullOrEmpty()) "" else supplierId) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> {
                            mobile = it.value.mobile
                            PartialState.ShowData(it.value, txnType!!)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<Intent.Load>()
                .switchMap { isPasswordSet.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> {
                            PartialState.SetIsPasswordStatus(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },
            intent<Intent.Load>()
                .map {
                    if (txnAmount != 0L) {
                        amount = abs(txnAmount)

                        if (amount % 100 != 0L) {
                            val decimalValue = if ((amount % 100).toString().length == 1) "0" else ""
                            amountCalculation =
                                (amount / 100).toString() + "." + decimalValue + (amount % 100).toString()
                        } else {
                            amountCalculation = (amount / 100).toString()
                        }

                        PartialState.SetAmountDetails(amount, amountCalculation)
                    } else {
                        PartialState.SetOriginInfo(AddSupplierTransactionContract.ORIGIN_CUSTOMER_SCREEN)
                    }
                },

            // LoadCustomers password info
            intent<Intent.Load>()
                .switchMap { getMerchantPreference.get().execute(PreferenceKey.PAYMENT_PASSWORD) }
                .map {
                    PartialState.SetPasswordEnableStatus(java.lang.Boolean.parseBoolean(it))
                },
            intent<Intent.Load>()
                .switchMap { experimentCanShowMidCamera.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.CanShowMidCamera(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            // handle `show alert` intent
            intent<Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it.message))
                },

            intent<Intent.AddTransaction>()
                .map {
                    navigator.get().goToAuthScreen()
                    PartialState.ChangeNote(it.note)
                },

            // navigate to supplier profile screen
            intent<Intent.GoToCustomerProfile>()
                .map {
                    navigator.get().gotoCustomerProfile(supplierId)
                    PartialState.NoChange
                },

            // set amount and amount calculation path when pressing digits
            intent<Intent.OnDigitClicked>()
                .map {
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (!isLastNumberExceededDecimal(amountCalculation)) {
                        amountCalculation = "$amountCalculation${it.digit}"
                        amount = MathUtils.eval(amountCalculation)
                    }

                    if (amount == -0L && amountCalculation != ".0") {
                        amount = 0L
                    }

                    if (amount.compareTo(0) == 0 && amountCalculation != ".0") {
                        amountCalculation = ""
                    }

                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
//                        amount = Math.round(amount * 100) / 100f
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },

            // set amount and amount calculation path when pressing operators
            intent<Intent.OnOperatorClicked>()
                .map {
                    addCalculatorOperatorsUsed(it.amountCalculation)
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (it.amountCalculation == "-") {
                        if (amountCalculation.isEmpty()) {
                            amountCalculation = "$amountCalculation${it.amountCalculation}"
                        } else if (amountCalculation.substring(amountCalculation.length - 1).isDigitsOnly()) {
                            amountCalculation = "$amountCalculation${it.amountCalculation}"
                        } else if (amountCalculation.substring(amountCalculation.length - 1) == "+" ||
                            amountCalculation.substring(amountCalculation.length - 1) == "."
                        ) {
                            amountCalculation =
                                amountCalculation.substring(0, amountCalculation.length - 1) + it.amountCalculation
                        } else if (amountCalculation.substring(amountCalculation.length - 1) != "-") {
                            amountCalculation = "$amountCalculation${it.amountCalculation}"
                        }
                    } else {
                        if (amountCalculation.isEmpty()) {
                        } else if (amountCalculation.substring(amountCalculation.length - 1).isDigitsOnly()) {
                            amountCalculation = "$amountCalculation${it.amountCalculation}"
                        } else if (amountCalculation.substring(amountCalculation.length - 1) == "." ||
                            amountCalculation.substring(amountCalculation.length - 1) == "+" ||
                            amountCalculation.substring(amountCalculation.length - 1) == "*"
                        ) {
                            amountCalculation =
                                amountCalculation.substring(0, amountCalculation.length - 1) + it.amountCalculation
                        } else if (amountCalculation.substring(amountCalculation.length - 1) == "-") {
                            if (amountCalculation.length > 2 &&
                                amountCalculation.substring(
                                        amountCalculation.length - 2,
                                        amountCalculation.length - 1
                                    ) == "*" ||
                                amountCalculation.substring(
                                        amountCalculation.length - 2,
                                        amountCalculation.length - 1
                                    ) == "+"
                            ) {
                                amountCalculation =
                                    amountCalculation.substring(0, amountCalculation.length - 2) + it.amountCalculation
                            } else {
                                amountCalculation =
                                    amountCalculation.substring(0, amountCalculation.length - 1) + it.amountCalculation
                            }
                        }
                    }

                    if (amount.compareTo(0) == 0) {
                        amountCalculation = ""
                    }

                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
//                        amount = Math.round(amount * 100) / 100f
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },

            // set amount and amount calculation path when pressing dot
            intent<Intent.OnDotClicked>()
                .map {
                    addCalculatorOperatorsUsed(".")
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (amountCalculation.isEmpty()) {
                        amountCalculation = "$amountCalculation${"."}"
                    } else {
                        val lastNumber = findLastNumber(amountCalculation)
                        if (!lastNumber.contains('.')) {
                            amountCalculation = "$amountCalculation${"."}"
                        }
                    }

                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },

            // set amount calculation when pressing equal
            intent<Intent.OnEqualClicked>()
                .map {
                    addCalculatorOperatorsUsed("=")
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    amountCalculation = if (amount % 100 != 0L) {
                        (amount / 100).toString() + "." + (amount % 100).toString()
                    } else {
                        (amount / 100).toString()
                    }

                    if (amount.compareTo(0) == 0) {
                        amountCalculation = ""
                    }

                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
//                        amount = Math.round(amount * 100) / 100f
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },

            // set amount calculation when pressing equal
            intent<Intent.OnBackSpaceClicked>()
                .map {
                    addCalculatorOperatorsUsed("D")
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (amountCalculation.isNotEmpty()) {
                        amountCalculation = amountCalculation.substring(0, amountCalculation.length - 1)
                        val lastChar: String =
                            if (amountCalculation.isNotEmpty()) amountCalculation.substring(amountCalculation.length - 1) else ""

                        if (amountCalculation.isEmpty()) {
                            amount = 0L
                        } else if (lastChar.isNotEmpty()) {
                            if (Character.isDigit(lastChar.toCharArray()[0])) {
                                amount = MathUtils.eval(amountCalculation)
                            } else {
                                val lLastIndexOfDigit = StringUtils.findLastIndexOfDigit(amountCalculation)
                                if (lLastIndexOfDigit != -1) {
                                    amount = MathUtils.eval(amountCalculation.substring(0, lLastIndexOfDigit))
                                }
                            }
                        }
                    }

                    if (amount.compareTo(0) == 0) {
                        amountCalculation = ""
                    }
                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
//                        amount = Math.round(amount * 100) / 100f
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },

            showInValidAmountPublishSubject
                .switchMap {
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                        .map<PartialState> { PartialState.HideAmountError }
                        .startWith(PartialState.ShowAmountError)
                },

            // set amount and amount calculation path when pressing digits
            intent<Intent.OnLongPressBackSpace>()
                .map {
                    addCalculatorOperatorsUsed("D")
                    amount = 0L
                    amountCalculation = ""
                    PartialState.SetAmountDetails(amount, amountCalculation)
                },

            // set amount calculation when pressing equal
            intent<Intent.OnChangeInputMode>()
                .map {
                    PartialState.ChangeInputMode(it.d)
                },

            // change bill_date
            intent<Intent.OnChangeDate>()
                .map {
                    PartialState.ChangeDate(it.date)
                },

            // change imageLocal
            intent<Intent.OnChangeImage>()
                .map {
                    PartialState.ChangeImage(it.image.absolutePath)
                },

            // change bill_date
            intent<Intent.OnDeleteImage>()
                .map {
                    PartialState.ChangeImage(null)
                },

            // change password
            intent<Intent.OnChangePassword>()
                .map {
                    PartialState.SetPassword(it.password)
                },

            // change bill_date
            intent<Intent.OnForgotPasswordClicked>()
                .switchMap { getActiveBusiness.get().execute() }
                .map {
                    navigator.get().gotoForgotPasswordScreen(it.mobile)
                    PartialState.NoChange
                },

            // Submit Password
            intent<Intent.SubmitPassword>()
                .switchMap {
                    if (!it.note.isNullOrEmpty()) {
                        Analytics.track(
                            AnalyticsEvents.ADD_NOTE,
                            EventProperties.create().with("customer_id", supplierId)
                                .with(PropertyKey.ACCOUNT_ID, supplierId)
                                .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
                        )

                        Analytics.setUserProperty(AnalyticsSuperProps.NOTE, null)
                    }
                    if (!getCalculatorOperatorsUsed().isBlank()) {
                        Analytics.track(
                            AnalyticsEvents.INPUT_CALCULATOR,
                            EventProperties
                                .create()
                                .with(PropertyKey.TYPE, getCalculatorOperatorsUsed())
                                .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
                        )
                    }
                    UseCase.wrapSingle(
                        addSupplierTransaction.get().execute(
                            AddSupplierTransaction.AddTransactionRequest(
                                supplierId = supplierId!!,
                                amount = it.amount,
                                payment = it.payment,
                                note = it.note,
                                receiptUrl = it.image,
                                password = it.password,
                                billDate = it.billDate,
                                isPasswordVerifyRequired = it.isPasswordVerifyRequired,
                                transactionState = -1
                            )
                        )
                    ).doOnNext { Timber.i(">> addTransaction 4") }
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowSubmitLoader
                        is Result.Success -> {
                            navigator.get().setNewTransactionIdAsNavResult(it.value.createTime.millis)
                            PartialState.HideSubmitLoader(true)
                        }
                        is Result.Failure -> {
                            navigator.get().showFailedMsg()
                            when {
                                isInternetIssue(it.error) -> PartialState.HideSubmitLoader(false)
                                else -> throw it.error
                            }
                        }
                    }
                },
            checkIsMerchantPrefSync(),
            checkIsFourDigitPinSet(),
            syncMerchantData(),
            syncIsFourDigitPinSet()
        )
    }

    private fun syncIsFourDigitPinSet() = intent<Intent.CheckIsFourDigitPinSet>()
        .switchMap {
            UseCase.wrapSingle(getMerchantPreference.get().execute(PreferenceKey.FOUR_DIGIT_PIN).firstOrError())
        }.map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    navigator.get().handleFourDigitPin(it.value.toBoolean())
                    PartialState.SetIsFourDigitPin(it.value.toBoolean())
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            navigator.get().showNetworkError()
                            PartialState.NoChange
                        }
                        else -> throw it.error
                    }
                }
            }
        }

    private fun checkIsFourDigitPinSet() = intent<Intent.Load>()
        .switchMap {
            UseCase.wrapSingle(
                getMerchantPreference.get().execute(PreferenceKey.FOUR_DIGIT_PIN).firstOrError()
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> PartialState.SetIsFourDigitPin(it.value.toBoolean())
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun checkIsMerchantPrefSync() = intent<Intent.Load>()
        .switchMap { UseCase.wrapSingle(merchantPrefSyncStatus.get().checkMerchantPrefSync()) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> PartialState.SetIsMerchantPrefSync(it.value)
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun syncMerchantData() = intent<Intent.SyncMerchantPref>()
        .switchMap { UseCase.wrapCompletable(merchantPrefSyncStatus.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    navigator.get().onMerchantPrefSynced()
                    PartialState.SetIsMerchantPrefSync(true)
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> {
                            PartialState.NoChange
                        }
                        isInternetIssue(it.error) -> {
                            navigator.get().showNetworkError()
                            PartialState.NoChange
                        }
                        else -> throw it.error
                    }
                }
            }
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.ShowData -> currentState.copy(
                isLoading = false,
                supplier = partialState.supplier,
                txType = partialState.txType
            )
            is PartialState.SetAmountDetails -> currentState.copy(
                amountCalculation = partialState.amountCalculation,
                amount = partialState.amount,
                amountError = false
            )
            is PartialState.ChangeInputMode -> currentState.copy(
                activeInputMode = partialState.value,
                isIncorrectPassword = false
            )
            is PartialState.ChangeDate -> currentState.copy(date = partialState.value)
            is PartialState.ChangeImage -> currentState.copy(imageLocal = partialState.localUrl)
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.SetPassword -> currentState.copy(
                password = partialState.password,
                isIncorrectPassword = false
            )
            is PartialState.ShowAmountError -> currentState.copy(amountError = true)
            is PartialState.HideAmountError -> currentState.copy(amountError = false)
            is PartialState.ShowAlert -> currentState.copy(isAlertVisible = true, alertMessage = partialState.message)
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.SetPasswordEnableStatus -> currentState.copy(isPassWordEnable = partialState.status)
            is PartialState.SetIsPasswordStatus -> currentState.copy(isPasswordSet = partialState.status)
            is PartialState.SetPasswordErrorStatus -> currentState.copy(isIncorrectPassword = partialState.status)
            is PartialState.ChangeNote -> currentState.copy(note = partialState.note)
            is PartialState.SetOriginInfo -> currentState.copy(originScreen = partialState.value)
            is PartialState.NoChange -> currentState
            is PartialState.CanShowMidCamera -> currentState.copy(canShowMidCamera = partialState.canShowMidCamera)
            PartialState.ShowSubmitLoader -> currentState.copy(isSubmitLoading = true, isIncorrectPassword = false)
            is PartialState.HideSubmitLoader -> currentState.copy(
                isSubmitLoading = false,
                isSubmitSuccess = partialState.result
            )
            is PartialState.SetIsMerchantPrefSync -> currentState.copy(
                isMerchantPrefSynced = partialState.isMerchantPrefSynced
            )
            is PartialState.SetIsFourDigitPin -> currentState.copy(
                isFourDigitPin = partialState.isFourDigitPin
            )
        }
    }

    private fun isLastNumberExceededDecimal(amountCalculation: String): Boolean {
        val lastNumber = findLastNumber(amountCalculation)
        val lastIndexOfDot = lastNumber.lastIndexOf('.')
        return if (lastIndexOfDot >= 0) {
            lastNumber.substring(lastIndexOfDot + 1, lastNumber.length).length >= 2
        } else {
            false
        }
    }

    private fun findLastNumber(amountCalculation: String): String {
        val lastIndex = amountCalculation.lastIndexOfAny(charArrayOf('+', '-', '*', '/'))
        return if (lastIndex >= 0) {
            amountCalculation.substring(lastIndex + 1, amountCalculation.length)
        } else {
            amountCalculation
        }
    }

    private fun addCalculatorOperatorsUsed(value: String) {
        calculatorOperatorsUsed += value
    }

    private fun getCalculatorOperatorsUsed(): String {
        var type = ""
        if (calculatorOperatorsUsed.contains("*")) {
            type += "M"
        }
        if (calculatorOperatorsUsed.contains("+")) {
            type += "A"
        }
        if (calculatorOperatorsUsed.contains("-")) {
            type += "S"
        }
        if (calculatorOperatorsUsed.contains("D")) {
            type += "D"
        }
        if (calculatorOperatorsUsed.contains("=")) {
            type += "E"
        }
        if (calculatorOperatorsUsed.contains(".")) {
            type += "P"
        }
        return type
    }
}

private fun CharSequence.isDigitsOnly(): Boolean {
    return toString().matches("[0-9]+".toRegex())
}
