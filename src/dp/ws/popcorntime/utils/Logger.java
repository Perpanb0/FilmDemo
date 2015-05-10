package dp.ws.popcorntime.utils;

import android.util.Log;
import dp.ws.popcorntime.BuildConfig;

public class Logger {

	private static final String TAG = "tag_pt";

	private Logger() {

	}

	public static void debug(String msg) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void debug(String msg, Throwable tr) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, msg, tr);
		}
	}

	public static void error(String msg) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, msg);
		}
	}

	public static void error(String msg, Throwable tr) {
		if (BuildConfig.DEBUG) {
			Log.e(TAG, msg, tr);
		}
	}
}