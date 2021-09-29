package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.TestData
import android.content.Context
import android.content.Intent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper

class GetSharableReportIntentTest {

    private lateinit var getSharableReportIntent: GetSharableReportIntent
    private val context: Context = mock()
    private val communicationApi: CommunicationRepository = mock()

    @Before
    fun setup() {
        getSharableReportIntent = GetSharableReportIntent(
            context = context,
            communicationApi = communicationApi,
        )
    }

    @Test
    fun `execute return intent successfully`() {

        whenever(context.getString(R.string.share_msg)).thenReturn("share_text")

        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = "share_text",
            phoneNumber = TestData.CUSTOMER.mobile,
            imageFrom = ImagePath.PdfUriFromRemote(
                context = context,
                fileUrl = "link",
                destinationPath = "share",
                fileName = "Monthly Report.pdf"
            ),
            uriType = "application/pdf"
        )

        val intent = Intent()

        whenever(communicationApi.goToWhatsApp(whatsappIntentBuilder)).thenReturn(Single.just(intent))

        val observer = getSharableReportIntent.execute(
            TestData.CUSTOMER.mobile,
            "link",
            "Monthly Report.pdf"
        ).test()

        observer.assertValue(intent)

        verify(context).getString(R.string.share_msg)
        verify(communicationApi).goToWhatsApp(whatsappIntentBuilder)
    }

    @Test
    fun `execute return error whatsapp not installed`() {

        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = "share_text",
            phoneNumber = TestData.CUSTOMER.mobile,
            imageFrom = ImagePath.PdfUriFromRemote(
                context = context,
                fileUrl = "link",
                destinationPath = "share",
                fileName = "Monthly Report.pdf"
            ),
            uriType = "application/pdf"
        )

        val intent = Intent()

        val exception = IntentHelper.NoWhatsAppError()

        whenever(context.getString(R.string.share_msg)).thenReturn("share_text")

        whenever(communicationApi.goToSharableApp(whatsappIntentBuilder)).thenReturn(Single.just(intent))

        whenever(communicationApi.goToWhatsApp(whatsappIntentBuilder)).thenReturn(Single.error(exception))

        val observer = getSharableReportIntent.execute(
            TestData.CUSTOMER.mobile,
            "link",
            "Monthly Report.pdf"
        ).test()

        observer.assertValue(intent)

        verify(context).getString(R.string.share_msg)
        verify(communicationApi).goToWhatsApp(whatsappIntentBuilder)
        verify(communicationApi).goToSharableApp(whatsappIntentBuilder)
    }

    @Test
    fun `execute return Some other error`() {

        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = "share_text",
            phoneNumber = TestData.CUSTOMER.mobile,
            imageFrom = ImagePath.PdfUriFromRemote(
                context = context,
                fileUrl = "link",
                destinationPath = "share",
                fileName = "Monthly Report.pdf"
            ),
            uriType = "application/pdf"
        )

        val intent = Intent()

        val exception = Exception("Some other error")

        whenever(context.getString(R.string.share_msg)).thenReturn("share_text")

        whenever(communicationApi.goToSharableApp(whatsappIntentBuilder)).thenReturn(Single.just(intent))

        whenever(communicationApi.goToWhatsApp(whatsappIntentBuilder)).thenReturn(Single.error(exception))

        val observer = getSharableReportIntent.execute(
            TestData.CUSTOMER.mobile,
            "link",
            "Monthly Report.pdf"
        ).test()

        observer.assertError(exception)

        verify(context).getString(R.string.share_msg)
        verify(communicationApi).goToWhatsApp(whatsappIntentBuilder)
    }
}
