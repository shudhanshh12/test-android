package `in`.okcredit.dynamicview.data.store

import `in`.okcredit.dynamicview.R
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.dynamicview.data.store.database.CustomizationDatabaseDao
import `in`.okcredit.dynamicview.data.store.database.CustomizationEntity
import `in`.okcredit.dynamicview.di.DynamicView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.ResourceUtils
import javax.inject.Inject

@Reusable
class CustomizationStore @Inject constructor(
    @DynamicView moshi: Moshi,
    private val resourceUtils: ResourceUtils,
    private val customizationDatabaseDao: CustomizationDatabaseDao,
) {

    private val componentAdapter: JsonAdapter<ComponentModel> = moshi.adapter(ComponentModel::class.java)
    private val adapter: JsonAdapter<List<Customization>>

    init {
        val listType = Types.newParameterizedType(List::class.java, Customization::class.java)
        adapter = moshi.adapter(listType)
    }

    fun getCustomizations(businessId: String): Observable<List<Customization>> {
        return customizationDatabaseDao.getCustomizations(businessId).map {
            if (it.isEmpty()) {
                getFallbackCustomizations()
            } else {
                it.toCustomizationList()
            }
        }
    }

    fun getFallbackCustomizations(): List<Customization> {
        val json = resourceUtils.getRawResource(R.raw.customization_fallback)
        return adapter.fromJson(json)!!
    }

    suspend fun saveCustomizations(customizations: List<Customization>, businessId: String) {
        val customizationEntityList = customizations.toCustomizationEntityList(businessId)
        clearCustomizations(businessId)
        customizationDatabaseDao.insert(*customizationEntityList.toTypedArray())
    }

    private fun List<CustomizationEntity>.toCustomizationList(): List<Customization> {
        return this.map { Customization(it.target, componentAdapter.fromJson(it.componentJsonString)) }
    }

    private fun List<Customization>.toCustomizationEntityList(businessId: String): List<CustomizationEntity> {
        return this.mapNotNull {
            try {
                CustomizationEntity(it.target, componentAdapter.toJson(it.component), businessId)
            } catch (e: Exception) {
                RecordException.recordException(e)
                null
            }
        }
    }

    suspend fun clearAllCustomizations() = customizationDatabaseDao.clearAllCustomizations()

    suspend fun clearCustomizations(businessId: String) = customizationDatabaseDao.clearCustomizations(businessId)
}
