package in.okcredit.shared._base_v2;

import androidx.annotation.NonNull;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import tech.okcredit.android.auth.Unauthorized;
import tech.okcredit.base.network.utils.NetworkHelper;

public abstract class BasePresenter<V extends MVP.View> implements MVP.Presenter<V> {
    private V view;
    private CompositeDisposable tasks;

    protected Scheduler uiScheduler;

    public BasePresenter(Scheduler uiScheduler) {
        this.uiScheduler = uiScheduler;
        tasks = new CompositeDisposable();
    }

    @Override
    public void attachView(V v) {
        view = v;
        loadData();
    }

    @Override
    public void detachView() {
        tasks.clear();
        view = null;
    }

    protected void loadData() {
        // called after attach view
    }

    /**
     * ************************************************************** Helpers
     * **************************************************************
     */
    protected final void ifAttached(ViewOperation<V> viewOperation) {
        if (view != null) viewOperation.runFor(view);
    }

    protected final void addTask(@NonNull Disposable disposable) {
        if (tasks == null) tasks = new CompositeDisposable();
        tasks.add(disposable);
    }

    protected final boolean isInternetIssue(@NonNull Throwable throwable) {
        return NetworkHelper.INSTANCE.isNetworkError(throwable);
    }

    protected final boolean isAuthenticationIssue(@NonNull Throwable throwable) {
        return (throwable instanceof Unauthorized)
                || (throwable.getCause() instanceof Unauthorized);
    }
}
