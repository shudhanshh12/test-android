package tech.okcredit.home.usecase.home

import android.view.ViewStub
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert
import org.junit.Test
import tech.okcredit.home.R

class ViewStubTest {
    private val viewStub: ViewStub = mock()

    @Test
    fun testDraw() {
        // if the function draw() does not throw any exception,
        // we think it is right, because it's an empty method.
        viewStub.draw(null)
    }

    @Test
    fun testAccessLayoutResource() {
        whenever(viewStub.layoutResource).thenReturn(R.layout.viewstub_layout)
        Assert.assertEquals(R.layout.viewstub_layout, viewStub.getLayoutResource())
    }
}
