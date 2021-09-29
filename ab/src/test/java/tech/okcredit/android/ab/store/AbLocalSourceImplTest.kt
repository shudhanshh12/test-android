package tech.okcredit.android.ab.store

import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.Profile
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.*

class AbLocalSourceImplTest {
    private lateinit var store: AbLocalSourceImpl
    private val rxPref: AbPreferences = mock()
    private val businessId = "businessId"

    @Before
    fun setup() {
        store = AbLocalSourceImpl { rxPref }

        mockkObject(ThreadUtils)
        every { ThreadUtils.worker() } returns Schedulers.trampoline()

        mockkStatic(Schedulers::class)
        every { Schedulers.io() } returns Schedulers.trampoline()
    }

    @Test
    fun `getProfileSingle() should return profile from shared prefs`() {
        val profile: Profile = mock()
        whenever(rxPref.getObject(eq("profile"), any(), any<Profile>(), any())).thenReturn(flowOf(profile))

        val testObserver = store.getProfileSingle(businessId).test()

        testObserver.assertValue(profile)
        verify(rxPref, times(1)).getObject(eq("profile"), any(), any<Profile>(), any())
    }

    @Test
    fun `setProfile() returns no error`() {
        val profile: Profile = mock()
        whenever(rxPref.getObject<Profile>(eq("profile"), any(), any(), any())).thenReturn(flowOf(profile))

        val testObserver =
            store.setProfile(Profile(features = mapOf("collection" to false)), businessId)
                .andThen(store.getProfile(businessId))
                .test()

        testObserver.assertNoErrors()
        testObserver.dispose()
    }

    @Test
    fun `startedExperiments() should return list of experiments running`() {
        whenever(rxPref.getString(eq("started_activation_experiments"), any(), anyOrNull()))
            .thenReturn(flowOf("experiment1,experiment2,experiment3"))

        val testObserver = store.startedExperiments(businessId).test()

        testObserver.assertValue(listOf("experiment1", "experiment2", "experiment3"))
        testObserver.dispose()
    }

    @Test
    fun `startedExperiments() should return empty list when preference is empty`() {
        whenever(rxPref.getString(eq("started_activation_experiments"), any(), anyOrNull()))
            .thenReturn(flowOf(""))

        val testObserver = store.startedExperiments(businessId).test()

        testObserver.assertValue(Collections.singletonList(""))
        testObserver.dispose()
    }
}
