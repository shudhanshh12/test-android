package `in`.okcredit

import `in`.okcredit.di.AppComponent

/**
 * Provides the core Dagger DI Component.
 *
 * The core module needs an application context as DI root. Therefor, the application classes of the apps using this module
 * should implement [AppComponentProvider].
 */
interface AppComponentProvider {
    /**
     * Returns the CoreComponent / DI root.
     */
    fun provideAppComponent(): AppComponent
}
