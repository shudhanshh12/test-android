package `in`.okcredit.shared.view

import `in`.okcredit.analytics.Tracker
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dagger.Lazy
import tech.okcredit.android.base.activity.OkcActivity
import javax.inject.Inject

class CallPermissionActivity : OkcActivity() {

    companion object {
        private const val SOURCE = "Call Permission Screen"
        private const val EXTRA_URI = "extra_uri"

        fun start(context: Context, uri: Uri) {
            val starter = Intent(context, CallPermissionActivity::class.java).apply {
                putExtra(EXTRA_URI, uri)
            }
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var tracker: Lazy<Tracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(EXTRA_URI)) {
            permissionResult.launch(Manifest.permission.CALL_PHONE)
        } else {
            finish()
        }
    }

    private val permissionResult: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        val intent = if (result) {
            // Call support directly
            tracker.get().trackCallPermissionGranted(SOURCE)
            Intent(Intent.ACTION_CALL).apply {
                data = intent.getParcelableExtra(EXTRA_URI)
            }
        } else {
            // Open dialer
            tracker.get().trackCallPermissionDenied(SOURCE)
            Intent(Intent.ACTION_DIAL).apply {
                data = intent.getParcelableExtra(EXTRA_URI)
            }
        }
        startActivity(intent)
        finish()
    }
}
