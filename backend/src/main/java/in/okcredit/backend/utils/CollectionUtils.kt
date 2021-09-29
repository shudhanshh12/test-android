package `in`.okcredit.backend.utils

object Utils {

    @JvmStatic
    fun sanitiseFilePathToURL(path: String): String {
        var res = path
        if (path.contains("https:/s3")) {
            res = path.replace("https:/s3", "https://s3")
        }
        return res
    }
}
