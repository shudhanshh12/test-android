package tech.okcredit.home.widgets.quick_add_card.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.frontend.contract.AccountingEventTracker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Lazy
import org.junit.Test

class QuickAddCardEventTrackerTest {
    private val ab: AnalyticsProvider = mock()
    private val accountingEventTracker: AccountingEventTracker = mock()
    private val quickAddCardEventTracker = QuickAddCardEventTracker(
        Lazy { ab },
        Lazy { accountingEventTracker }
    )

    @Test
    fun `should call track event with correct name when trackShareAppInteracted is called`() {
        quickAddCardEventTracker.quickAddCardInteracted("button", InteractionType.LONG_PRESS)

        verify(ab).trackObjectInteracted(
            "Quick Add Card",
            InteractionType.LONG_PRESS,
            mapOf("Item" to "button")
        )
    }

    @Test
    fun `should call track event with correct name when quickAddCardViewed is called`() {
        quickAddCardEventTracker.quickAddCardViewed()

        verify(ab).trackObjectViewed("Quick Add Card")
    }

    @Test
    fun `should call track event with correct name when tooltipViewed is called`() {
        quickAddCardEventTracker.tooltipViewed()

        verify(ab).trackObjectViewed("Quick Add Card Tooltip")
    }

    @Test
    fun `should call track event with correct name when quickAddCardError is called`() {
        quickAddCardEventTracker.quickAddCardError("Internal Server Error")

        verify(ab).trackObjectError("Quick Add Card", "Internal Server Error")
    }
}
