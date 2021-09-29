package `in`.okcredit.merchant.customer_ui.ui.add_discount

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.backend.analytics.AnalyticsSuperProps
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.RxSharedPrefValues.SHOULD_SHOW_NOTE_TUTORIAL
import `in`.okcredit.backend.utils.MathUtils
import `in`.okcredit.backend.utils.StringUtils
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.add_discount.AddDiscountContract.*
import `in`.okcredit.merchant.customer_ui.usecase.AddDiscount
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.IncorrectPassword
import tech.okcredit.android.auth.InvalidPassword
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddDiscountViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("customer_id") val customerId: String?,
    @ViewModelParam("transaction_type") val txnType: Int?,
    @ViewModelParam("transaction_amount") val txnAmount: Long,
    @ViewModelParam("voice_amount") val voiceAmount: Long,
    @ViewModelParam("voice_id") val voiceId: String,
    @ViewModelParam("input_type") val inputType: String,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val getCustomer: Lazy<GetCustomer>,
    private val addDiscount: Lazy<AddDiscount>,
    private val getMerchantPreference: Lazy<GetMerchantPreferenceImpl>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val ab: Lazy<AbRepository>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private var isPasswordVerifyRequired: Boolean = false
    private var note: String? = null
    private var staleAmount: Long = 0
    private var password: String? = null
    private var mMobile: String? = null
    private var amount: Long = 0
    private var amountCalculation: String = ""
    private var calculatorOperatorsUsed = ""
    private var voiceDataInjected = false
    private val showInValidAmountPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            // handle `load` screen intent
            intent<Intent.Load>()
                .switchMap {
                    UseCase.wrapObservable(
                        getCustomer.get().execute(if (customerId.isNullOrEmpty()) "" else customerId)
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> {
                            mMobile = it.value.mobile
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
                .switchMap {
                    wrap(isPasswordSet.get().execute())
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> PartialState.SetIsPasswordStatus(it.value)
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
                    if (inputType == "voice" && !voiceDataInjected) {
                        voiceDataInjected = true
                        val amountCalculationTemp: String = amountCalculation
                        val amountTemp: Long = amount

                        if (!isLastNumberExceededDecimal(amountCalculation)) {
                            amountCalculation = "$amountCalculation$voiceAmount"
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
                            PartialState.SetVoiceInputData(
                                amount,
                                amountCalculation,
                                voiceId,
                                inputType,
                                AddDiscountContract.INPUT_MODE_AMOUNT
                            )
                        }
                    } else PartialState.NoChange
                },

            intent<Intent.Load>()
                .map {
                    if (txnAmount != 0L) {
                        amount = Math.abs(txnAmount)

                        if (amount % 100 != 0L) {
                            val decimalValue = if ((amount % 100).toString().length == 1) "0" else ""
                            amountCalculation =
                                (amount / 100).toString() + "." + decimalValue + (amount % 100).toString()
                        } else {
                            amountCalculation = (amount / 100).toString()
                        }

                        PartialState.SetAmountDetails(amount, amountCalculation)
                    } else {
                        PartialState.SetOriginInfo(AddDiscountContract.ORIGIN_CUSTOMER_SCREEN)
                    }
                },

            // load note tutorial visibility
            intent<Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(
                        rxSharedPreference.get().getBoolean(SHOULD_SHOW_NOTE_TUTORIAL, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .flatMap {
                    when (it) {
                        is Result.Progress -> Observable.just(PartialState.NoChange)
                        is Result.Success -> {
                            rxCompletable {
                                if (it.value) {
                                    rxSharedPreference.get().remove(SHOULD_SHOW_NOTE_TUTORIAL, Scope.Individual)
                                }
                            }.andThen(Observable.just(PartialState.SetNoteTutorialVisibility(it.value)))
                        }
                        is Result.Failure -> {
                            Observable.just(PartialState.NoChange)
                        }
                    }
                },

            // Load password info
            intent<Intent.Load>()
                .switchMap {
                    getMerchantPreference.get()
                        .execute(PreferenceKey.PAYMENT_PASSWORD)
                }
                .map {
                    PartialState.SetPasswordEnableStatus(java.lang.Boolean.parseBoolean(it))
                },

            // handle `show alert` intent
            intent<Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it.message))
                },

            // navigate to supplier profile screen
            intent<Intent.GoToCustomerProfile>()
                .map {
                    emitViewEvent(ViewEvent.GoToCustomerProfile(customerId))
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
                        } else if (amountCalculation.substring(amountCalculation.length - 1).isDidgitsOnly()) {
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
                        } else if (amountCalculation.substring(amountCalculation.length - 1).isDidgitsOnly()) {
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
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },

            // set amount calculation when pressing equal
            intent<Intent.OnBackSpaceClicked>()
                .map {
                    addCalculatorOperatorsUsed("D")
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (!amountCalculation.isEmpty()) {
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
                    PartialState.ChangeImage(it.listPhotos)
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
                    emitViewEvent(ViewEvent.GoToForgotPasswordScreen(it.mobile))
                    PartialState.NoChange
                },

            intent<Intent.AddTransaction>()
                .switchMap {
                    Observable.just(
                        PartialState.ChangeNote(it.note),
                        PartialState.ChangeInputMode(AddDiscountContract.INPUT_MODE_PASSWORD)
                    )
                },

            // Submit Password
            intent<Intent.SubmitPassword>()
                .switchMap {
                    if (!it.note.isNullOrEmpty()) {
                        Analytics.setUserProperty(AnalyticsSuperProps.NOTE, null)
                    }
                    if (!getCalculatorOperatorsUsed().isBlank()) {
                        Analytics.track(
                            AnalyticsEvents.INPUT_CALCULATOR,
                            EventProperties
                                .create()
                                .with(PropertyKey.TYPE, getCalculatorOperatorsUsed())
                                .with(PropertyKey.RELATION, PropertyValue.CUSTOMER)
                        )
                    }
                    staleAmount = it.amount
                    note = it.note
                    password = it.password
                    isPasswordVerifyRequired = it.isPasswordVerifyRequired
                    addDiscount.get().execute(

                        AddDiscount.Request(
                            customerId!!,
                            it.amount,
                            it.note,
                            it.password,
                            it.isPasswordVerifyRequired
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.ShowSubmitLoader
                        is Result.Success -> PartialState.HideSubmitLoader(true)
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    PartialState.HideSubmitLoader(
                                        false,
                                        R.string.no_internet_connection
                                    )
                                }
                                (
                                    it.error is IncorrectPassword || it.error.cause is IncorrectPassword ||
                                        it.error is InvalidPassword || it.error.cause is InvalidPassword
                                    ) -> {
                                    emitViewEvent(ViewEvent.ShowPasswordError)
                                    emitViewEvent(ViewEvent.DismissBottomLoader)
                                    PartialState.NoChange
                                }
                                else -> PartialState.HideSubmitLoader(false)
                            }
                        }
                    }
                },

            intent<Intent.SetSpeechAnimationVisibility>()
                .map {
                    PartialState.OnUserSpeaking(it.status)
                }

        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.SetNoteTutorialVisibility -> currentState.copy(showNoteTutorial = partialState.isVisible)
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.ShowData -> currentState.copy(
                isLoading = false,
                customer = partialState.customer,
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
            is PartialState.ChangeImage -> currentState.copy(imageList = partialState.listPhotos)
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.SetPassword -> currentState.copy(
                password = partialState.password,
                isIncorrectPassword = false
            )
            is PartialState.ShowAmountError -> currentState.copy(amountError = true)
            is PartialState.HideAmountError -> currentState.copy(amountError = false)
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.SetPasswordEnableStatus -> currentState.copy(isPassWordEnable = partialState.status)
            is PartialState.SetIsPasswordStatus -> currentState.copy(isPasswordSet = partialState.status)
            is PartialState.SetPasswordErrorStatus -> currentState.copy(isIncorrectPassword = partialState.status)
            is PartialState.ChangeNote -> currentState.copy(note = partialState.note)
            is PartialState.SetOriginInfo -> currentState.copy(originScreen = partialState.value)
            is PartialState.NoChange -> currentState
            is PartialState.OnUserSpeaking -> currentState.copy(
                isAlertVisible = false,
                isUserSpeaking = partialState.isUserSpeaking
            )
            is PartialState.SetVoiceInputData -> currentState.copy(
                amountCalculation = partialState.voiceAmountCalculation,
                amount = partialState.voiceAmount,
                amountError = false,
                activeInputMode = partialState.inputMode,
                isIncorrectPassword = false
            )
            is PartialState.ShowDiscountLoading -> currentState.copy(canShowDiscountAddLoading = partialState.discountLoading)
            PartialState.ShowSubmitLoader -> currentState.copy(isSubmitLoading = true)
            is PartialState.HideSubmitLoader -> currentState.copy(
                isSubmitLoading = false,
                isSubmitSuccess = partialState.result
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

private fun CharSequence.isDidgitsOnly(): Boolean {
    return toString().matches("[0-9]+".toRegex())
}
