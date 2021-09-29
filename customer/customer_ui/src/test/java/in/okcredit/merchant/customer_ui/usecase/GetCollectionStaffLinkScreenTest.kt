package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetCollectionStaffLinkScreenTest {

    private lateinit var getCollectionStaffLinkScreen: GetCollectionStaffLinkScreen

    private val customerRepositoryImpl: CustomerRepositoryImpl = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()
    private val businessId = "businessId"

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        val firebaseCrashlytics: FirebaseCrashlytics = mockk()
        justRun { firebaseCrashlytics.recordException(any()) }

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        getCollectionStaffLinkScreen = GetCollectionStaffLinkScreen(
            customerRepositoryImpl = { customerRepositoryImpl },
            getActiveBusinessId = { getActiveBusinessId }
        )
    }

    @Test
    fun `when active link present show active screen`() = runBlocking {
        coJustRun { customerRepositoryImpl.setStaffLinkEducation(true) }
        coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
        coEvery { customerRepositoryImpl.activeStaffLinkDetails(businessId) } returns ActiveStaffLinkResponse(
            accountIds = listOf(
                "customer_1",
                "customer_2"
            ),
            link = "link",
            linkId = "link_id",
            createTime = 1000000L,
        )

        val screen = getCollectionStaffLinkScreen.execute()
        assert(
            screen == GetCollectionStaffLinkScreen.StaffLinkScreen.ActiveStaffLink(
                linkId = "link_id",
                link = "link",
                customerIds = listOf("customer_1", "customer_2"),
                createTime = 1000000_000L,
            )
        )
        coVerify { customerRepositoryImpl.setStaffLinkEducation(true) }

        return@runBlocking
    }

    @Test
    fun `when active link not present and need to show education`() = runBlocking {
        coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
        coEvery { customerRepositoryImpl.activeStaffLinkDetails(businessId) } returns ActiveStaffLinkResponse(
            null,
            null,
            null,
            null
        )
        coEvery { customerRepositoryImpl.staffLinkEducationShown() } returns false
        coJustRun { customerRepositoryImpl.setStaffLinkEducation(true) }

        assert(getCollectionStaffLinkScreen.execute() == GetCollectionStaffLinkScreen.StaffLinkScreen.Education)

        coVerify { customerRepositoryImpl.setStaffLinkEducation(true) }
        return@runBlocking
    }

    @Test
    fun `when active link not present, and education shown then select customer`() = runBlocking {
        coEvery { customerRepositoryImpl.staffLinkEducationShown() } returns true
        coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
        coEvery { customerRepositoryImpl.activeStaffLinkDetails(businessId) } returns ActiveStaffLinkResponse(
            accountIds = null,
            link = null,
            linkId = null,
            createTime = null
        )

        assert(getCollectionStaffLinkScreen.execute() == GetCollectionStaffLinkScreen.StaffLinkScreen.SelectCustomer)
        return@runBlocking
    }
}
