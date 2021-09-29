package tech.okcredit.account_chat_sdk

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.Completable
import tech.okcredit.account_chat_contract.RDB
import tech.okcredit.account_chat_contract.STRING_CONSTANTS

object FirebaseRDB {
    fun initRDB(merchantId: String): Completable {
        return Completable.fromAction {
            val database = Firebase.database
            val myConnectionsRef = database.getReference("users/$merchantId/status")
            val lastOnlineRef = database.getReference("/users/$merchantId/lastOnline")
            val connectedRef = database.getReference(RDB.CONNECTED_REF_PATH)
            connectedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected: Boolean = snapshot.value as Boolean? ?: false
                    if (connected) {
                        myConnectionsRef.setValue(STRING_CONSTANTS.ONLINE)
                        myConnectionsRef.onDisconnect().setValue(STRING_CONSTANTS.OFFLINE)
                        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
}
