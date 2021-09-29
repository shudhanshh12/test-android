package tech.okcredit.android.auth

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@Retention(AnnotationRetention.RUNTIME)
annotation class AssertOffline

class OfflineRule(private val offlineCheck: () -> Unit) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            base.evaluate()
            val isOfflineTest: Boolean = description.getAnnotation(AssertOffline::class.java) != null
            if (isOfflineTest) {
                try {
                    offlineCheck()
                } catch (e: Throwable) {
                    throw RuntimeException("violates offline assertion", e)
                }
            }
        }
    }
}
