package tech.okcredit.userSupport.server

import androidx.annotation.Keep
import tech.okcredit.userSupport.model.Help

@Keep
data class HelpApiResponse(
    val variant: String,
    val sections: List<Help>
)
