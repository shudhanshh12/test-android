
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.userSupport.SupportRepositoryImpl
import tech.okcredit.userSupport.model.Help
import tech.okcredit.userSupport.model.HelpInstruction
import tech.okcredit.userSupport.model.HelpItem
import tech.okcredit.userSupport.server.SupportRemoteSourceImpl
import tech.okcredit.userSupport.store.SupportLocalSourceImpl

class SupportRepositoryTest {
    private val store: SupportLocalSourceImpl = mock()
    private val server: SupportRemoteSourceImpl = mock()
    private val workManager: OkcWorkManager = mock()
    private lateinit var supportRepository: SupportRepositoryImpl

    companion object {
        val helpList = listOf(
            Help(
                "how_to_use_Help", "https://d2vo9sg0p6n7i7.cloudfront.net/icons/add_customer.webp_Help",
                "Add a new customer_Help", "transaction_Help",
                listOf(
                    HelpItem(
                        "add_customer_HelpItem",
                        "Add a new customer_HelpItem",
                        "Create a new customer account in your OkCredit_HelpItem",
                        "video_HelpItem",
                        "He7kETWJ7CQ_HelpItem",
                        listOf(
                            HelpInstruction(
                                "add_customer_HelpInstruction",
                                "https://d2vo9sg0p6n7i7.cloudfront.net/images/addCustomer-en-1.webp_HelpInstruction",
                                "Add a new customer_HelpInstruction",
                                "video_HelpInstruction"
                            )
                        )

                    )
                )
            )
        )
    }

    @Before
    fun setUp() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        supportRepository = SupportRepositoryImpl({ store }, { server }, workManager)
    }

    @Test
    fun getHelpTestResponse() {
        // given
        whenever(store.getHelp()).thenReturn(Observable.just(helpList))

        // when
        val actualOutPut = supportRepository.getHelp().test()

        // then
        actualOutPut.assertValueCount(1)
        val resultList = actualOutPut.values()[0]
        Assert.assertEquals(resultList[0].id, "how_to_use_Help")
        Assert.assertEquals(resultList[0].help_items?.get(0)?.id, "add_customer_HelpItem")
        Assert.assertEquals(resultList[0].help_items?.get(0)?.instructions?.size, 1)
    }
}
