package org.persianbms.andromeda;

import android.util.Log;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class L {

    private static final String TAG = "pbms";

    public static void i(@NonNull String msg) {
        Log.i(TAG, msg);
    }

    public static void d(@NonNull String msg) {
        Log.d(TAG, msg);
    }

    public static void w(@NonNull String msg) {
        Log.w(TAG, msg);
    }

    public static void w(@NonNull String msg, @NonNull Throwable t) {
        Log.w(TAG, msg, t);
    }

    public static void e(@NonNull String msg, @NonNull Throwable t) {
        Log.e(TAG, msg, t);
    }

}
