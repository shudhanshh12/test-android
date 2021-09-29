package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public final class TxnFile {
    @SerializedName("id")
    public final String id;

    @SerializedName("merchant_id")
    public final String merchant_id;

    @SerializedName("status")
    public final int status;

    @SerializedName("encryption_key")
    public final String encryption_key;

    @SerializedName("file")
    public final String file;

    public TxnFile(String id, String merchant_id, int status, String encryption_key, String file) {
        this.id = id;
        this.merchant_id = merchant_id;
        this.status = status;
        this.encryption_key = encryption_key;
        this.file = file;
    }

    // status 0 is in-progress and 1 is ready to download.
    public boolean isFileReady() {
        return status == 1;
    }
}
