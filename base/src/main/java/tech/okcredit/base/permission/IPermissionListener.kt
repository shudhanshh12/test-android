package tech.okcredit.base.permission

// This gives status of the invoked permission
// whether use granted or denied the permission
interface IPermissionListener {

    // `onPermissionGrantedFirstTime` helps to check checking permission granted for first time.
    fun onPermissionGrantedFirstTime()

    fun onPermissionGranted()

    fun onPermissionDenied()

    @JvmDefault
    fun onPermissionPermanentlyDenied() {}
}
