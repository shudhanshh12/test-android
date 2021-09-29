package merchant.okcredit.gamification.ipl.game.data.server.model.response

enum class MysteryPrizeStatus constructor(val status: Int) {

    UNCLAIMED(0),
    CLAIMED(1);
}

enum class MysteryRewardSubType constructor(val status: String) {

    WELCOME_REWARD("1");
}
