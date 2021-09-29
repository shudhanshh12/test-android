package in.okcredit.shared._base_v2;

public interface ViewOperation<V extends MVP.View> {
    void runFor(V view);
}
