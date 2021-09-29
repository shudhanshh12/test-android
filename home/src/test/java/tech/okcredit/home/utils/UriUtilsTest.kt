package tech.okcredit.home.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.okcredit.home.R
import tech.okcredit.home.utils.UriUtils.replaceLastSegmentWithValue

@RunWith(RobolectricTestRunner::class)
class UriUtilsTest {

    @Test
    fun `getString() should return customer profile deeplink with placeholder`() {
        val deeplinkWithPlaceholder = "okcredit://merchant/v1/home/customer_profile/{customer_id}"
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertTrue(context.getString(R.string.customer_profile_dialog_deeplink) == deeplinkWithPlaceholder)
    }

    @Test
    fun `replaceLastSegmentWithValue() should replace customer profile deeplink placeholder with customer id`() {
        val customerId = "hb1c8-b1ca-18bc1b-8a5788475b"
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deeplinkWithPlaceholder = context.getString(R.string.customer_profile_dialog_deeplink)

        val deeplink = deeplinkWithPlaceholder.replaceLastSegmentWithValue(customerId)

        assertTrue(deeplink == "okcredit://merchant/v1/home/customer_profile/$customerId")
    }
}
