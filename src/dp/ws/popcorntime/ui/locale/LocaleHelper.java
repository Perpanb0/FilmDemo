package dp.ws.popcorntime.ui.locale;

import java.util.Locale;

import android.content.res.Configuration;
import dp.ws.popcorntime.PopcornApplication;

public class LocaleHelper {

	private PopcornApplication mApplication;
	private LocaleListener mListener;
	private String lastLang;

	public LocaleHelper(PopcornApplication application,
			LocaleListener localeListener) {
		mApplication = application;
		mListener = localeListener;
		lastLang = mApplication.getAppLocale().getLanguage();
		updateLocale();
	}

	public void checkLanguage() {
		if (lastLang.equals(mApplication.getAppLocale().getLanguage())) {
			updateLocale();
		} else {
			lastLang = mApplication.getAppLocale().getLanguage();
			updateLocale();
			mListener.updateLocaleText();
		}
	}

	public void updateLocale() {
		Locale.setDefault(mApplication.getAppLocale());
		Configuration config = mApplication.getResources().getConfiguration();
		config.locale = mApplication.getAppLocale();
		mApplication.getResources().updateConfiguration(config,
				mApplication.getResources().getDisplayMetrics());
	}

}