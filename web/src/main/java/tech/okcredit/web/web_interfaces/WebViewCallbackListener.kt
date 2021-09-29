package tech.okcredit.web.web_interfaces

interface WebViewCallbackListener {
    fun backPress()
    fun pageBack()
    fun shareOnWhatsApp(msg: String, phone: String, url: String?)
    fun shareOnAnyApp(msg: String, imageUrl: String?)
    fun call(phone: String)
    fun makeToast(msg: String)
    fun debug(msg: String)
    fun navigate(deepLink: String)
    fun requestLocationPermission()
    fun getLocation(): String // Should call only after requestLocationPermission.
    fun requestSmsPermission()
    fun stopListeningSms()
}
