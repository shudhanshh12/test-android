package `in`.okcredit.merchant.device

import javax.inject.Inject

class DeviceHelper @Inject constructor() {

    fun getMappedAppsFlyerData(data: MutableMap<String, String>): HashMap<String, Any> {
        val properties = HashMap<String, Any>()
        data.forEach { (key, value) ->
            if (mappedKeys().containsKey(key)) {
                properties[mappedKeys().getValue(key)] = value
            }
        }
        return properties
    }

    private fun mappedKeys(): HashMap<String, String> {
        val keys = HashMap<String, String>()
        keys["media_source"] = "ad_partner_name"
        keys["adgroup_id"] = "adgroup_id"
        keys["adset"] = "ad_set"
        keys["campaign_id"] = "campaign_id"
        keys["advertising_id"] = "advertising_id"
        keys["adset_id"] = "adset_id"
        keys["adgroup"] = "adgroup"
        keys["af_siteid"] = "af_siteid"
        keys["af_sub1"] = "af_sub1"
        keys["af_sub2"] = "af_sub2"
        keys["af_sub3"] = "af_sub3"
        keys["af_sub4"] = "af_sub4"
        keys["af_sub5"] = "af_sub5"
        return keys
    }
}
