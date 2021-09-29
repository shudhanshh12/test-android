package `in`.okcredit.communication_inappnotification.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Lazy
import javax.inject.Inject

class ActionAdapterFactory @Inject constructor(
    private val actions: Lazy<Map<String, @JvmSuppressWildcards Class<out Action>>>
) {

    fun newInstance(): JsonAdapter.Factory {
        var factory = PolymorphicJsonAdapterFactory.of(Action::class.java, Action.KEY_POLYMORPHISM)
        actions.get().forEach {
            factory = factory.withSubtype(it.value, it.key)
        }
        factory = factory.withDefaultValue(null)
        return factory!!
    }
}
