package dp.ws.popcorntime.prefs;

import android.content.SharedPreferences;

public abstract class BasePrefs {

	protected SharedPreferences preferences;

	public final SharedPreferences getPrefs() {
		return preferences;
	}
}