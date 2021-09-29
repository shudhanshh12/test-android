package `in`.okcredit.business_health_dashboard.datasource.remote.apiClient

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import com.google.common.base.Converter
import `in`.okcredit.business_health_dashboard.contract.model.Feedback as DbFeedback
import `in`.okcredit.business_health_dashboard.contract.model.TimeCadence as DbTimeCadence
import `in`.okcredit.business_health_dashboard.contract.model.Trend as DbTrend

object BusinessHealthEntityMapper {
    val BUSINESS_HEALTH_ENTITY_CONVERTER: Converter<BusinessHealthDashboardModelDto, BusinessHealthDashboardModel> =
        object : Converter<BusinessHealthDashboardModelDto, BusinessHealthDashboardModel>() {

            override fun doForward(businessHealthDashboardModelDto: BusinessHealthDashboardModelDto): BusinessHealthDashboardModel {
                return BusinessHealthDashboardModel(
                    lastUpdatedAtText = businessHealthDashboardModelDto.dashboardData.lastUpdatedAt,
                    timeCadenceList = businessHealthDashboardModelDto.dashboardData.timeCadenceList.map { timeCadence ->
                        requireNotNull(TIME_CADENCE_ENTITY_CONVERTER.convert(timeCadence))
                    },
                    selectedTimeCadence = requireNotNull(
                        TIME_CADENCE_ENTITY_CONVERTER.convert(
                            getTimeCadenceByString(
                                businessHealthDashboardModelDto.dashboardData.defaultTimeCadenceString,
                                businessHealthDashboardModelDto.dashboardData.timeCadenceList
                            )
                        )
                    )
                )
            }

            override fun doBackward(businessHealthDashboardModel: BusinessHealthDashboardModel): BusinessHealthDashboardModelDto {
                throw NotImplementedError("BUSINESS_HEALTH_ENTITY_CONVERTER doBackward not implemented")
            }
        }

    val TIME_CADENCE_ENTITY_CONVERTER: Converter<TimeCadence, DbTimeCadence> =
        object : Converter<TimeCadence, DbTimeCadence>() {

            override fun doForward(timeCadence: TimeCadence): DbTimeCadence {
                // TODO: For next versions, improve payload contract w/ backend
                return DbTimeCadence(
                    title = timeCadence.title,
                    totalBalanceMetric = `in`.okcredit.business_health_dashboard.contract.model.Metric(
                        timeCadence.metricsList[0].title,
                        timeCadence.metricsList[0].value
                    ),
                    paymentMetric = `in`.okcredit.business_health_dashboard.contract.model.Metric(
                        timeCadence.metricsList[1].title,
                        timeCadence.metricsList[1].value
                    ),
                    creditMetric = `in`.okcredit.business_health_dashboard.contract.model.Metric(
                        timeCadence.metricsList[2].title,
                        timeCadence.metricsList[2].value
                    ),
                    trendsSectionTitle = timeCadence.trends.title,
                    trendList = timeCadence.trends.trendList.map { trend ->
                        requireNotNull(TREND_ENTITY_CONVERTER.convert(trend))
                    }
                )
            }

            override fun doBackward(timeCadence: DbTimeCadence): TimeCadence {
                throw NotImplementedError("TIME_CADENCE_ENTITY_CONVERTER doBackward not implemented")
            }
        }

    val TREND_ENTITY_CONVERTER: Converter<Trend, DbTrend> =
        object : Converter<Trend, DbTrend>() {

            override fun doForward(trend: Trend): DbTrend {
                return DbTrend(
                    id = trend.id,
                    iconUrl = trend.iconUrl,
                    title = trend.title,
                    description = trend.description,
                    feedback = requireNotNull(FEEDBACK_ENTITY_CONVERTER.convert(trend.feedback))
                )
            }

            override fun doBackward(trend: DbTrend): Trend {
                throw NotImplementedError("TREND_ENTITY_CONVERTER doBackward not implemented")
            }
        }

    val FEEDBACK_ENTITY_CONVERTER: Converter<Feedback, DbFeedback> =
        object : Converter<Feedback, DbFeedback>() {

            override fun doForward(feedback: Feedback): DbFeedback {
                return DbFeedback(
                    isVisible = feedback.isVisible,
                    description = feedback.description,
                    response = feedback.response,
                )
            }

            override fun doBackward(feedback: DbFeedback): Feedback {
                throw NotImplementedError("FEEDBACK_ENTITY_CONVERTER doBackward not implemented")
            }
        }
}

fun getTimeCadenceByString(timeCadenceString: String, timeCadenceList: List<TimeCadence>): TimeCadence {
    return timeCadenceList.firstOrNull {
        it.title == timeCadenceString
    } ?: error("timeCadenceString $timeCadenceString not found in timeCadenceList")
}
