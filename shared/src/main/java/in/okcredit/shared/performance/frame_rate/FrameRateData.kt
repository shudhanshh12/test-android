package `in`.okcredit.shared.performance.frame_rate

import org.jetbrains.annotations.NonNls

class FrameRateData(
    private val total: Long = 0,
    private val slow: Long = 0,
    private val laggy: Long = 0,
    private val frozen: Long = 0
) {

    @NonNls
    override fun toString(): String {
        return """
            Total Frames : $total,
            Slow Frames  : $slow (${this.getSlowFrameRate()}%),
            Frozen Frames: $frozen ( ${this.getFrozenFrameRate()}%)
        """.trimIndent()
    }

    fun getFrozenFrameRate() = frozen.toRate()

    fun getSlowFrameRate() = slow.toRate()

    fun getLaggyFrameRate() = laggy.toRate()

    private fun Long.toRate() = if (total == 0L) 0 else (this * 100 / total)
}
