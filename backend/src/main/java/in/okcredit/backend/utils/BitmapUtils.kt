package `in`.okcredit.backend.utils

import `in`.okcredit.backend.R
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.utils.DateTimeUtils

class BitmapUtils {
    companion object {

        fun createTransactionClusterBitmap(
            context: Context,
            customer: Customer,
            business: Business,
            transaction: Transaction
        ): Bitmap {
            val cluster = LayoutInflater.from(context).inflate(R.layout.tx_share_layout, null)

            cluster.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            cluster.layout(
                0, 0,
                cluster.measuredWidth,
                cluster.measuredHeight
            )

            val merchantName = cluster.findViewById(R.id.merchantName) as TextView
            val address = cluster.findViewById(R.id.address) as TextView
            val rootLayout = cluster.findViewById(R.id.rootLayout) as RelativeLayout
            val phoneNumberMerchant = cluster.findViewById(R.id.phone_number_merchant) as TextView
            val phoneNumberCustomer = cluster.findViewById(R.id.phone_number_customer) as TextView
            val txType = cluster.findViewById(R.id.tx_type) as TextView
            val amount = cluster.findViewById(R.id.amount) as TextView
            val date = cluster.findViewById(R.id.date) as TextView
            val noteContainer = cluster.findViewById(R.id.note_container) as LinearLayout
            val note = cluster.findViewById(R.id.note) as TextView

            merchantName.text = business.name
            phoneNumberCustomer.text = business.mobile

            if (business.address.isNullOrEmpty()) {
                address.visibility = View.GONE
            } else {
                address.visibility = View.VISIBLE
                address.text = business.address
            }

            if (business.mobile.isEmpty()) {
                phoneNumberMerchant.visibility = View.GONE
            } else {
                phoneNumberMerchant.visibility = View.VISIBLE
                phoneNumberMerchant.text = business.mobile
            }

            if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
                txType.text = context.getString(R.string.payment_amount)
            } else if (transaction.type == Transaction.CREDIT) {
                txType.text = context.getString(R.string.credit_amount)
            }

            CurrencyUtil.renderV2(transaction.amountV2, amount, transaction.type)

            date.text = DateTimeUtils.formatDateOnly(transaction.billDate)

            if (transaction.note.isNullOrEmpty()) {
                noteContainer.visibility = View.GONE
            } else {
                noteContainer.visibility = View.VISIBLE
                note.text = transaction.note
            }

            phoneNumberCustomer.text = customer.mobile

            rootLayout.isDrawingCacheEnabled = true
            rootLayout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            rootLayout.layout(0, 0, rootLayout.measuredWidth, rootLayout.measuredHeight)
            rootLayout.buildDrawingCache(true)

            return rootLayout.drawingCache
        }

        fun createSupplierTransactionClusterBitmap(
            context: Context,
            supplier: Supplier,
            business: Business,
            transaction: `in`.okcredit.merchant.suppliercredit.Transaction
        ): Bitmap {
            val cluster = LayoutInflater.from(context).inflate(R.layout.tx_share_layout, null)

            cluster.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            cluster.layout(
                0, 0,
                cluster.measuredWidth,
                cluster.measuredHeight
            )

            val merchantName = cluster.findViewById(R.id.merchantName) as TextView
            val address = cluster.findViewById(R.id.address) as TextView
            val rootLayout = cluster.findViewById(R.id.rootLayout) as RelativeLayout
            val phoneNumberMerchant = cluster.findViewById(R.id.phone_number_merchant) as TextView
            val phoneNumberCustomer = cluster.findViewById(R.id.phone_number_customer) as TextView
            val txType = cluster.findViewById(R.id.tx_type) as TextView
            val amount = cluster.findViewById(R.id.amount) as TextView
            val date = cluster.findViewById(R.id.date) as TextView
            val noteContainer = cluster.findViewById(R.id.note_container) as LinearLayout
            val note = cluster.findViewById(R.id.note) as TextView

            merchantName.text = business.name
            phoneNumberCustomer.text = business.mobile

            if (business.address.isNullOrEmpty()) {
                address.visibility = View.GONE
            } else {
                address.visibility = View.VISIBLE
                address.text = business.address
            }

            if (business.mobile.isEmpty()) {
                phoneNumberMerchant.visibility = View.GONE
            } else {
                phoneNumberMerchant.visibility = View.VISIBLE
                phoneNumberMerchant.text = business.mobile
            }

            if (transaction.payment) {
                txType.text = context.getString(R.string.payment_amount)
            } else {
                txType.text = context.getString(R.string.credit_amount)
            }

            CurrencyUtil.renderV2(transaction.amount, amount, transaction.payment)

            date.text = DateTimeUtils.formatDateOnly(transaction.billDate)

            if (transaction.note.isNullOrEmpty()) {
                noteContainer.visibility = View.GONE
            } else {
                noteContainer.visibility = View.VISIBLE
                note.text = transaction.note
            }

            phoneNumberCustomer.text = supplier.mobile

            rootLayout.isDrawingCacheEnabled = true
            rootLayout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            rootLayout.layout(0, 0, rootLayout.measuredWidth, rootLayout.measuredHeight)
            rootLayout.buildDrawingCache(true)

            return rootLayout.drawingCache
        }
    }
}
