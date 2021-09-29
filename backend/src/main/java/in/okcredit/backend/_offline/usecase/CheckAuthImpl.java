package in.okcredit.backend._offline.usecase;

import dagger.Lazy;
import in.okcredit.backend.contract.CheckAuth;
import io.reactivex.Observable;
import javax.inject.Inject;
import tech.okcredit.android.auth.AuthService;
import tech.okcredit.android.base.utils.ThreadUtils;

public final class CheckAuthImpl implements CheckAuth {

    private Lazy<AuthService> authService;

    @Inject
    public CheckAuthImpl(Lazy<AuthService> authService) {
        this.authService = authService;
    }

    public Observable<Boolean> execute() {
        return Observable.fromCallable(() -> authService.get().isAuthenticated())
                .subscribeOn(ThreadUtils.INSTANCE.database());
    }
}
