package tech.okcredit.base.dagger.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelParam(val value: String)
