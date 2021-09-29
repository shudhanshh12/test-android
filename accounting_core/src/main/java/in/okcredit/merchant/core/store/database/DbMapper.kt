package `in`.okcredit.merchant.core.store.database

import `in`.okcredit.merchant.core.Command.CommandType
import `in`.okcredit.merchant.core.Command.CommandType.*
import `in`.okcredit.merchant.core.common.Timestamp
import androidx.room.TypeConverter

class TimestampMapper {
    @TypeConverter
    fun longToTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    @TypeConverter
    fun timestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.epoch
    }
}

class CommandTypeMapper {
    @TypeConverter
    fun intToType(value: Int): CommandType {
        return when (value) {
            CREATE_TRANSACTION.code -> CREATE_TRANSACTION
            UPDATE_TRANSACTION_NOTE.code -> UPDATE_TRANSACTION_NOTE
            DELETE_TRANSACTION.code -> DELETE_TRANSACTION
            CREATE_TRANSACTION_IMAGE.code -> CREATE_TRANSACTION_IMAGE
            DELETE_TRANSACTION_IMAGE.code -> DELETE_TRANSACTION_IMAGE
            UPDATE_TRANSACTION_AMOUNT.code -> UPDATE_TRANSACTION_AMOUNT

            CREATE_CUSTOMER_DIRTY.code -> CREATE_CUSTOMER_DIRTY
            CREATE_CUSTOMER_IMMUTABLE.code -> CREATE_CUSTOMER_IMMUTABLE
            else -> UNKNOWN
        }
    }

    @TypeConverter
    fun typeToInt(type: CommandType): Int {
        return type.code
    }
}
