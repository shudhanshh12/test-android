package `in`.okcredit.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.airbnb.deeplinkdispatch.BaseRegistry
import com.airbnb.deeplinkdispatch.DeepLinkEntry
import com.airbnb.deeplinkdispatch.DeepLinkUri
import com.google.gson.Gson
import dagger.Lazy
import tech.okcredit.android.communication.NotificationData
import tech.okcredit.app_contract.ResolveIntentsAndExtrasFromDeeplink
import javax.inject.Inject

class ResolveIntentsAndExtrasFromDeeplinkImpl @Inject constructor(
    private val resolveIntentsFromDeeplink: Lazy<ResolveIntentsFromDeeplink>,
) : ResolveIntentsAndExtrasFromDeeplink {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val extras = Bundle()

    companion object {

        @JvmStatic
        fun deepLinkScreen(
            context: Context?,
            primaryAction: String,
            notificationData: NotificationData?,
        ): PendingIntent {
            val intent = Intent(context, DeepLinkActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(primaryAction)
                notificationData?.let {
                    putExtra("data", Gson().toJson(it))
                }
            }

            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun execute(deepLinkUrl: String): List<Intent> {

        // Implementation copied from BaseDeepLinkDelegate.java
        // We can't use BaseDeepLinkDelegate since it starts activity in new task
        // To open in same task and in same stack, we have copied the implementation
        // This is primarily being used in DynamicComponent
        // We should switch to navigation component to avoid such implementations
        val entry = findEntry(deepLinkUrl)
        if (entry != null) {
            val deepLinkUri = DeepLinkUri.parse(deepLinkUrl)

            // Puts places holder in the url into map
            val parameterMap: MutableMap<String, String> = entry.getParameters(deepLinkUri).toMutableMap()

            // Puts query params into map
            deepLinkUri.queryParameterNames().map {
                parameterMap[it] = deepLinkUri.queryParameterValues(it).last()
            }

            // Converts map into bundle
            parameterMap.map { extras.putString(it.key, it.value) }
        }
        return resolveIntentsFromDeeplink.get().execute(deepLinkUrl, extras)
    }

    private fun findEntry(uriString: String): DeepLinkEntry? {
        val uri = DeepLinkUri.parse(uriString)

        val listOfRegistry: List<BaseRegistry> = listOf(
            AppDeepLinkModuleRegistry(),
            LibraryDeepLinkModuleRegistry()
        )

        for (registry in listOfRegistry) {
            val entryIdxMatch = registry.idxMatch(uri)
            if (entryIdxMatch != null) {
                return entryIdxMatch
            }
        }
        return null
    }
}
