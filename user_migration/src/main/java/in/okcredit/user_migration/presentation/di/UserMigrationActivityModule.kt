package `in`.okcredit.user_migration.presentation.di

import `in`.okcredit.user_migration.presentation.ui.UserMigrationActivity
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataFragment
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.di_.DisplayParsedDataModule
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailsBottomSheet
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet._di.EditDetailsBottomSheetModule
import `in`.okcredit.user_migration.presentation.ui.file_pick.di.UploadFileListModule
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickerFragment
import `in`.okcredit.user_migration.presentation.ui.upload_status.di.UploadFileStatusModule
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusFragment
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class UserMigrationActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [DisplayParsedDataModule::class])
    abstract fun displayParsedDataScreen(): DisplayParsedDataFragment

    @Binds
    abstract fun activity(activity: UserMigrationActivity): AppCompatActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [UploadFileListModule::class])
    abstract fun uploadListScreen(): FilePickerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [UploadFileStatusModule::class])
    abstract fun uploadStatusScreen(): UploadFileStatusFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [EditDetailsBottomSheetModule::class])
    abstract fun editDetailsBottomSheet(): EditDetailsBottomSheet
}
