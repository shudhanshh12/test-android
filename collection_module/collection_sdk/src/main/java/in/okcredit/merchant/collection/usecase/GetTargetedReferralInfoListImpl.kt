package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.backend.contract.GetSpecificCustomerList
import `in`.okcredit.collection.contract.GetTargetedReferralInfoList
import `in`.okcredit.collection.contract.GetTargetedReferralList
import `in`.okcredit.collection.contract.TargetedCustomerReferralInfo
import dagger.Lazy
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class GetTargetedReferralInfoListImpl @Inject constructor(
    private val getTargetedReferralList: Lazy<GetTargetedReferralList>,
    private val getSpecificCustomerList: Lazy<GetSpecificCustomerList>,
) : GetTargetedReferralInfoList {
    override fun execute(): Observable<List<TargetedCustomerReferralInfo>> {
        return getTargetedReferralList.get().execute().flatMap { customerAdditionalInfoList ->
            val finalTargetedList = ArrayList<TargetedCustomerReferralInfo>()

            val customerIdListMap = customerAdditionalInfoList.associateBy { it.id }

            // get all customer from db and then create TargetedCustomerReferralInfo list with both list
            getSpecificCustomerList.get().execute(customerIdListMap.keys.toList()).map { customerList ->
                finalTargetedList.clear()
                customerList.forEach { customer ->
                    customerIdListMap[customer.id]?.let { customerAdditionalInfoList ->
                        finalTargetedList.add(
                            TargetedCustomerReferralInfo(
                                id = customer.id,
                                mobile = customer.mobile,
                                profileImage = customer.profileImage,
                                description = customer.description,
                                link = customerAdditionalInfoList.link,
                                status = customerAdditionalInfoList.status,
                                amount = customerAdditionalInfoList.amount,
                                message = customerAdditionalInfoList.message,
                                youtubeLink = customerAdditionalInfoList.youtubeLink,
                                customerMerchantId = customerAdditionalInfoList.customerMerchantId,
                                ledgerSeen = customerAdditionalInfoList.ledgerSeen
                            )
                        )
                    }
                }

                finalTargetedList
            }
        }
    }
}
