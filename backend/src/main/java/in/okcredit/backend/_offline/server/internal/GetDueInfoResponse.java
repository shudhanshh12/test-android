package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class GetDueInfoResponse {

    @SerializedName("due_info")
    public DueInfo dueInfo;

    public GetDueInfoResponse(DueInfo dueInfo) {
        this.dueInfo = dueInfo;
    }

    public DueInfo getDueInfo() {
        return dueInfo;
    }

    public void setDueInfo(DueInfo dueInfo) {
        this.dueInfo = dueInfo;
    }
}
