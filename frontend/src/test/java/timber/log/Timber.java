package timber.log;

import org.jetbrains.annotations.NonNls;

public final class Timber {
    /** Log a verbose message with optional format args. */
    public static void v(@NonNls String message, Object... args) {}

    /** Log a verbose exception and a message with optional format args. */
    public static void v(Throwable t, @NonNls String message, Object... args) {}

    /** Log a verbose exception. */
    public static void v(Throwable t) {}

    /** Log an info message with optional format args. */
    public static void i(@NonNls String message, Object... args) {}

    /** Log an info exception and a message with optional format args. */
    public static void i(Throwable t, @NonNls String message, Object... args) {}

    /** Log an info exception. */
    public static void i(Throwable t) {}

    /** Log a debug message with optional format args. */
    public static void d(@NonNls String message, Object... args) {}

    /** Log a debug exception and a message with optional format args. */
    public static void d(Throwable t, @NonNls String message, Object... args) {}

    /** Log a debug exception. */
    public static void d(Throwable t) {}

    public static void e(@NonNls String message, Object... args) {}

    public static void e(Throwable t) {}
}
