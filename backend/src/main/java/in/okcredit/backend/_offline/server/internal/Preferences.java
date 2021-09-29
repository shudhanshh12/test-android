package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class Preferences {

    @SerializedName("txn_alert_enabled")
    private Boolean txnAlertEnabled;

    @SerializedName("comm_channel")
    private String commChannel;

    @SerializedName("add_transaction_restricted")
    private Boolean addTransactionRestricted;

    public Boolean getTxnAlertEnabled() {
        return txnAlertEnabled;
    }

    public void setTxnAlertEnabled(Boolean txnAlertEnabled) {
        this.txnAlertEnabled = txnAlertEnabled;
    }

    public String getCommChannel() {
        return commChannel;
    }

    public void setCommChannel(String commChannel) {
        this.commChannel = commChannel;
    }

    public Boolean getAddTransactionRestricted() {
        return addTransactionRestricted;
    }

    public void setAddTransactionRestricted(Boolean addTransactionRestricted) {
        this.addTransactionRestricted = addTransactionRestricted;
    }
}
