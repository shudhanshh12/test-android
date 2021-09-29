package tech.okcredit.user_migration.contract

import androidx.fragment.app.FragmentManager

interface UserMigrationNavigator {
    fun showUploadOptionBottomSheet(fragmentManager: FragmentManager)
}
