package `in`.okcredit.ui.whatsapp

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class GetWhatsAppNumber @Inject constructor(private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>) {

    companion object {
        private const val FRC_WHATSAPP_KEY = "help_number"
    }

    fun execute(): String = firebaseRemoteConfig.get().getString(FRC_WHATSAPP_KEY)
}
