package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;

public class Account {

    @SerializedName("id")
    private String id;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("update_time")
    private String updateTime;

    @SerializedName("creator_role")
    private Integer creatorRole;

    @SerializedName("summary")
    private Summary summary;

    @SerializedName("seller")
    private Seller seller;

    @SerializedName("buyer")
    private Buyer buyer;

    @SerializedName("state")
    private Integer state;

    @SerializedName("blocked_by")
    private Integer blockedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getCreatorRole() {
        return creatorRole;
    }

    public void setCreatorRole(Integer creatorRole) {
        this.creatorRole = creatorRole;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(Integer blockedBy) {
        this.blockedBy = blockedBy;
    }
}
