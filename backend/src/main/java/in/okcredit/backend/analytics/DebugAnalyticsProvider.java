package in.okcredit.backend.analytics;

import in.okcredit.analytics.AnalyticsHelper;
import in.okcredit.analytics.EventProperties;
import in.okcredit.analytics.helpers.AnalyticsNotificationHelper;
import timber.log.Timber;

public class DebugAnalyticsProvider implements AnalyticsHelper {

    private AnalyticsNotificationHelper analyticsNotificationHelper;

    public DebugAnalyticsProvider(AnalyticsNotificationHelper analyticsNotificationHelper) {
        this.analyticsNotificationHelper = analyticsNotificationHelper;
    }

    @Override
    public void setUserProperty(String key, String value) {}

    @Override
    public void track(String eventName, EventProperties props) {
        if (props != null) {
            analyticsNotificationHelper.addInNotification(
                    eventName, props.getProperties().toString());
            Timber.i("[Analytics] %s: %s", eventName, props.getProperties().toString());
        } else {
            analyticsNotificationHelper.addInNotification(eventName, "");
            Timber.i("[Analytics] %s", eventName);
        }
    }
}
