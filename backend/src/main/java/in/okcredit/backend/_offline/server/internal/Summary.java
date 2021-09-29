package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class Summary {

    @SerializedName("balance")
    private String balance;

    @SerializedName("txn_count")
    private String txnCount;

    @SerializedName("last_txn_time")
    private String lastTxnTime;

    @SerializedName("last_payment_time")
    private String lastPaymentTime;

    @SerializedName("last_settlement_time")
    private String lastSettlementTime;

    @SerializedName("update_time")
    private String updateTime;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getTxnCount() {
        return txnCount;
    }

    public void setTxnCount(String txnCount) {
        this.txnCount = txnCount;
    }

    public String getLastTxnTime() {
        return lastTxnTime;
    }

    public void setLastTxnTime(String lastTxnTime) {
        this.lastTxnTime = lastTxnTime;
    }

    public String getLastPaymentTime() {
        return lastPaymentTime;
    }

    public void setLastPaymentTime(String lastPaymentTime) {
        this.lastPaymentTime = lastPaymentTime;
    }

    public String getLastSettlementTime() {
        return lastSettlementTime;
    }

    public void setLastSettlementTime(String lastSettlementTime) {
        this.lastSettlementTime = lastSettlementTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
