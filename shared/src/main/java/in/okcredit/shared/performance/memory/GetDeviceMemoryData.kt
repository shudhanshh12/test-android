package `in`.okcredit.shared.performance.memory

import android.app.ActivityManager
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.Lazy
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import java.io.BufferedReader
import java.io.FileReader
import java.text.DecimalFormat
import javax.inject.Inject

class GetDeviceMemoryData @Inject constructor(
    private val context: Lazy<Context>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
) {

    fun measureTimeDiffInMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block.invoke()
        return System.currentTimeMillis() - start
    }

    suspend fun execute(): DeviceMemoryResponse {
        return withContext(dispatcherProvider.get().io()) {
            val heapMemoryData = getHeapMemoryData()
            val ramMemoryData = getRAMData()

            DeviceMemoryResponse(
                heapMemoryResponse = heapMemoryData,
                ramMemoryData = ramMemoryData
            )
        }
    }

    private fun getRAMData(): RAMResponse {
        val reader = BufferedReader(FileReader("/proc/meminfo"))
        var s = reader.readLine()

        var memTotal = 0L
        var memFree = 0L
        var memCached = 0L
        var memAvailable = 0L

        while (s != null) {
            when {
                s.startsWith("MemTotal:") -> {
                    memTotal = getValueFromMemInfoLine(s)
                }
                s.startsWith("MemFree:") -> {
                    memFree = getValueFromMemInfoLine(s)
                }
                s.startsWith("Cached:") -> {
                    memCached = getValueFromMemInfoLine(s)
                }
                s.startsWith("MemAvailable:") -> {
                    memAvailable = getValueFromMemInfoLine(s)
                }
            }
            s = reader.readLine()
        }

        // On Some Lower Version of android MemAvailable is missing. Assuming available is cached + free on those devices
        if (memAvailable == 0L) {
            memAvailable = memFree + memCached
        }

        val usedMemory = memTotal - memAvailable
        val usedMemoryPercentage = (usedMemory * 100) / memTotal
        val cacheMemoryPercentage = (memCached * 100) / memTotal
        val freeMemoryPercentage = (memFree * 100) / memTotal

        val am = context.get().getSystemService(SplitCompatApplication.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val isLowMemory = mi.lowMemory
        val memoryThreshold = convertByteToMB(mi.threshold).toLong()
        val thresholdMemoryPercentage = (memoryThreshold * 100) / memTotal

        return RAMResponse(
            totalMemory = memTotal,
            freeMemory = memFree,
            cachedMemory = memCached,
            availableMemory = memAvailable,
            usedMemory = usedMemory,
            usedMemoryPercentage = usedMemoryPercentage,
            cacheMemoryPercentage = cacheMemoryPercentage,
            freeMemoryPercentage = freeMemoryPercentage,
            thresholdMemoryPercentage = thresholdMemoryPercentage,
            isDeviceOnLowMemory = isLowMemory,
            memoryThreshold = memoryThreshold
        )
    }

    fun getHeapMemoryData(): HeapMemoryResponse {
        val maxHeapMemory = convertByteToMB(Runtime.getRuntime().maxMemory()).toLong()
        val totalAllocatedHeapMemory = convertByteToMB(Runtime.getRuntime().totalMemory()).toLong()
        val freeAllocatedHeapMemory = convertByteToMB(Runtime.getRuntime().freeMemory()).toLong()
        val usedHeapMemory = totalAllocatedHeapMemory - freeAllocatedHeapMemory
        val percentageOfHeapUsed = (usedHeapMemory * 100) / maxHeapMemory
        val percentageOfHeapAllocated = (totalAllocatedHeapMemory * 100) / maxHeapMemory

        return HeapMemoryResponse(
            maxHeapMemory = maxHeapMemory,
            totalAllocatedHeapMemory = totalAllocatedHeapMemory,
            freeAllocatedHeapMemory = freeAllocatedHeapMemory,
            usedHeapMemory = usedHeapMemory,
            percentageOfHeapUsed = percentageOfHeapUsed,
            percentageOfHeapAllocated = percentageOfHeapAllocated
        )
    }

    fun getValueFromMemInfoLine(line: String): Long {
        return convertKBtoMB(Integer.parseInt(line.split("[ ]+".toRegex(), 3)[1]).toLong()).toLong()
    }

    private fun convertKBtoMB(size: Long): Double {
        if (size == 0L) return 0.0
        val df = DecimalFormat("#.##")
        return df.format(size.toDouble() / 1024).toDouble()
    }

    private fun convertByteToMB(size: Long): Double {
        if (size == 0L) return 0.0
        val df = DecimalFormat("#.##")
        return df.format(size.toDouble() / (1024 * 1024)).toDouble()
    }
}

data class DeviceMemoryResponse(
    val heapMemoryResponse: HeapMemoryResponse,
    val ramMemoryData: RAMResponse,
)

data class HeapMemoryResponse(
    val maxHeapMemory: Long, // Total MB of heap app allowed to use.
    val totalAllocatedHeapMemory: Long, // Total MB of heap allocated during app usage.
    val freeAllocatedHeapMemory: Long, // Total MB of free heap on allocated heap.
    val usedHeapMemory: Long, // Total MB of heap usage on the allocated heap
    val percentageOfHeapUsed: Long, // Percentage of heap usage from the maximum allowed heap
    val percentageOfHeapAllocated: Long, // Percentage of heap allocated from the maximum allowed heap.
)

data class RAMResponse(
    val totalMemory: Long, // Total Device RAM in MB.
    val freeMemory: Long, // Total Device RAM in MB.
    val cachedMemory: Long, // Total Cached RAM in MB.
    val availableMemory: Long, // Total Available RAM in MB. It Includes Both Cached and Free Memory.
    val usedMemory: Long, // Total Used RAM in MB. It Excludes Cached Memory.
    val memoryThreshold: Long, // Threshold Ram In MB at which Android consider low memory.
    val usedMemoryPercentage: Long, // Percentage of used RAM from Total RAM.
    val cacheMemoryPercentage: Long, // Percentage of Cached RAM from Total RAM.
    val freeMemoryPercentage: Long, // Percentage of Free RAM from Total RAM.
    val thresholdMemoryPercentage: Long, // Percentage of Threshold RAM from Total RAM.
    val isDeviceOnLowMemory: Boolean,
)
