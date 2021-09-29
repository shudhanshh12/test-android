package tech.okcredit.base.network

import okhttp3.OkHttpClient
import javax.inject.Qualifier

// handles all network related stuff
interface NetworkManager {

    fun createHttpClient(): OkHttpClient
}

// qualifier for default okhttp client
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultOkHttpClient

// qualifier for long okhttp client
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class LongOkHttpClient

// used to tag any class, function that requires network access and might throw network related errors
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiresNetwork
