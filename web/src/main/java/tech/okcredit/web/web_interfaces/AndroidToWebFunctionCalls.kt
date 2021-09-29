package tech.okcredit.web.web_interfaces

import android.webkit.WebView

fun WebView.sendOtpEvent(otp: String) {
    evaluateJavascript(
        "(function() { var otpEvent = new CustomEvent(\"otpEvent\", {\n" +
            "    \"detail\": {\"otp\":\"$otp\"}\n" +
            "  });\n" +
            "window.dispatchEvent(otpEvent); })();"
    ) { }
}

fun WebView.sendSmsPermissionEvent(granted: Boolean) {
    evaluateJavascript(
        "(function() { var smsEvent = new CustomEvent(\"smsEvent\", {\n" +
            "    \"detail\": {\"granted\":$granted}\n" +
            "  });\n" +
            "window.dispatchEvent(smsEvent); })();"
    ) { }
}
