package in.okcredit.shared._base_v2;

public interface MVP {
    interface View {
        void showError();
    }

    interface Presenter<V extends View> {
        void attachView(V v);

        void detachView();
    }
}
