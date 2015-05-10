package dp.ws.popcorntime.prefs;

import android.content.Context;

public class SettingsPrefs extends BasePrefs {

	public static final String ROOT_FOLDER_PATH = "chache-folder-path";
	public static final String HARDWARE_ACCELERATION = "hardware-acceleration";
	public static final String IS_PROXY_ENABLE = "is-proxy-enable";
	public static final String CLEAR_ON_EXIT = "clear-on-exit";

	public static final String SUBTITLE_LANGUAGE = "subtitle-language";
	public static final String SUBTITLE_FONT_SIZE = "subtitle-font-size";
	public static final String SUBTITLE_FONT_COLOR = "subtitle-font-color";

	public static final String START_PAGE = "start-page";

	protected SettingsPrefs(Context context) {
		preferences = context.getSharedPreferences("PopcornPreferences", Context.MODE_PRIVATE);
	}
}