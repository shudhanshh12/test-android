package `in`.okcredit.collection_ui.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CollectionUtilsTest {

    @Test
    fun `return false for isUpiEmpty`() {
        val upi = "8882946897@ybl"

        assertThat(CollectionUtils.isUpiEmpty(upi)).isFalse()
    }

    @Test
    fun `return true for isUpiEmpty`() {
        val upi = ""

        assertThat(CollectionUtils.isUpiEmpty(upi)).isTrue()
    }

    @Test
    fun `return true for ifsc is valid`() {
        val ifsc = "ICIC0000541"

        assertThat(CollectionUtils.isValidIFSC(ifsc)).isTrue()
    }

    @Test
    fun `return false for isValidIFSC when has 5 characters in start`() {
        val ifsc = "ICICI0000541"

        assertThat(CollectionUtils.isValidIFSC(ifsc)).isFalse()
    }

    @Test
    fun `return false for isValidIFSC when after characters first character is non zero`() {
        val ifsc = "ICIC70000541"

        assertThat(CollectionUtils.isValidIFSC(ifsc)).isFalse()
    }

    @Test
    fun `return false for isValidIFSC when ifsc length is 4 and all are string chars`() {
        val ifsc = "ICIC"

        assertThat(CollectionUtils.isValidIFSC(ifsc)).isTrue()
    }

    @Test
    fun `return true for valid bank details`() {
        val accountNumber = "054101507917"
        val ifsc = "ICICI0000541"

        assertThat(CollectionUtils.isInvalidBankDetails(accountNumber, ifsc, true)).isTrue()
    }

    @Test
    fun `return false for invalid account Number in bank details`() {
        val accountNumber = "05410150"
        val ifsc = "ICICI0000541"

        assertThat(CollectionUtils.isInvalidBankDetails(accountNumber, ifsc, true)).isTrue()
    }

    @Test
    fun `return false for empty ifsc in bank details`() {
        val accountNumber = "054101507917"
        val ifsc = ""

        assertThat(CollectionUtils.isInvalidBankDetails(accountNumber, ifsc, true)).isTrue()
    }

    @Test
    fun `return false for invalid ifsc in bank details`() {
        val accountNumber = "054101507917"
        val ifsc = "ICICI00005"

        assertThat(CollectionUtils.isInvalidBankDetails(accountNumber, ifsc, true)).isTrue()
    }
}
