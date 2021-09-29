package `in`.okcredit.dynamicview.data.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import javax.inject.Inject

class ComponentAdapterFactory @Inject constructor(
    private val components: Map<String, @JvmSuppressWildcards Class<out ComponentModel>>
) {

    fun newInstance(): JsonAdapter.Factory {
        var factory = PolymorphicJsonAdapterFactory.of(ComponentModel::class.java, ComponentModel.KEY_POLYMORPHISM)
        components.forEach {
            factory = factory.withSubtype(it.value, it.key)
        }
        factory = factory.withDefaultValue(null)
        return factory!!
    }
}
