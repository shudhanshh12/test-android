package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.GetBusinessIdList
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import tech.okcredit.android.base.preferences.DefaultPreferences
import javax.inject.Inject

@Reusable
class GetBusinessIdListImpl @Inject constructor(
    private val businessRepository: Lazy<BusinessRepositoryImpl>,
) : GetBusinessIdList, DefaultPreferences.GetBusinessIdListForDefaultPreferencesMigration {
    override fun execute(): Flow<List<String>> {
        return businessRepository.get().getBusinessIdList()
    }
}
