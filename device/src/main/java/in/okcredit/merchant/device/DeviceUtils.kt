package `in`.okcredit.merchant.device

import `in`.okcredit.merchant.device.DeviceUtils.Companion.getDirSize
import `in`.okcredit.merchant.device.DeviceUtils.Companion.sizeInMB
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.base.crashlytics.RecordException
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

class DeviceUtils @Inject constructor(
    private val context: Lazy<Context>,
) {

    companion object {

        const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PACKAGE_NAME = "com.whatsapp.w4b"

        fun createDeviceId(): String {
            return UUID.randomUUID().toString()
        }

        fun fetchAdId(context: Context): Single<String> {
            return Single.fromCallable { AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: "" }
                .onErrorReturnItem("")
                .subscribeOn(Schedulers.io())
        }

        fun getVersionCode() = BuildConfig.VERSION_CODE.toInt()

        fun getApiLevel(): Int {
            return Build.VERSION.SDK_INT
        }

        fun getDirSize(dir: File): Long {
            var size: Long = 0
            dir.listFiles()?.let {
                for (file in it) {
                    if (file != null && file.isDirectory) {
                        size += getDirSize(file)
                    } else if (file != null && file.isFile) {
                        size += file.length()
                    }
                }
            }
            return size
        }

        fun sizeInMB(size: Long): Double {
            if (size == 0L) return 0.0
            val df = DecimalFormat("#.##")
            return df.format(size.toDouble() / (1024 * 1024)).toDouble()
        }
    }

    fun isWhatsAppInstalled() = isAppPackageInstalled(WHATSAPP_PACKAGE_NAME)

    fun isWhatsAppBusinessInstalled() = isAppPackageInstalled(WHATSAPP_BUSINESS_PACKAGE_NAME)

    private fun isAppPackageInstalled(packageName: String): Boolean {
        return try {
            context.get().packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e, "App not installed")
            false
        }
    }

    fun getRandomAccessMemory(): String {
        var totalRam = "UNKNOWN"
        try {
            val memoryInfo = ActivityManager.MemoryInfo()
            val activityManager = (context.get().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            activityManager.getMemoryInfo(memoryInfo)
            totalRam = ConvertToRAM(memoryInfo.totalMem)
        } catch (e: Exception) {
            Timber.i("Getting Ram info Raised Exception : ${e.message}")
            RecordException.recordException(e)
        }
        return totalRam
    }

    fun getDeviceSystemStorage(): String {
        var totalSystemMemory = "UNKNOWN"
        try {
            val systemStoragePath = StatFs(Environment.getRootDirectory().absolutePath)
            totalSystemMemory = convertToGigaByte(
                systemStoragePath.blockSizeLong.toDouble() *
                    systemStoragePath.blockCountLong.toDouble()
            )
        } catch (e: Exception) {
            Timber.d("Getting RomSystem info Raised Exception : ${e.message}")
            RecordException.recordException(e)
        }
        return totalSystemMemory
    }

    fun getDeviceInternalStorage(): String {
        var totalInternalStorage = "UNKNOWN"
        try {
            val internalStoragePath = StatFs(Environment.getExternalStorageDirectory().path)
            totalInternalStorage = convertToGigaByte(
                internalStoragePath.blockSizeLong.toDouble() * internalStoragePath.blockCountLong.toDouble()
            )
        } catch (e: Exception) {
            Timber.d("Getting RomInternal info Raised Exception : ${e.message}")
            RecordException.recordException(e)
        }
        return totalInternalStorage
    }

    fun getCPUClockSpeed(): String {
        var cpuFrequencyRange = "UNKNOWN"
        try {
            if (ContextCompat.checkSelfPermission(context.get(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                val readerMax =
                    RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r")
                val readerMin =
                    RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq", "r")
                val cpuMaxFrequency = convertToMHz(readerMax.readLine().toDouble())
                val cpuMinFrequency = convertToMHz(readerMin.readLine().toDouble())
                cpuFrequencyRange = "$cpuMinFrequency - $cpuMaxFrequency"
                readerMin.close()
                readerMax.close()
            }
        } catch (e: Exception) {
            Timber.d("Getting Cpu Frequency info Raised Exception : ${e.message}")
            RecordException.recordException(e)
        }
        return cpuFrequencyRange
    }

    fun getCpuCores(): Int {
        var cpuCount = 0
        try {
            cpuCount = Runtime.getRuntime().availableProcessors()
        } catch (e: Exception) {
            Timber.d("Getting cpu cores info Raised Exception : ${e.message}")
            RecordException.recordException(e)
        }
        return cpuCount
    }

    fun getNetworkConnectivityType(): String {
        val mTelephonyManager = context.get().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when (mTelephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN,
            -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_TD_SCDMA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP,
            -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "WI-FI"
            else -> "UNKNOWN"
        }
    }
}

fun convertToGigaByte(byte: Double): String {
    return (byte / 1024 / 1024 / 1024).toString()
}

fun ConvertToRAM(byte: Long): String {
    return (byte / 1024 / 1024).toString()
}

fun convertToMHz(Hz: Double): Double {
    return Hz / 1000
}

inline fun nougat(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        block()
    }
}

inline fun androidPie(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        block()
    }
}

inline fun oreo(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        block()
    }
}

inline fun marshmallow(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        block()
    }
}

inline fun belowNouget(block: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        block()
    }
}

inline fun lollipopMr1(block: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        block()
    }
}

class DeviceInfoUtils @Inject constructor(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(): Boolean {
        val packageName: String = context.packageName
        val pm: PowerManager? = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        return pm?.isIgnoringBatteryOptimizations(packageName) ?: false
    }

    fun isMarshMallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun areNotificationsEnabled() = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun getInternalCacheDirectorySizeInMB() = sizeInMB(getDirSize(context.cacheDir))

    fun getInternalFileDirectorySizeInMB() = sizeInMB(getDirSize(context.filesDir))

    fun getExternalFileDirectorySizeInMB(): Double? {
        context.getExternalFilesDir(null)?.let {
            return sizeInMB(getDirSize(it))
        }
        return null
    }
}
