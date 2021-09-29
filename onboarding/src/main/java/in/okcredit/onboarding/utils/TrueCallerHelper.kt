package `in`.okcredit.onboarding.utils

import android.content.Context
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.extensions.doesPackageExist
import javax.inject.Inject

class TrueCallerHelper @Inject constructor(private val context: Lazy<Context>) {

    companion object {
        private const val PACKAGE_NAME = "com.truecaller"
    }

    fun isTrueCallerInstalled() = Single.fromCallable {
        context.get().doesPackageExist(PACKAGE_NAME)
    }
}
