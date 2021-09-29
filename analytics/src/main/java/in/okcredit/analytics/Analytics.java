package in.okcredit.analytics;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Set;

public final class Analytics {
    private static Set<AnalyticsHelper> analyticsProviders = Collections.emptySet();

    private Analytics() {
    }

    public static void setup(Set<AnalyticsHelper> providers) {
        if (providers != null) {
            analyticsProviders = Collections.unmodifiableSet(providers);
        }
    }

    public static void setUserProperty(String key, String value) {
        for (AnalyticsHelper analyticsProvider : analyticsProviders) {
            analyticsProvider.setUserProperty(key, value);
        }
    }

    public static void track(@NonNull String name, EventProperties properties) {
        for (AnalyticsHelper analyticsProvider : analyticsProviders) {
            analyticsProvider.track(name, properties);
        }
    }

    public static void track(@NonNull String name) {
        track(name, null);
    }
}
