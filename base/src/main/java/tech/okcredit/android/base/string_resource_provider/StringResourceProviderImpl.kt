package tech.okcredit.android.base.string_resource_provider

import android.content.Context
import androidx.annotation.StringRes
import dagger.Lazy
import javax.inject.Inject

class StringResourceProviderImpl @Inject constructor(
    private val context: Lazy<Context>,
) : StringResourceProvider {

    override fun getByResourceId(@StringRes resId: Int): String {
        return context.get().getString(resId)
    }

    override fun getByResourceId(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return context.get().getString(resId, formatArgs)
    }
}

interface StringResourceProvider {
    fun getByResourceId(@StringRes resId: Int): String

    fun getByResourceId(@StringRes resId: Int, vararg formatArgs: Any?): String
}
