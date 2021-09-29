import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.contract.UpdateBusinessRequest
import `in`.okcredit.merchant.usecase.UpdateBusinessImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class UpdateBusinessImplTest {
    private val businessApi: BusinessRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val updateMerchantImpl = UpdateBusinessImpl(businessApi, { getActiveBusinessId })

    companion object {
        val updatedValue = "updated Value"
        const val BUSINESS_NAME = 1
        const val CATEGORY = 3
        const val ADDRESS = 5
        const val ABOUT = 7
        const val PERSON_NAME = 8
        const val PROFILE_IMAGE = 9
        val businessId = "businessId"
        val request = Request(
            inputType = 5,
            updatedValue = updatedValue,
            address = Triple("first", 2.toDouble(), 3.toDouble()),
            category = Pair("test1", "test2")
        )
    }

    @Test
    fun `executeTest BUSINESS_NAME`() {
        // given
        whenever(
            businessApi.updateBusiness(
                UpdateBusinessRequest.UpdateBusinessName(updatedValue),
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val request = request.copy(inputType = BUSINESS_NAME)

        // when
        val result = updateMerchantImpl.execute(request).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `executeTest ADDRESS`() {
        // given
        whenever(
            businessApi.updateBusiness(
                UpdateBusinessRequest.UpdateAddress(
                    request.address?.first!!,
                    request.address?.second!!,
                    request.address?.third!!
                ),
                businessId
            )
        ).thenReturn(
            Completable.complete()
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val request = request.copy(inputType = ADDRESS)

        // when
        val result = updateMerchantImpl.execute(request).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `executeTest ABOUT`() {
        // given
        val updateAbout = UpdateBusinessRequest.UpdateAbout(request.updatedValue!!)
        whenever(businessApi.updateBusiness(updateAbout, businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val request = request.copy(inputType = ABOUT)

        // when
        val result = updateMerchantImpl.execute(request).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `executeTest PERSON_NAME`() {
        // given
        val updateName = UpdateBusinessRequest.UpdateName(request.updatedValue!!)
        whenever(businessApi.updateBusiness(updateName, businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val request = request.copy(inputType = PERSON_NAME)

        // when
        val result = updateMerchantImpl.execute(request).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `executeTest PROFILE_IMAGE`() {
        // given
        val updateProfileImage = UpdateBusinessRequest.UpdateProfileImage(request.updatedValue!!)
        whenever(businessApi.updateBusiness(updateProfileImage, businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val request = request.copy(inputType = PROFILE_IMAGE)

        // when
        val result = updateMerchantImpl.execute(request).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `executeTest CATEGORY`() {
        // given
        val updateCategory = UpdateBusinessRequest.UpdateCategory(request.category?.first!!, request.category?.second!!)
        whenever(businessApi.updateBusiness(updateCategory, businessId)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val request = request.copy(inputType = CATEGORY)

        // when
        val result = updateMerchantImpl.execute(request).test()

        // then
        result.assertComplete()
    }
}
