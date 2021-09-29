package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.analytics.Screen
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.AddTxnTransparentActivityBinding
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerContract.*
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.util.*

class AddTxnContainerActivity : BaseActivity<State, ViewEvent, AddTxnContainerContract.Intent>("AddTxnContainer") {

    private val binding: AddTxnTransparentActivityBinding by viewLifecycleScoped(AddTxnTransparentActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(binding.root)

        // this is only done because of readability
        val finalHost = when (intent.extras!!.getString(ADD_TRANSACTION_SCREEN_TYPE)) {
            ADD_TRANSACTION_SCREEN -> {
                NavHostFragment.create(R.navigation.add_txn_flow)
            }
            ADD_TRANSACTION_WITH_AMOUNT -> {
                NavHostFragment.create(
                    R.navigation.add_txn_flow,
                    bundleOf(
                        AMOUNT to intent.getLongExtra(AMOUNT, 0)
                    )
                )
            }
            ADD_TRANSACTION_ROBOFLOW -> {
                NavHostFragment.create(
                    R.navigation.add_txn_flow,
                    bundleOf(
                        ADD_TRANSACTION_ROBOFLOW to true
                    )
                )
            }
            else -> NavHostFragment.create(R.navigation.add_txn_flow)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, finalHost)
            .commit()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val navHostFragment: NavHostFragment? = supportFragmentManager.primaryNavigationFragment as NavHostFragment?
        navHostFragment?.childFragmentManager?.primaryNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun loadIntent(): UserIntent {
        return AddTxnContainerContract.Intent.Load
    }

    override fun render(state: State) {
        if (state.loading || state.customerName.isEmpty()) return

        binding.textCustomerName.text = state.customerName
        CurrencyUtil.renderV2(state.balanceDue, binding.textBalance, 0)

        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                state.customerName.substring(0, 1).uppercase(Locale.getDefault()),
                ColorGenerator.MATERIAL.getColor(state.customerName)
            )

        if (state.customerProfile.isNullOrEmpty()) {
            binding.imageProfile.setImageDrawable(defaultPic)
        } else {
            GlideApp.with(this)
                .load(state.customerProfile)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .thumbnail(0.25f)
                .into(binding.imageProfile)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowError -> longToast(event.error)
        }
    }

    companion object {
        @JvmStatic
        fun getIntent(
            context: Context,
            customerId: String,
            txnType: Int,
            addTransactionScreenType: String? = null,
            amount: Long = 0L,
            source: Source = Source.CUSTOMER_SCREEN,
        ) = Intent(context, AddTxnContainerActivity::class.java).apply {
            putExtra(CUSTOMER_ID, customerId)
            putExtra(TRANSACTION_TYPE, txnType)
            putExtra(AMOUNT, amount)
            putExtra(ADD_TRANSACTION_SCREEN_TYPE, addTransactionScreenType)
            putExtra(SOURCE, source)
        }

        @JvmStatic
        fun getAddTransactionIntent(
            context: Context,
            customerId: String,
            source: Source = Source.CUSTOMER_SCREEN,
        ) = Intent(context, AddTxnContainerActivity::class.java).apply {
            putExtra(CUSTOMER_ID, customerId)
            putExtra(SOURCE, source)
        }

        const val CUSTOMER_ID = "customer_id"
        const val TRANSACTION_TYPE = "transaction_type"
        const val AMOUNT = "amount"
        const val ADD_TRANSACTION_SCREEN_TYPE = "add_transaction_screen_type"
        const val SOURCE = "source_screen"
        const val ADD_TRANSACTION_SCREEN = "add_transaction_screen"
        const val ADD_TRANSACTION_ROBOFLOW = "add_transaction_roboflow"
        const val ADD_TRANSACTION_WITH_AMOUNT = "add_transaction_with_amount"
    }

    enum class Source(val value: String) {
        CUSTOMER_SCREEN(Screen.CUSTOMER_SCREEN),
        CUSTOMER_SCREEN_VOICE_TRANSACTION(Screen.CUSTOMER_SCREEN_VOICE_TRANSACTION),
        ADD_TRANSACTION_SHORTCUT_SCREEN(Screen.ADD_TRANSACTION_SHORTCUT),
        DELETE_CUSTOMER_SCREEN(Screen.DELETE_CUSTOMER)
    }
}
