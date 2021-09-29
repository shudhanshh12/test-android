package merchant.android.okstream.contract

import android.content.Context

interface OkStreamService {
    fun connect(context: Context)
    fun disconnect(context: Context)
    fun publishAddCustomerTransaction(data: Any, receiver: String)
    fun publishAddCustomerSuccess(data: Any, receiver: String)
}
