package in.okcredit.backend._offline.usecase;

import in.okcredit.backend._offline.server.BackendRemoteSource;
import in.okcredit.backend.contract.CheckMobileStatus;
import io.reactivex.Single;
import javax.inject.Inject;

// Internet
public final class CheckMobileStatusImpl implements CheckMobileStatus {
    private BackendRemoteSource server;

    @Inject
    public CheckMobileStatusImpl(BackendRemoteSource server) {
        this.server = server;
    }

    public Single<Boolean> execute(String mobile) {
        return server.checkMobileStatus(mobile);
    }
}
