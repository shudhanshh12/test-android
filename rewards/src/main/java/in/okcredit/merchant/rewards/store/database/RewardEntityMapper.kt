package `in`.okcredit.merchant.rewards.store.database

import `in`.okcredit.rewards.contract.RewardModel
import com.google.common.base.Converter
import com.google.gson.reflect.TypeToken
import tech.okcredit.android.base.json.GsonUtils

object RewardEntityMapper {

    var mapper = object : Converter<RewardModel, Rewards>() {
        override fun doForward(model: RewardModel): Rewards {
            return Rewards(
                model.id,
                model.create_time,
                model.update_time,
                model.status,
                model.reward_type,
                model.amount,
                model.featureName,
                model.featureTitle,
                model.description,
                model.deepLink,
                model.icon,
                GsonUtils.gson().toJson(model.labels),
                model.createdBy,
            )
        }

        override fun doBackward(dbEntity: Rewards): RewardModel {
            return RewardModel(
                id = dbEntity.id,
                create_time = dbEntity.createTime,
                update_time = dbEntity.updateTime,
                status = dbEntity.status,
                reward_type = dbEntity.rewardType,
                amount = dbEntity.amount,
                featureName = dbEntity.featureName ?: "",
                featureTitle = dbEntity.featureTitle ?: "",
                description = dbEntity.description ?: "",
                deepLink = dbEntity.deepLink ?: "",
                icon = dbEntity.icon ?: "",
                labels = GsonUtils.gson().fromJson(
                    dbEntity.labels, object : TypeToken<HashMap<String, String>>() {}.type
                ),
                createdBy = dbEntity.createdBy,
            )
        }
    }
}
