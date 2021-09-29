package `in`.okcredit.merchant.store.sharedprefs

import `in`.okcredit.merchant.contract.IndividualPreferencesMigration
import `in`.okcredit.merchant.store.database.BusinessDao
import androidx.core.content.edit
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import javax.inject.Inject

class IndividualPreferencesMigrationImpl @Inject constructor(
    private val businessDao: Lazy<BusinessDao>,
) : IndividualPreferencesMigration {
    override fun migration0To1() = SharedPreferencesMigration(0, 1) { userPrefs ->
        val preferences = businessDao.get().getPreferences().await()
        preferences.forEach { entry ->
            userPrefs.edit(commit = true) {
                SharedPreferencesMigration.put(entry.key, entry.value, this)
            }
        }
    }
}
