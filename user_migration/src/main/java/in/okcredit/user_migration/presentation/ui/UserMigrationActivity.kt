package `in`.okcredit.user_migration.presentation.ui

import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.UserMigrationActivityBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class UserMigrationActivity : OkcActivity() {

    private val binding: UserMigrationActivityBinding by viewLifecycleScoped(UserMigrationActivityBinding::inflate)

    companion object {

        fun getIntent(context: Context) =
            Intent(context, UserMigrationActivity::class.java)

        @JvmStatic
        fun start(context: Context) {
            context.startActivity(getIntent(context))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.user_migration_container, NavHostFragment.create(R.navigation.user_migration_flow))
            .commit()
    }

    override fun onBackPressed() {
        val isBackStackEmpty = findNavController(R.id.user_migration_container).popBackStack()
        if (!isBackStackEmpty) {
            super.onBackPressed()
        }
    }
}
