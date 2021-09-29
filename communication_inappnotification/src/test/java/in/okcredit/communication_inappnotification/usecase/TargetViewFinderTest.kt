package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.TargetIdType
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.model.EducationSheet
import `in`.okcredit.communication_inappnotification.usecase.TargetViewFinder.Companion.ID_DEF_TYPE
import android.content.res.Resources
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.coroutines.DispatcherProvider
import java.lang.ref.WeakReference

class TargetViewFinderTest {

    private val dispatcherProvider: DispatcherProvider = mock()
    private val tracker: InAppNotificationTracker = mock()
    private val targetViewFinder = TargetViewFinder({ dispatcherProvider }, { tracker })
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        whenever(dispatcherProvider.main()).thenReturn(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        mainThreadSurrogate.close()
    }

    @Test
    fun `given unsupported notification kind should track exception`() {
        runBlocking {
            // given
            val kind = "education_sheet"
            val notification = mock<EducationSheet>().apply {
                whenever(this.kind).thenReturn(kind)
                whenever(this.getTypeForAnalyticsTracking()).thenReturn(kind)
            }

            // when
            targetViewFinder.execute(mock(), mock(), notification)

            // then
            val exceptionCaptor = argumentCaptor<Exception>()
            verify(tracker).trackException(exceptionCaptor.capture())
            assertThat(exceptionCaptor.firstValue).isInstanceOf(IllegalArgumentException::class.java)
            assertThat(exceptionCaptor.firstValue.message).isEqualTo("Unsupported notification type: $kind")
        }
    }

    @Test
    fun `given tooltip notification with target id type CONTENT_DESCRIPTION with given target index should return expected view`() {
        runBlocking {
            // given
            val targetIdType = TargetIdType.CONTENT_DESCRIPTION
            val targetId = "target-id"
            val targetIndex = 2
            val notification = mock<Tooltip>().apply {
                whenever(this.targetIdType).thenReturn(targetIdType)
                whenever(this.targetId).thenReturn(targetId)
                whenever(this.targetIndex).thenReturn(targetIndex)
            }
            val weakScreen = mock<WeakReference<FragmentActivity>>()
            val weakView = mock<View>()
            val view1 = mock<View>()
            val view2 = mock<View>()
            val view3 = mock<View>()
            val viewList = listOf(view1, view2, view3)
            val captor = argumentCaptor<ArrayList<View>>()
            whenever(
                weakView.findViewsWithText(
                    captor.capture(),
                    eq(targetId),
                    eq(View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
                )
            ).thenAnswer { captor.firstValue.addAll(viewList) }

            // when
            val view = targetViewFinder.execute(weakScreen, WeakReference(weakView), notification)

            // then
            assertThat(view.get() == viewList[targetIndex]).isTrue()
            verify(weakView).findViewsWithText(any(), eq(targetId), eq(View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION))
        }
    }

    @Test
    fun `given tooltip notification with target id type FIND_VIEWS_WITH_TEXT with given target index should return expected view`() {
        runBlocking {
            // given
            val targetIdType = TargetIdType.TEXT
            val targetId = "target-id"
            val targetIndex = 1
            val notification = mock<Tooltip>().apply {
                whenever(this.targetIdType).thenReturn(targetIdType)
                whenever(this.targetId).thenReturn(targetId)
                whenever(this.targetIndex).thenReturn(targetIndex)
            }
            val weakScreen = mock<WeakReference<FragmentActivity>>()
            val view = mock<View>()
            val targetView1 = mock<View>()
            val targetView2 = mock<View>()
            val targetView3 = mock<View>()
            val viewList = listOf(targetView1, targetView2, targetView3)
            val captor = argumentCaptor<ArrayList<View>>()
            whenever(
                view.findViewsWithText(
                    captor.capture(),
                    eq(targetId),
                    eq(View.FIND_VIEWS_WITH_TEXT)
                )
            ).thenAnswer { captor.firstValue.addAll(viewList) }

            // when
            val targetView = targetViewFinder.execute(weakScreen, WeakReference(view), notification)

            // then
            assertThat(targetView.get() == viewList[targetIndex]).isTrue()
            verify(view).findViewsWithText(any(), eq(targetId), eq(View.FIND_VIEWS_WITH_TEXT))
        }
    }

    @Test
    fun `given tooltip notification with target id type ID should return expected view`() {
        runBlocking {
            // given
            val targetIdType = TargetIdType.ID
            val targetId = "target-id"
            val notification = mock<TapTarget>().apply {
                whenever(this.targetIdType).thenReturn(targetIdType)
                whenever(this.targetId).thenReturn(targetId)
            }
            val view = mock<View>()
            val resources = mock<Resources>()
            whenever(view.resources).thenReturn(resources)
            val identifier = 0
            whenever(resources.getIdentifier(eq(targetId), eq(ID_DEF_TYPE), any()))
                .thenReturn(identifier)
            val targetView = mock<View>()
            whenever(view.findViewById<View>(identifier)).thenReturn(targetView)

            // when
            val weakTargetView = targetViewFinder.execute(mock(), WeakReference(view), notification)

            // then
            assertThat(weakTargetView.get() == targetView).isTrue()
            verify(view).findViewById<View>(identifier)
        }
    }

    @Test
    fun `given tooltip notification with target id type TAG should return expected view`() {
        runBlocking {
            // given
            val targetIdType = TargetIdType.TAG
            val targetId = "target-id"
            val notification = mock<Tooltip>().apply {
                whenever(this.targetIdType).thenReturn(targetIdType)
                whenever(this.targetId).thenReturn(targetId)
            }
            val view = mock<View>()
            val targetView = mock<View>()
            whenever(view.findViewWithTag<View>(targetId)).thenReturn(targetView)

            // when
            val weakTargetView = targetViewFinder.execute(mock(), WeakReference(view), notification)

            // then
            assertThat(weakTargetView.get() == targetView).isTrue()
            verify(view).findViewWithTag<View>(targetId)
        }
    }
}
