package dp.ws.popcorntime.utils;

import java.util.HashMap;
import java.util.Locale;

import dp.ws.popcorntime.R;

import android.content.res.Resources;

public class LanguageUtil {

	public static final String DEFAULT_LOCALE = "en";
	public static final String DEFAULT_SUBTITLE_LOCALE = "";
	public static final int DEFAULT_LOCALE_POSITION = 0;
	public static final int POSITION_WITHOUT_SUBTITLES = 0;

	private static String[] interfaceNative;
	private static String[] interfaceLocale;

	private static String[] subtitleName;
	private static String[] subtitleNative;

	private static HashMap<String, String> subtitleNativeByName;
	private static HashMap<String, String> subtitleNameByIso;

	public static void init(Resources resources) {
		interfaceNative = resources.getStringArray(R.array.interface_native);
		interfaceLocale = resources.getStringArray(R.array.interface_locale);

		subtitleName = resources.getStringArray(R.array.subtitle_name);
		subtitleNative = resources.getStringArray(R.array.subtitle_native);

		subtitleNativeByName = new HashMap<String, String>();
		int count = subtitleName.length <= subtitleNative.length ? subtitleName.length : subtitleNative.length;
		for (int i = 1; i < count; i++) {
			subtitleNativeByName.put(subtitleName[i], subtitleNative[i]);
		}

		String[] subtitle_iso = resources.getStringArray(R.array.subtitle_iso);
		subtitleNameByIso = new HashMap<String, String>();
		count = subtitle_iso.length <= subtitleName.length ? subtitle_iso.length : subtitleName.length;
		for (int i = 1; i < count; i++) {
			subtitleNameByIso.put(subtitle_iso[i], subtitleName[i]);
		}
	}

	public static String getInterfaceSupportedLocale() {
		String language = Locale.getDefault().getLanguage();
		String locale = Locale.getDefault().toString();
		for (String _locale : interfaceLocale) {
			if (_locale.equals(language) || _locale.equals(locale)) {
				return _locale;
			}
		}

		return DEFAULT_LOCALE;
	}

	public static String getDefaultSubtitleLanguage() {
		String language = Locale.getDefault().getLanguage();

		if (subtitleNameByIso.containsKey(language)) {
			return subtitleNameByIso.get(language);
		}

		return DEFAULT_SUBTITLE_LOCALE;
	}

	public static String[] getInterfaceNative() {
		return interfaceNative;
	}

	public static String[] getInterfaceLocale() {
		return interfaceLocale;
	}

	public static void setWithoutSubtitlesText(String text) {
		subtitleNative[POSITION_WITHOUT_SUBTITLES] = text;
	}

	public static String[] getSubtitlesName() {
		return subtitleName;
	}

	public static String[] getSubtitlesNative() {
		return subtitleNative;
	}

	public static String subtitleNameToNative(String name) {
		name = name.toLowerCase();
		if (subtitleNativeByName.containsKey(name)) {
			return subtitleNativeByName.get(name);
		}

		return name;
	}

	public static String subtitleIsoToName(String iso) {
		iso = iso.toLowerCase();
		if (subtitleNameByIso.containsKey(iso)) {
			return subtitleNameByIso.get(iso);
		}

		return iso;
	}

	public static String subtitleIsoToNative(String iso) {
		return subtitleNameToNative(subtitleIsoToName(iso));
	}

}