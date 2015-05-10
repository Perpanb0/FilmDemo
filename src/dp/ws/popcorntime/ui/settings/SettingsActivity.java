package dp.ws.popcorntime.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import dp.ws.popcorntime.PopcornApplication;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.prefs.PopcornPrefs;
import dp.ws.popcorntime.prefs.Prefs;
import dp.ws.popcorntime.prefs.SettingsPrefs;
import dp.ws.popcorntime.subtitles.Subtitles;
import dp.ws.popcorntime.ui.FolderChooserActivity;
import dp.ws.popcorntime.ui.MainActivity;
import dp.ws.popcorntime.ui.base.PopcornBaseActivity;
import dp.ws.popcorntime.ui.settings.CheckerSettingsItem.CheckerItemListener;
import dp.ws.popcorntime.ui.settings.ChooserSettingsItem.ChooserItemListener;
import dp.ws.popcorntime.ui.settings.SettingsItem.SettingItemListener;
import dp.ws.popcorntime.utils.LanguageUtil;
import dp.ws.popcorntime.utils.StorageHelper;

public class SettingsActivity extends PopcornBaseActivity {

	private final int REQUEST_DIRECTORY = 3457;

	private final String APP_SITE = "http://popcorn-time.se/";
	private final String APP_FORUM = "http://forum.popcorn-time.se/";

	private SettingsAdapter adapter;
	private BaseSettingsItem cacheFolderItem;

	// view
	private TextView headerTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		initApdater();

		// Header
		getPopcornLogoView().setVisibility(View.GONE);
		View header = setPopcornHeaderView(R.layout.header_settings);
		header.findViewById(R.id.header_back).setOnClickListener(backListener);
		headerTitle = (TextView) header.findViewById(R.id.header_title);

