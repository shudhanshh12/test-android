package tech.okcredit.help

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.help.help_main.usecase.GetHelp
import tech.okcredit.userSupport.SupportRepository

class HelpUsecaseMain {
    private val userSupport: SupportRepository = mock()
    private val getHelp = GetHelp(Lazy { userSupport })

    @Test
    fun `when scheduleSyncEverything`() {
        val businessId = "business-id"
        whenever(userSupport.scheduleSyncEverything("Langugae", businessId)).thenReturn(Completable.complete())
        val result = getHelp.scheduleSyncEverything("Langugae", businessId).subscribeOn(Schedulers.trampoline()).test()
        result.assertComplete()
    }
}
