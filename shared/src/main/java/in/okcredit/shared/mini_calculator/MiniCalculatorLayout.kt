package `in`.okcredit.shared.mini_calculator

import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.mini_calculator_layout.view.*
import org.jetbrains.annotations.Nullable

class MiniCalculatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseLayout<MiniCalculatorContract.State>(context, attrs, defStyleAttr),
    MiniCalculatorContract.Interactor,
    @Nullable MiniCalculatorView.CalcListener {
    init {
        LayoutInflater.from(context).inflate(R.layout.mini_calculator_layout, this, true)
        viewModel.setNavigation(this)
    }

    private val onDigitClicked: PublishSubject<Int> = PublishSubject.create()
    private val onDotClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onLongBackPress: PublishSubject<Unit> = PublishSubject.create()
    private val onBackPressClicked: PublishSubject<Unit> = PublishSubject.create()
    private var callback: MiniCalculatorContract.Callback? = null
    private val loadSubject = BehaviorSubject.create<MiniCalculatorContract.IntialData>()

    override fun loadIntent(): UserIntent {
        return MiniCalculatorContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {

        return Observable.mergeArray(
            loadSubject.map {
                MiniCalculatorContract.Intent.LoadInitialData(it)
            },
            onDigitClicked
                .map {
                    MiniCalculatorContract.Intent.OnDigitClicked(
                        it
                    )
                },

            onDotClicked
                .map { MiniCalculatorContract.Intent.OnDotClicked },

            onLongBackPress
                .map { MiniCalculatorContract.Intent.OnLongPressBackSpace },

            onBackPressClicked
                .map { MiniCalculatorContract.Intent.OnBackSpaceClicked }

        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        recycler_view.withModels {
            cancelPendingModelBuild()
            miniCalculatorView {
                id("calculatorView")
                listener(this@MiniCalculatorLayout)
            }
        }
    }

    override fun render(state: MiniCalculatorContract.State) {
        state.amountCalculation?.let {
            callback?.miniCallbackData(state.amountCalculation, state.amount)
        }
    }

    fun setData(
        callback: MiniCalculatorContract.Callback
    ) {
        this.callback = callback
    }

    override fun onDigitClicked(d: Int) {
        onDigitClicked.onNext(d)
    }

    override fun onDotClicked() {
        onDotClicked.onNext(Unit)
    }

    override fun onBackspaceLongPress() {
        onLongBackPress.onNext(Unit)
    }

    override fun onBackspaceClicked() {
        onBackPressClicked.onNext(Unit)
    }

    fun clear() {
        recycler_view.clear()
    }
}
