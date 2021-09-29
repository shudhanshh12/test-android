package `in`.okcredit.voice_first.ui.bulk_add.search_merchant

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import android.content.Intent as AndroidIntent

class SearchMerchantActivityContract : ActivityResultContract<Void, DraftMerchant?>() {
    override fun createIntent(context: Context, input: Void?): AndroidIntent {
        return SearchMerchantActivity.getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: AndroidIntent?): DraftMerchant? {
        return intent?.toDraftMerchant()
    }

    // Refactor to method if making it public as it is tightly coupled with the SearchContract
    private fun AndroidIntent.toDraftMerchant(): DraftMerchant? {
        val merchantId = getStringExtra(KEY_MERCHANT_ID) ?: return null
        val merchantType = getStringExtra(KEY_MERCHANT_TYPE) ?: return null
        val merchantName = getStringExtra(KEY_MERCHANT_NAME)

        return DraftMerchant(
            merchantId = merchantId,
            merchantType = merchantType,
            merchantName = merchantName
        )
    }

    companion object {

        private const val KEY_MERCHANT_ID = "merchant_id"
        private const val KEY_MERCHANT_TYPE = "merchant_type"
        private const val KEY_MERCHANT_NAME = "merchant_name"

        fun intentFromMerchant(merchant: DraftMerchant) = AndroidIntent().apply {
            putExtra(KEY_MERCHANT_ID, merchant.merchantId)
            putExtra(KEY_MERCHANT_NAME, merchant.merchantName)
            putExtra(KEY_MERCHANT_TYPE, merchant.merchantType)
        }
    }
}
