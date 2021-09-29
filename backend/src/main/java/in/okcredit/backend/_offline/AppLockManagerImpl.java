package in.okcredit.backend._offline;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import in.okcredit.backend.contract.AppLockManager;
import tech.okcredit.android.base.preferences.DefaultPreferences;

import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.blockingGetBoolean;
import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.blockingGetString;
import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.blockingRemove;
import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.blockingSet;
import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.getIndividualScope;
import static tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_APP_LOCK_PATTERN;
import static tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_APP_LOCK_STATUS;

public class AppLockManagerImpl implements AppLockManager {

    private boolean isLocked;
    private final DefaultPreferences sharedPreferences;

    @Inject
    public AppLockManagerImpl(DefaultPreferences preferences) {
        isLocked = true;
        sharedPreferences = preferences;
    }

    @Override
    public boolean isAppLockAuthReqd() {
        synchronized (AppLockManagerImpl.class) {
            return isAppLockActive() && isLocked;
        }
    }

    @Override
    public boolean isAppLockActive() {
        return blockingGetBoolean(
                sharedPreferences,
                PREF_INDIVIDUAL_APP_LOCK_STATUS,
                getIndividualScope(),
                false
        );
    }

    @Override
    public boolean authenticatePattern(String pattern) {
        synchronized (AppLockManagerImpl.class) {
            String oldPattern = blockingGetString(
                    sharedPreferences,
                    PREF_INDIVIDUAL_APP_LOCK_PATTERN,
                    getIndividualScope(),
                    null
            );

            if (oldPattern != null && oldPattern.equals(pattern)) {
                isLocked = false;
                return true;
            } else {
                isLocked = true;
                return false;
            }
        }
    }

    @Override
    public void enableAppLock(String pattern) {
        synchronized (AppLockManagerImpl.class) {
            blockingSet(sharedPreferences, PREF_INDIVIDUAL_APP_LOCK_STATUS, true, getIndividualScope());
            blockingSet(sharedPreferences, PREF_INDIVIDUAL_APP_LOCK_PATTERN, pattern, getIndividualScope());

            isLocked = false;
        }
    }

    @Override
    public boolean disableAppLock(@NotNull String oldPatternAttempt) {
        synchronized (AppLockManagerImpl.class) {
            String oldPattern = blockingGetString(
                    sharedPreferences,
                    PREF_INDIVIDUAL_APP_LOCK_PATTERN,
                    getIndividualScope(),
                    null
            );
            if (oldPattern.equals(oldPatternAttempt)) {
                blockingRemove(sharedPreferences, PREF_INDIVIDUAL_APP_LOCK_STATUS, getIndividualScope());
                blockingRemove(sharedPreferences, PREF_INDIVIDUAL_APP_LOCK_PATTERN, getIndividualScope());

                isLocked = false;

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void clearAppLockData() {
        synchronized (AppLockManagerImpl.class) {
            blockingRemove(sharedPreferences, PREF_INDIVIDUAL_APP_LOCK_STATUS, getIndividualScope());
            blockingRemove(sharedPreferences, PREF_INDIVIDUAL_APP_LOCK_PATTERN, getIndividualScope());
        }
    }

    @Override
    public void setLocked(boolean isLocked) {
        synchronized (AppLockManagerImpl.class) {
            this.isLocked = isLocked;
        }
    }
}
