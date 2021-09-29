package `in`.okcredit.supplier.statement.di

import `in`.okcredit.supplier.statement.SupplierAccountStatementActivity
import `in`.okcredit.supplier.statement.SupplierAccountStatementFragment
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SupplierAccountStatementActivityModule {

    @Binds
    abstract fun activity(activity: SupplierAccountStatementActivity): AppCompatActivity

    @ContributesAndroidInjector(modules = [SupplierAccountStatementFragmentModule::class])
    abstract fun supplierAccountStatementFragment(): SupplierAccountStatementFragment
}
