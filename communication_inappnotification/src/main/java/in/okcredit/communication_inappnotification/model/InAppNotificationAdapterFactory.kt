package `in`.okcredit.communication_inappnotification.model

import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Lazy
import javax.inject.Inject

class InAppNotificationAdapterFactory @Inject constructor(
    private val notifications: Lazy<Map<String, @JvmSuppressWildcards Class<out InAppNotification>>>
) {

    fun newInstance(): JsonAdapter.Factory {
        var factory =
            PolymorphicJsonAdapterFactory.of(InAppNotification::class.java, InAppNotification.KEY_POLYMORPHISM)
        notifications.get().forEach {
            factory = factory.withSubtype(it.value, it.key)
        }
        factory = factory.withDefaultValue(null)
        return factory!!
    }
}
