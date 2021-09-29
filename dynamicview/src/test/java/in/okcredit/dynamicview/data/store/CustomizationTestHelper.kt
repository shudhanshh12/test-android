package `in`.okcredit.dynamicview.data.store

import `in`.okcredit.dynamicview.component.banner.BannerComponentModel
import `in`.okcredit.dynamicview.component.cell.CellComponentModel
import `in`.okcredit.dynamicview.component.dashboard.advertisement.AdvertisementComponentModel
import `in`.okcredit.dynamicview.component.dashboard.summary_card.SummaryCardComponentModel
import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import `in`.okcredit.dynamicview.component.recycler.RecyclerComponentModel
import `in`.okcredit.dynamicview.component.toolbar.ToolbarComponentModel
import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ActionAdapterFactory
import `in`.okcredit.dynamicview.data.model.ComponentAdapterFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.dynamicview.data.store.database.CustomizationEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

class CustomizationTestHelper {

    val moshi = Moshi.Builder()
        .add(
            ActionAdapterFactory(
                mapOf(
                    Action.Navigate.NAME to Action.Navigate::class.java,
                    Action.Track.NAME to Action.Track::class.java
                )
            ).newInstance()
        )
        .add(
            ComponentAdapterFactory(
                mapOf(
                    MenuComponentModel.KIND to MenuComponentModel::class.java,
                    BannerComponentModel.KIND to BannerComponentModel::class.java,
                    CellComponentModel.KIND to CellComponentModel::class.java,
                    ToolbarComponentModel.KIND to ToolbarComponentModel::class.java,
                    RecyclerComponentModel.Kind.VERTICAL to RecyclerComponentModel::class.java,
                    RecyclerComponentModel.Kind.HORIZONTAL to RecyclerComponentModel::class.java,
                    RecyclerComponentModel.Kind.GRID to RecyclerComponentModel::class.java,
                    AdvertisementComponentModel.KIND to AdvertisementComponentModel::class.java,
                    SummaryCardComponentModel.KIND to SummaryCardComponentModel::class.java
                )
            ).newInstance()
        )
        .build()

    private val listType = Types.newParameterizedType(List::class.java, Customization::class.java)
    private val adapter = moshi.adapter<List<Customization>>(listType)
    private val componentModelAdapter = moshi.adapter(ComponentModel::class.java)

    fun getDummyCustomizations() = adapter.fromJson(getDummyCustomizationsJson()) ?: emptyList()
    fun getDummyCustomizationEntities() = adapter.fromJson(getDummyCustomizationsJson())
        ?.map { CustomizationEntity(it.target, componentModelAdapter.toJson(it.component), "business-id") }
        ?: emptyList()

    fun getDummyCustomizationsJson(): String {
        val inputStream = javaClass.classLoader?.getResourceAsStream("dummy_customizations.json")
        val s: Scanner = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
