package tech.okcredit.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.help.databinding.HelpActivityBinding
import tech.okcredit.userSupport.ContextualHelp

class HelpActivity : OkcActivity() {

    companion object {

        const val HELP_ID = "help_id"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_CONTEXTUAL_HELP = "contextual_help"

        fun start(context: Context, source: String, contextualHelp: ContextualHelp? = null) {
            val intent = Intent(context, HelpActivity::class.java)
                .putExtra(EXTRA_SOURCE, source)
                .putExtra(EXTRA_CONTEXTUAL_HELP, contextualHelp)
            context.startActivity(intent)
        }
    }

    private val binding: HelpActivityBinding by viewLifecycleScoped(HelpActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
