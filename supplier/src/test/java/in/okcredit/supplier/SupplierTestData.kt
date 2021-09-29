package `in`.okcredit.supplier

import `in`.okcredit.merchant.suppliercredit.Supplier
import org.joda.time.DateTime

object SupplierTestData {

    val SUPPLIER = Supplier(
        "supplier_id",
        true,
        false,
        DateTime(),
        1L,
        "supplier_name",
        "9999999999",
        "address",
        "profile_image",
        1,
        0L,
        DateTime(),
        DateTime(),
        true,
        "eng",
        true,
        DateTime(),
        true,
        Supplier.ACTIVE,
        false,
        false
    )
}
