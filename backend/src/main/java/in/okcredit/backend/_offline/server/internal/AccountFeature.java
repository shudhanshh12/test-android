package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class AccountFeature {

    @SerializedName("account_id")
    private String accountId;

    @SerializedName("buyer_txn_alert")
    private boolean buyerTxnAlert;

    public AccountFeature(String accountId, boolean buyerTxnAlert) {
        this.accountId = accountId;
        this.buyerTxnAlert = buyerTxnAlert;
    }

    public String getAccountId() {
        return accountId;
    }

    public boolean getBuyerTxnAlert() {
        return buyerTxnAlert;
    }
}
