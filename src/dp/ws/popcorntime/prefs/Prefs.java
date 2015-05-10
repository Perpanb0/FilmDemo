package dp.ws.popcorntime.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {

	private final static Prefs INSTANCE = new Prefs();

	private PopcornPrefs popcornPrefs;
	private SettingsPrefs settingsPrefs;

	private Prefs() {

	}

	public static void init(Context context) {
		INSTANCE.popcornPrefs = new PopcornPrefs(context);
		INSTANCE.settingsPrefs = new SettingsPrefs(context);
	}

	public static SharedPreferences getPopcorn() {
		return INSTANCE.popcornPrefs.getPrefs();
	}

	public static SharedPreferences getSettngs() {
		return INSTANCE.settingsPrefs.getPrefs();
	}
}