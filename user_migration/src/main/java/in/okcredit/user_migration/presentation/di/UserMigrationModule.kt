package `in`.okcredit.user_migration.presentation.di

import `in`.okcredit.user_migration.presentation.UserMigrationNavigatorImpl
import `in`.okcredit.user_migration.presentation.UserMigrationRepositoryImpl
import `in`.okcredit.user_migration.presentation.ui.UserMigrationActivity
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import tech.okcredit.user_migration.contract.UserMigrationNavigator
import tech.okcredit.user_migration.contract.UserMigrationRepository

@Module
abstract class UserMigrationModule {

    @ContributesAndroidInjector(modules = [UserMigrationActivityModule::class])
    abstract fun userMigrationActivity(): UserMigrationActivity

    @Binds
    @Reusable
    abstract fun migrationApi(migrationApi: UserMigrationRepositoryImpl): UserMigrationRepository

    @Binds
    @Reusable
    abstract fun userMigrationNavigator(userMigrationNavigatorImpl: UserMigrationNavigatorImpl): UserMigrationNavigator
}
