package tech.okcredit.android.auth.usecases

import dagger.Lazy
import tech.okcredit.android.auth.AuthServiceImpl
import javax.inject.Inject

class InvalidateAccessToken @Inject constructor(
    private val authServiceImpl: Lazy<AuthServiceImpl>,
) {
    suspend fun execute() = authServiceImpl.get().invalidateAccessToken()
}
