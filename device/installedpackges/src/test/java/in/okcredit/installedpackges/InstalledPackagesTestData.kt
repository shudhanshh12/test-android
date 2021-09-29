package `in`.okcredit.installedpackges

import `in`.okcredit.installedpackges.server.PackageInfo
import `in`.okcredit.installedpackges.server.PackageReport
import `in`.okcredit.merchant.contract.Business
import org.joda.time.DateTime

object InstalledPackagesTestData {
    val BUSINESS_ID = "businessId"

    val BUSINESS = Business(
        BUSINESS_ID,
        "abc Store",
        "8888888888",
        "",
        "",
        0.0,
        0.0,
        "",
        "",
        "",
        DateTime.now(),
        null,
        false,
        null,
        null,
        false,
        null,
        null
    )

    val PackageInfoList = listOf(
        PackageInfo(packageName = "pkg1", packageId = "1"),
        PackageInfo(packageName = "pkg2", packageId = "2")
    )

    val PackageReportList = listOf(
        PackageReport(packageName = "pkg1", packageId = "1", isInstalled = false),
        PackageReport(packageName = "pkg2", packageId = "2", isInstalled = true)
    )
}
