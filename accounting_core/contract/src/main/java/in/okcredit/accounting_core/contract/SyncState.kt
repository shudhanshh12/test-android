package `in`.okcredit.accounting_core.contract

enum class SyncState {
    WAITING,
    GENERATE_FILE,
    DOWNLOADING,
    PROCESSING,
    FILE_DOWONLOAD_ERROR,
    FILE_COMPRESSION_ERROR,
    SYNC_GENERIC_ERROR,
    NETWORK_ERROR,
    COMPLETED
}
