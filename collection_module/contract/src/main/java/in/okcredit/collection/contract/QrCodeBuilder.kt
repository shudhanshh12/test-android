package `in`.okcredit.collection.contract

import android.net.Uri
import org.joda.time.DateTime
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object QrCodeBuilder {

    fun getQrCode(qrIntent: String?, currentBalance: Long, lastPayment: DateTime?): String {
        if (qrIntent.isNullOrEmpty()) return ""

        val newQrIntent = if (qrIntent.contains("%24current_balance")) {
            // replace place holder with real value
            qrIntent.replace("%24current_balance", (abs(currentBalance / 100.0)).toString())
        } else {
            qrIntent
        }

        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, Locale.ENGLISH)
        return parseAndBuildQrCode(qrIntent = newQrIntent) {
            it.amount = (abs(currentBalance / 100.0))
            it.minimumAmount = 1.0
            if (it.transactionNote.isNullOrEmpty()) {
                if (lastPayment != null) { // check for last payment done for customer
                    it.transactionNote = "Last paid on ${dateFormat.format(lastPayment.toDate())}"
                } else { // fallback if no payment was made
                    it.transactionNote = "Paying via OkCredit" // explicitly kept in english
                }
            }
        }
    }

    /**
     * Helper method to parse existing qr intent, take all its properties and provide a builder to add new properties
     *
     * @param qrIntent - existing qr intent string to parse
     * @param qrBuilder - builder to add new properties
     * @return New qr_intent which can be used to start activity or build qr code
     */
    fun parseAndBuildQrCode(qrIntent: String, qrBuilder: ((QrCode) -> Unit)): String {
        val existingIntent = parseQrIntent(qrIntent)
        return build(existingIntent, qrBuilder)
    }

    /**
     * Helper method to parse existing qr intent into more readable [QrCode].
     *
     * @param qrIntent - existing qr intent string to parse
     */
    fun parseQrIntent(qrIntent: String): QrCode {
        val qrUri = Uri.parse(qrIntent)
        return QrCode(
            payeeAddress = qrUri.getQueryParameter(PAYEE_ADDRESS)
                ?: throw IllegalArgumentException("Not a valid qr intent, pa not present"),
            payeeName = qrUri.getQueryParameter(PAYEE_NAME),
            amount = qrUri.getQueryParameter(AMOUNT)?.toDoubleOrNull(),
            minimumAmount = qrUri.getQueryParameter(MINIMUM_AMOUNT)?.toDoubleOrNull(),
            transactionNote = qrUri.getQueryParameter(TRANSACTION_NOTE)
        )
    }

    /**
     * Helper method to create new qr intent string which can be used to start activity or build qr code
     *
     * @param qrBuilder - builder to add new properties
     */
    fun buildQrCode(qrBuilder: ((QrCode) -> Unit)): String {
        val qrIntent = QrCode(payeeAddress = "")
        return build(qrIntent, qrBuilder)
    }

    private fun build(qrCode: QrCode, qrBuilder: ((QrCode) -> Unit)): String {
        qrBuilder(qrCode)
        if (qrCode.payeeAddress.isEmpty()) {
            throw IllegalArgumentException("Payee address(pa) cannot be empty")
        }
        val builder = Uri.Builder()
            .scheme(UPI_SCHEME)
            .authority(UPI_AUTHORITY)
            .appendQueryParameter(PAYEE_ADDRESS, qrCode.payeeAddress)

        if (!qrCode.payeeName.isNullOrBlank()) {
            builder.appendQueryParameter(PAYEE_NAME, qrCode.payeeName)
        }

        if (qrCode.amount != null && qrCode.amount!! > 0.0) {
            builder.appendQueryParameter(AMOUNT, qrCode.amount.toString())
        }

        if (qrCode.minimumAmount != null && qrCode.minimumAmount!! > 0.0) {
            builder.appendQueryParameter(MINIMUM_AMOUNT, qrCode.minimumAmount.toString())
        }

        if (!qrCode.transactionNote.isNullOrBlank()) {
            builder.appendQueryParameter(TRANSACTION_NOTE, qrCode.transactionNote)
        }

        return builder.build().toString()
    }

    private const val UPI_SCHEME = "upi"
    private const val UPI_AUTHORITY = "pay"

    private const val PAYEE_ADDRESS = "pa"
    private const val PAYEE_NAME = "pn"
    private const val AMOUNT = "am"
    private const val MINIMUM_AMOUNT = "mam"
    private const val TRANSACTION_NOTE = "tn"
}

data class QrCode(
    var payeeAddress: String,
    var payeeName: String? = null,
    var amount: Double? = null,
    var minimumAmount: Double? = null,
    var transactionNote: String? = null,
)
