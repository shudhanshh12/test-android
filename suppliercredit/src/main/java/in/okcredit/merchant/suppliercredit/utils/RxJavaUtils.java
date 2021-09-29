package in.okcredit.merchant.suppliercredit.utils;

import io.reactivex.Completable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RxJavaUtils {
    public static Completable runConcurrently(List<Completable> jobs, int loadFactor) {
        if (loadFactor < 2) {
            loadFactor = 2;
        }

        Map<Integer, Completable> workers = new HashMap<>();
        for (int workerIndex = 0; workerIndex < loadFactor; workerIndex++) {
            workers.put(workerIndex, Completable.complete());
        }

        int index = 0;
        for (Completable job : jobs) {
            int workerIndex = index % loadFactor;
            Completable worker = workers.get(workerIndex);
            workers.put(workerIndex, worker.andThen(job));
        }

        return Completable.merge(workers.values());
    }
}
