package `in`.okcredit.user_migration.presentation

import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheet
import androidx.fragment.app.FragmentManager
import tech.okcredit.user_migration.contract.UserMigrationNavigator
import javax.inject.Inject

class UserMigrationNavigatorImpl @Inject constructor() : UserMigrationNavigator {

    override fun showUploadOptionBottomSheet(fragmentManager: FragmentManager) {
        UploadOptionBottomSheet.show(fragmentManager)
    }
}
