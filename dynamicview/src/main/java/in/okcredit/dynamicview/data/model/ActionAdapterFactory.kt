package `in`.okcredit.dynamicview.data.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import javax.inject.Inject

class ActionAdapterFactory @Inject constructor(
    private val actions: Map<String, @JvmSuppressWildcards Class<out Action>>
) {

    fun newInstance(): JsonAdapter.Factory {
        var factory = PolymorphicJsonAdapterFactory.of(Action::class.java, Action.KEY_POLYMORPHISM)
        actions.forEach {
            factory = factory.withSubtype(it.value, it.key)
        }
        factory = factory.withDefaultValue(null)
        return factory!!
    }
}
