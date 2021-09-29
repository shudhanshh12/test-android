package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

class SyncContactRequest(@SerializedName("contacts") private val contactSyncs: List<ContactSync>)
