package dp.ws.popcorntime;

import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Intent;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import dp.ws.popcorntime.prefs.PopcornPrefs;
import dp.ws.popcorntime.prefs.Prefs;
import dp.ws.popcorntime.prefs.SettingsPrefs;
import dp.ws.popcorntime.utils.LanguageUtil;
import dp.ws.popcorntime.utils.Logger;
import dp.ws.popcorntime.utils.StorageHelper;

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.SILENT, mailTo = "support.popcorn@yandex.ru")
public class PopcornApplication extends Application {

	private Locale mLocale;

	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(PopcornApplication.this);
		ImageLoader.getInstance().init(
				new ImageLoaderConfiguration.Builder(PopcornApplication.this)
						.build());
		Prefs.init(PopcornApplication.this);
		StorageHelper.getInstance().init(PopcornApplication.this);
		LanguageUtil.init(getResources());

		initLocale();
		initSubtitleLanguage();
		addShortcut();
	}

	public Locale getAppLocale() {
		return mLocale;
	}

	public void changeLanguage(String locale) {
		if (mLocale.getLanguage().equals(locale)) {
			return;
		}

		mLocale = createLocale(locale);
		Logger.debug("Change locale: " + mLocale.toString());
		Prefs.getPopcorn().edit().putString(PopcornPrefs.APP_LOCALE, locale)
				.commit();
	}

	private Locale createLocale(String locale) {
		String[] args = locale.split("_");
		if (args.length == 2) {
			return new Locale(args[0], args[1]);
		}

		return new Locale(locale);
	}

	private void initLocale() {
		String locale = null;
		if (Prefs.getPopcorn().contains(PopcornPrefs.APP_LOCALE)) {
			locale = Prefs.getPopcorn().getString(PopcornPrefs.APP_LOCALE,
					LanguageUtil.DEFAULT_LOCALE);
		} else {
			locale = LanguageUtil.getInterfaceSupportedLocale();
			Prefs.getPopcorn().edit()
					.putString(PopcornPrefs.APP_LOCALE, locale).commit();
		}
		mLocale = createLocale(locale);
	}

	private void initSubtitleLanguage() {
		if (!Prefs.getSettngs().contains(SettingsPrefs.SUBTITLE_LANGUAGE)) {
			Prefs.getSettngs()
					.edit()
					.putString(SettingsPrefs.SUBTITLE_LANGUAGE,
							LanguageUtil.getDefaultSubtitleLanguage()).commit();
		}
	}

	private void addShortcut() {
		if (!Prefs.getPopcorn().getBoolean(PopcornPrefs.IS_SHORTCUT_CREATED,
				false)) {
			Intent shortcutIntent = new Intent(getApplicationContext(),
					dp.ws.popcorntime.ui.MainActivity.class);
			shortcutIntent.setAction(Intent.ACTION_MAIN);

			Intent addIntent = new Intent();
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources()
					.getString(R.string.app_name));
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					Intent.ShortcutIconResource.fromContext(
							getApplicationContext(), R.drawable.ic_launcher));
			addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

			getApplicationContext().sendBroadcast(addIntent);
			Prefs.getPopcorn().edit()
					.putBoolean(PopcornPrefs.IS_SHORTCUT_CREATED, true)
					.commit();
		}
	}
}