package in.okcredit;

import android.util.Log;

import tech.okcredit.base.exceptions.ExceptionUtils;
import timber.log.Timber;

public class ProdLoggingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {

        if (priority == Log.ERROR) ExceptionUtils.Companion.log("Error Log:" + message);
    }
}