		// Content
		SettingsView list = (SettingsView) setPopcornContentView(R.layout.activity_settings);
		list.setAdapter(adapter);

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		LanguageUtil
				.setWithoutSubtitlesText(getString(R.string.without_subtitle));
		headerTitle.setText(R.string.settings);
		adapter.notifyDataSetInvalidated();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (REQUEST_DIRECTORY == requestCode) {
				String path = data
						.getStringExtra(FolderChooserActivity.SELECTED_DIR);
				StorageHelper.getInstance().setRootDirectory(
						SettingsActivity.this, path);
				cacheFolderItem.update();
			}
		}
	}

	private void initApdater() {
		adapter = new SettingsAdapter(SettingsActivity.this);
		adapter.add(new HeaderSettingsItem(R.string.interface_));
		adapter.add(new ChooserSettingsItem(getFragmentManager(),
				languageListener));
		adapter.add(new ChooserSettingsItem(getFragmentManager(), themeListener));
		adapter.add(new ChooserSettingsItem(getFragmentManager(),
				startPageListener));
		adapter.add(new HeaderSettingsItem(R.string.player));
		adapter.add(new HeaderSettingsItem(R.string.subtitles));
		adapter.add(new ChooserSettingsItem(getFragmentManager(),
				subtitleLanguageListener));
		adapter.add(new ChooserSettingsItem(getFragmentManager(),
				subtitleFontSizeListener));
		adapter.add(new ChooserSettingsItem(getFragmentManager(),
				subtitleFontColorListener));
		adapter.add(new HeaderSettingsItem(R.string.downloads));
		cacheFolderItem = new SettingsItem(cacheFolderListener);
		adapter.add(cacheFolderItem);
		adapter.add(new CheckerSettingsItem(clearCacheFolderListener));
		adapter.add(new HeaderSettingsItem(R.string.about));
		adapter.add(new SettingsItem(visitSiteListener));
		adapter.add(new SettingsItem(visitForumListener));
		adapter.add(new SettingsItem(versionListener));
	}

	private OnClickListener backListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private ChooserItemListener languageListener = new ChooserItemListener() {

		@Override
		public void onItemChoose(int position) {
			((PopcornApplication) getApplication()).changeLanguage(LanguageUtil
					.getInterfaceLocale()[position]);
			SettingsActivity.this.mLocaleHelper.checkLanguage();
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.language);
		}

		@Override
		public CharSequence getSummary() {
			return getItems()[getItemPosition()];
		}

		@Override
		public CharSequence[] getItems() {
			return LanguageUtil.getInterfaceNative();
		}

		@Override
		public int getItemPosition() {
			String locale = Prefs.getPopcorn().getString(
					PopcornPrefs.APP_LOCALE, LanguageUtil.DEFAULT_LOCALE);
			if (LanguageUtil.DEFAULT_LOCALE.equals(locale)) {
				return LanguageUtil.DEFAULT_LOCALE_POSITION;
			}

			for (int i = 0; i < LanguageUtil.getInterfaceLocale().length; i++) {
				if (locale.equals(LanguageUtil.getInterfaceLocale()[i])) {
					return i;
				}
			}

			((PopcornApplication) getApplication())
					.changeLanguage(LanguageUtil.DEFAULT_LOCALE);
			return LanguageUtil.DEFAULT_LOCALE_POSITION;
		}

	};

	private ChooserItemListener themeListener = new ChooserItemListener() {

		@Override
		public void onItemChoose(int position) {

		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.theme);
		}

		@Override
		public CharSequence getSummary() {
			return getItems()[getItemPosition()];
		}

		@Override
		public CharSequence[] getItems() {
			return getResources().getStringArray(R.array.themes);
		}

		@Override
		public int getItemPosition() {
			return 0;
		}
	};

	private ChooserItemListener startPageListener = new ChooserItemListener() {

		@Override
		public void onItemChoose(int position) {
			Prefs.getSettngs().edit()
					.putInt(SettingsPrefs.START_PAGE, position).commit();
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.start_page);
		}

		@Override
		public CharSequence getSummary() {
			return getItems()[getItemPosition()];
		}

		@Override
		public CharSequence[] getItems() {
			return getResources().getStringArray(R.array.start_pages);
		}

		@Override
		public int getItemPosition() {
			return Prefs.getSettngs().getInt(SettingsPrefs.START_PAGE,
					MainActivity.DEFAULT_START_PAGE);
		}
	};

	private ChooserItemListener subtitleLanguageListener = new ChooserItemListener() {

		@Override
		public void onItemChoose(int position) {
			Prefs.getSettngs()
					.edit()
					.putString(SettingsPrefs.SUBTITLE_LANGUAGE,
							LanguageUtil.getSubtitlesName()[position]).commit();
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.default_subtitle);
		}

		@Override
		public CharSequence getSummary() {
			return getItems()[getItemPosition()];
		}

		@Override
		public CharSequence[] getItems() {
			return LanguageUtil.getSubtitlesNative();
		}

		@Override
		public int getItemPosition() {
			String subLang = Prefs.getSettngs().getString(
					SettingsPrefs.SUBTITLE_LANGUAGE,
					LanguageUtil.DEFAULT_SUBTITLE_LOCALE);
			if (LanguageUtil.DEFAULT_SUBTITLE_LOCALE.equals(subLang)) {
				return LanguageUtil.POSITION_WITHOUT_SUBTITLES;
			}

			for (int i = 0; i < LanguageUtil.getSubtitlesName().length; i++) {
				if (subLang.equals(LanguageUtil.getSubtitlesName()[i])) {
					return i;
				}
			}

			Prefs.getSettngs()
					.edit()
					.putString(SettingsPrefs.SUBTITLE_LANGUAGE,
							LanguageUtil.DEFAULT_SUBTITLE_LOCALE).commit();
			return LanguageUtil.POSITION_WITHOUT_SUBTITLES;
		}
	};

	private ChooserItemListener subtitleFontSizeListener = new ChooserItemListener() {

		@Override
		public void onItemChoose(int position) {
			Prefs.getSettngs().edit()
					.putInt(SettingsPrefs.SUBTITLE_FONT_SIZE, position)
					.commit();
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.font_size);
		}

		@Override
		public CharSequence getSummary() {
			return getItems()[getItemPosition()];
		}

		@Override
		public CharSequence[] getItems() {
			return getResources().getStringArray(R.array.font_size_names);
		}

		@Override
		public int getItemPosition() {
			return Prefs.getSettngs().getInt(SettingsPrefs.SUBTITLE_FONT_SIZE,
					Subtitles.FontSize.DEFAULT_POSITION);
		}
	};

	private ChooserItemListener subtitleFontColorListener = new ChooserItemListener() {

		@Override
		public void onItemChoose(int position) {
			Prefs.getSettngs().edit()
					.putInt(SettingsPrefs.SUBTITLE_FONT_COLOR, position)
					.commit();
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.font_color);
		}

		@Override
		public CharSequence getSummary() {
			return getItems()[getItemPosition()];
		}

		@Override
		public CharSequence[] getItems() {
			return getResources().getStringArray(R.array.font_color_names);
		}

		@Override
		public int getItemPosition() {
			return Prefs.getSettngs().getInt(SettingsPrefs.SUBTITLE_FONT_COLOR,
					Subtitles.FontColor.DEFAULT_POSITION);
		}
	};

	private SettingItemListener cacheFolderListener = new SettingItemListener() {

		@Override
		public void onItemClick() {
			SettingsActivity.this.startActivityForResult(new Intent(
					SettingsActivity.this, FolderChooserActivity.class),
					REQUEST_DIRECTORY);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.cache_folder);
		}

		@Override
		public CharSequence getSummary() {
			String folderSummary = StorageHelper.getInstance()
					.getRootDirectoryPath();
			if (TextUtils.isEmpty(folderSummary)) {
				return getResources().getString(
						R.string.cache_folder_not_selected);
			}
			return folderSummary;
		}
	};

	private CheckerItemListener clearCacheFolderListener = new CheckerItemListener() {

		@Override
		public void onItemChecked(boolean isChecked) {
			Prefs.getSettngs().edit()
					.putBoolean(SettingsPrefs.CLEAR_ON_EXIT, isChecked)
					.commit();
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.clear_cache_folder_on_exit);
		}

		@Override
		public CharSequence getSummary() {
			if (isChecked()) {
				return getString(R.string.enabled);
			}
			return getString(R.string.disabled);
		}

		@Override
		public boolean isChecked() {
			return Prefs.getSettngs().getBoolean(SettingsPrefs.CLEAR_ON_EXIT,
					StorageHelper.DEFAULT_CLEAR);
		}
	};

	private SettingItemListener visitSiteListener = new SettingItemListener() {

		@Override
		public void onItemClick() {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_SITE));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			SettingsActivity.this.startActivity(intent);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.visit_site);
		}

		@Override
		public CharSequence getSummary() {
			return APP_SITE;
		}
	};

	private SettingItemListener visitForumListener = new SettingItemListener() {

		@Override
		public void onItemClick() {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_FORUM));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			SettingsActivity.this.startActivity(intent);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.visit_forum);
		}

		@Override
		public CharSequence getSummary() {
			return APP_FORUM;
		}
	};

	private SettingItemListener versionListener = new SettingItemListener() {

		@Override
		public void onItemClick() {

		}

		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public CharSequence getTitle() {
			return getString(R.string.version);
		}

		@Override
		public CharSequence getSummary() {
			try {
				PackageInfo pInfo = getPackageManager().getPackageInfo(
						getPackageName(), 0);
				return pInfo.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return "";
		}
	};

	public static void start(Context context) {
		context.startActivity(new Intent(context, SettingsActivity.class));
	}
}