package `in`.okcredit.supplier.statement

import `in`.okcredit.supplier.databinding.SupplierAccountStamentActivityBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class SupplierAccountStatementActivity : OkcActivity() {

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, SupplierAccountStatementActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val binding: SupplierAccountStamentActivityBinding by viewLifecycleScoped(
        SupplierAccountStamentActivityBinding::inflate
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
