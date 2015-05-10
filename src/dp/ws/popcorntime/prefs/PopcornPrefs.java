package dp.ws.popcorntime.prefs;

import android.content.Context;

public class PopcornPrefs extends BasePrefs {

	public static final String IS_SHORTCUT_CREATED = "is-shortcut-created";
	public static final String APP_LOCALE = "app-locale";
	public static final String UPDATE_APK_PATH = "update-apk-path";
	public static final String LAST_TORRENT = "last-torrent";

	protected PopcornPrefs(Context context) {
		preferences = context.getSharedPreferences("PopcornPreferences", Context.MODE_PRIVATE);
	}

}