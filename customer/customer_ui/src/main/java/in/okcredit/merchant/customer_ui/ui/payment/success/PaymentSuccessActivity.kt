package `in`.okcredit.merchant.customer_ui.ui.payment.success

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.DialogPaymentSuccessBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.*

class PaymentSuccessActivity : OkcActivity() {

    private val binding: DialogPaymentSuccessBinding by viewLifecycleScoped(DialogPaymentSuccessBinding::inflate)

    private lateinit var textToSpeech: TextToSpeech

    private lateinit var utteranceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(binding.root)
        utteranceId = UUID.randomUUID().toString()
        val collectionId = intent.getStringExtra(ARG_COLLECTION_ID)
        val amount = intent.getLongExtra(ARG_PAYMENT_AMOUNT, 0L)
        val customerName = intent.getStringExtra(ARG_CUSTOMER_NAME) ?: getString(R.string.customer)
        val paymentTime = intent.getLongExtra(ARG_PAYMENT_TIME, 0L)
        val balance = intent.getLongExtra(ARG_UPDATED_BALANCE, 0L) + amount
        val balanceText = if (balance < 0) getString(R.string.due) else getString(R.string.advance)
        val lasPayment = DateTime(paymentTime)
        binding.imageClose.debounceClickListener {
            finish()
        }

        binding.textCollectionId.text =
            getString(R.string.t_002_payment_success_UTR, collectionId)
        binding.textAmountPaid.text =
            getString(R.string.t_002_payment_success_H1, customerName, CurrencyUtil.formatV2(amount))
        binding.textTime.text = getString(
            R.string.t_002_payment_success_date,
            DateTimeUtils.formatAccountStatement(this, lasPayment)
        )
        binding.textBalance.text =
            getString(R.string.t_002_payment_success_updated_balance, CurrencyUtil.formatV2(balance), balanceText)

        textToSpeech = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.ENGLISH
                textToSpeech.speak(
                    getString(R.string.received_amount_speech, (amount / 100).toString()),
                    TextToSpeech.QUEUE_FLUSH,
                    Bundle(),
                    utteranceId
                )
            }
        }
        textToSpeech.setOnUtteranceCompletedListener {
            lifecycleScope.launch {
                delay(2_000)
                finish()
            }
        }
    }

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    companion object {

        fun getIntent(
            context: Context,
            amount: Long,
            customerName: String,
            paymentTime: Long,
            balance: Long,
            collectionId: String,
        ) = Intent(context, PaymentSuccessActivity::class.java).apply {
            putExtras(
                bundleOf(
                    ARG_PAYMENT_AMOUNT to amount,
                    ARG_CUSTOMER_NAME to customerName,
                    ARG_PAYMENT_TIME to paymentTime,
                    ARG_UPDATED_BALANCE to balance,
                    ARG_COLLECTION_ID to collectionId,
                )
            )
        }

        private const val ARG_COLLECTION_ID = "arg_collection_id"
        private const val ARG_PAYMENT_AMOUNT = "arg_payment_amount"
        private const val ARG_CUSTOMER_NAME = "arg_customer_name"
        private const val ARG_PAYMENT_TIME = "arg_payment_time"
        private const val ARG_UPDATED_BALANCE = "arg_updated_balance"
    }
}
