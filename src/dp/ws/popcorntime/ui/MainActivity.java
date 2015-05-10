package dp.ws.popcorntime.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.vovi.entity.UserEntity;
import vn.vovi.utils.MessageId;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.GenreAdapter;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.model.videodata.CatInfo;
import dp.ws.popcorntime.model.videodata.MovieData;
import dp.ws.popcorntime.model.videodata.TVShowData;
import dp.ws.popcorntime.model.videodata.VideoData;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import dp.ws.popcorntime.prefs.Prefs;
import dp.ws.popcorntime.prefs.SettingsPrefs;
import dp.ws.popcorntime.ui.base.PopcornLoadActivity;
import dp.ws.popcorntime.ui.settings.SettingsActivity;
import dp.ws.popcorntime.ui.widget.BlockTouchFrameLayout;
import dp.ws.popcorntime.updater.Updater;
import dp.ws.popcorntime.utils.JSONHelper;
import dp.ws.popcorntime.utils.StorageHelper;

public class MainActivity extends PopcornLoadActivity implements
		LoaderCallbacks<LoaderResponse>, OnClickListener {

	public static final int MOVIES_PAGE = 0;
	public static final int TV_SHOWS_PAGE = 1;
	public static final int DEFAULT_START_PAGE = MOVIES_PAGE;

	@SuppressWarnings("unused")
	private final int SPLASH_SHOW_TIME = 3000;
	private final int EXIT_DELAY_TIME = 2000;

	private VideoData currentVideoData = null;
	private VideoData moviesData = null;
	private VideoData tvShowsData = null;

	private DrawerLayout mDrawerLayout;
	private BlockTouchFrameLayout mContentFrame;
	private RelativeLayout mDrawer;
	private Button moviesDrawerBtn;
	private Button tvShowsDrawerBtn;
	private EditText searchView;
	private ListView mGenreList;
	private GenreAdapter mGenreAdapter;

	private GridVideoFragment videoFragment;
	private GridFavoritesFragment favoritesFragment = new GridFavoritesFragment();

	private boolean doubleBackToExitPressedOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		moviesData = new MovieData(MainActivity.this);
		tvShowsData = new TVShowData(MainActivity.this);

		// Splash
		setPopcornSplashView(R.layout.view_splash);

		// Header
		View header = setPopcornHeaderView(R.layout.header_main);
		header.findViewById(R.id.header_menu).setOnClickListener(
				MainActivity.this);
		header.findViewById(R.id.header_overflow).setOnClickListener(
				MainActivity.this);

		// Content
		View content = setPopcornContentView(R.layout.activity_main);
		setPopcornContentViewId(R.id.main_content_frame);

		mDrawerLayout = (DrawerLayout) content
				.findViewById(R.id.main_drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerLayout.setFocusableInTouchMode(false);
		mDrawerLayout.setDrawerListener(drawerListener);
		mContentFrame = (BlockTouchFrameLayout) content
				.findViewById(R.id.main_content_frame);
		mDrawer = (RelativeLayout) content.findViewById(R.id.main_drawer);

		// video type switch
		moviesDrawerBtn = (Button) mDrawer
				.findViewById(R.id.main_drawer_movies_btn);
		moviesDrawerBtn.setOnClickListener(MainActivity.this);
		tvShowsDrawerBtn = (Button) mDrawer
				.findViewById(R.id.main_drawer_tvshows_btn);
		tvShowsDrawerBtn.setOnClickListener(MainActivity.this);

		mGenreAdapter = new GenreAdapter(MainActivity.this);
		mGenreList = (ListView) mDrawer.findViewById(R.id.main_drawer_list);
		mGenreList.setAdapter(mGenreAdapter);
		mGenreList.setOnItemClickListener(genreListener);

		searchView = (EditText) mDrawer.findViewById(R.id.main_drawer_search);
		searchView.setOnEditorActionListener(searchListener);
		searchView.setOnKeyListener(searchKeyListener);
		updateSearchCursor(getResources().getConfiguration());

		// updateLocaleText();

		if (savedInstanceState == null) {
			showSplash();
		}

	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		moviesDrawerBtn.setText(R.string.movies);
		tvShowsDrawerBtn.setText(R.string.tv_shows);
		searchView.setHint(R.string.search);
		getFilmCatelogy();
		// moviesData.setLocaleGenres(MainActivity.this);
		// tvShowsData.setLocaleGenres(MainActivity.this);
		mGenreAdapter.notifyDataSetInvalidated();
	}

	private void getFilmCatelogy() {
		try {
			Bundle data = new Bundle();
			data.putString(URLLoader.URL_KEY,
					getResources().getString(R.string.url));
			JSONObject obj = new JSONObject();
			obj.put("mid", MessageId.GET_CATELOGY);
			obj.put("sessionKey", UserEntity.getInstant().getSessionKey());
			obj.put("username", UserEntity.getInstant().getUserName());
			data.putString(URLLoader.DATA, obj.toString());
			getLoaderManager()
					.restartLoader(MessageId.GET_CATELOGY, data, this)
					.forceLoad();
		} catch (Throwable e) {
			Log.e("MyApp", e.getMessage(), e);
		}
	}

	private void setFilmCatelogy(LoaderResponse response) {
		if (response.error != null) {
			showError();
		} else {
			try {
				String data = response.data;
				JSONObject jsO = new JSONObject(data);
				JSONArray jsA = jsO.getJSONArray("cats");
				ArrayList<CatInfo> list = new ArrayList<CatInfo>();
				for (int i = 0; i < jsA.length(); i++) {
					try {
						list.add(new CatInfo(jsA.getJSONObject(i).getString(
								"id"), jsA.getJSONObject(i).getString("name")));
					} catch (Throwable e) {
						Log.e("MyApp", e.getMessage(), e);
					}
				}
				moviesData.setLocaleGenres(list);
			} catch (JSONException e) {
				Log.e("MyApp", e.getMessage(), e);
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			setPopcornSplashVisible(false);
			Updater.getInstance().init(MainActivity.this);
			onMoviesClick();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		updateSearchCursor(newConfig);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			if (mDrawerLayout.isDrawerOpen(mDrawer)) {
				mDrawerLayout.closeDrawer(mDrawer);
			}
		}
	}

	@Override
	protected void onDestroy() {
		Updater.getInstance().destroy(MainActivity.this);
		super.onDestroy();
		// android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void showContent() {
		replaceFragment(videoFragment);
	}

	@Override
	public void retryLoad() {
		restartVideosLoader();
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		showLoading();

		switch (id) {
		case MessageId.GET_CATELOGY:
			return new URLLoader(MainActivity.this, args);
		case MessageId.GET_FILM_WITH_CATELOGY:
			return new URLLoader(MainActivity.this, args);
		case MessageId.LOGIN:
			return new URLLoader(MainActivity.this, args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {
		Log.d("MyApp", "====loader.getId():" + loader.getId() + "@data:"
				+ response.data);
		switch (loader.getId()) {
		case MessageId.GET_CATELOGY:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					MessageId.GET_CATELOGY, response));
			break;
		case MessageId.GET_FILM_WITH_CATELOGY:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					MessageId.GET_FILM_WITH_CATELOGY, response));
			break;
		case MessageId.LOGIN:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					MessageId.LOGIN, response));
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	@Override
	public void onClick(View v) {
		Log.d("MyApp", "========================= onClick: " + v.getId());
		switch (v.getId()) {
		case R.id.main_drawer_movies_btn:
			onMoviesClick();
			break;
		case R.id.main_drawer_tvshows_btn:
			onTVShowsClick();
			break;
		case R.id.header_menu:
			onMenuClick();
			break;
		case R.id.header_overflow:
			if (mDrawerLayout.isDrawerOpen(mDrawer)) {
				mDrawerLayout.closeDrawer(mDrawer);
			}
			onOverflowPressed(v);
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(mDrawer)) {
			mDrawerLayout.closeDrawer(mDrawer);
			return;
		}

		if (doubleBackToExitPressedOnce) {
			if (Prefs.getSettngs()
					.getBoolean(SettingsPrefs.CLEAR_ON_EXIT, true)) {
				StorageHelper.getInstance().clearCacheDirectory();
			}
			finish();
		} else {
			doubleBackToExitPressedOnce = true;
			Toast.makeText(this, R.string.exit_msg, Toast.LENGTH_SHORT).show();
		}

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, EXIT_DELAY_TIME);
	}

	private void showSplash() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setPopcornSplashVisible(true);
		
		try {
			Bundle data = new Bundle();
			data.putString(URLLoader.URL_KEY,
					getResources().getString(R.string.url));
			JSONObject obj = new JSONObject();
			obj.put("mid", MessageId.LOGIN);
			obj.put("username", "test");
			UserEntity.getInstant().setUserName("test");
			obj.put("password", "123");
			data.putString(URLLoader.DATA, obj.toString());
			getLoaderManager().restartLoader(MessageId.LOGIN, data, this)
					.forceLoad();
		} catch (Throwable e) {
			Log.d("MyApp", e.getMessage(), e);
		}
	}

	private void restartVideosLoader() {
		Log.d("MyApp", "============================== restartVideosLoader");
		// if (UserEntity.getInstant().getSessionKey() == null) {
		// return;
		// }
		try {
			Bundle data = new Bundle();
			data.putString(URLLoader.URL_KEY,
					getResources().getString(R.string.url));
			JSONObject obj = new JSONObject();
			obj.put("mid", MessageId.GET_FILM_WITH_CATELOGY);
			obj.put("sessionKey", UserEntity.getInstant().getSessionKey());
			obj.put("username", UserEntity.getInstant().getUserName());
			// currentVideoData.setCatId(30);
			obj.put("catId", currentVideoData.getCatId(currentVideoData
					.getGenrePosition()));
			obj.put("pageIndex", currentVideoData.getPage());
			data.putString(URLLoader.DATA, obj.toString());
			getLoaderManager().restartLoader(MessageId.GET_FILM_WITH_CATELOGY,
					data, this).forceLoad();
		} catch (Throwable e) {
			Log.e("MyApp", e.getMessage(), e);
		}
	}

	private void onMenuClick() {
		if (mDrawerLayout.isDrawerOpen(mDrawer)) {
			mDrawerLayout.closeDrawer(mDrawer);

		} else {
			mDrawerLayout.openDrawer(mDrawer);
			if (currentVideoData != null) {
				mGenreList.setSelection(currentVideoData.getGenrePosition());
			}
		}
	}

	private void onOverflowPressed(View v) {
		PopupMenu popup = new PopupMenu(MainActivity.this, v);
		popup.setOnMenuItemClickListener(overflowMenuListener);
		popup.inflate(R.menu.popup_main);
		popup.show();
	}

	private void onMoviesClick() {
		if (currentVideoData != moviesData) {
			moviesBtnSelect();
			tvShowsBtnUnselect();
			selectVideoData(moviesData);
		}
	}

	private void onTVShowsClick() {
		if (currentVideoData != tvShowsData) {
			moviesBtnUnselect();
			tvShowsBtnSelect();
			selectVideoData(tvShowsData);
		}
	}

	private void moviesBtnSelect() {
		moviesDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_selected_selector);
		moviesDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchSelected);
		moviesDrawerBtn.setShadowLayer(1, 1, 1,
				getResources().getColor(R.color.classic_text_shadow));
	}

	private void moviesBtnUnselect() {
		moviesDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_unselected_selector);
		moviesDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchUnselected);
		moviesDrawerBtn.setShadowLayer(0, 0, 0,
				getResources().getColor(android.R.color.transparent));
	}

	private void tvShowsBtnSelect() {
		tvShowsDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_selected_selector);
		tvShowsDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchSelected);
		tvShowsDrawerBtn.setShadowLayer(1, 1, 1,
				getResources().getColor(R.color.classic_text_shadow));
	}

	private void tvShowsBtnUnselect() {
		tvShowsDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_unselected_selector);
		tvShowsDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchUnselected);
		tvShowsDrawerBtn.setShadowLayer(0, 0, 0,
				getResources().getColor(android.R.color.transparent));
	}

	private void unselectVideoData() {
		if (currentVideoData != null) {
			if (moviesData == currentVideoData) {
				moviesBtnUnselect();
			} else if (tvShowsData == currentVideoData) {
				tvShowsBtnUnselect();
			}
			mGenreList.setItemChecked(currentVideoData.getGenrePosition(),
					false);
			mGenreAdapter.inactive();
			currentVideoData.setPage(1);
			currentVideoData = null;
		}
	}

	private void selectVideoData(VideoData videoData) {
		Log.d("MyApp", "============================== selectVideoData");
		if (currentVideoData != null) {
			currentVideoData.setPage(1);
		}
		currentVideoData = videoData;
		searchView.setText(currentVideoData.getKeywords());
		mGenreAdapter.replaceData(currentVideoData);
		mGenreList.setItemChecked(currentVideoData.getGenrePosition(), true);
		mGenreList.clearFocus();
		mGenreList.post(new Runnable() {

			@Override
			public void run() {
				mGenreList.setSelection(currentVideoData.getGenrePosition());
			}
		});
		searchView.clearFocus();
		restartVideosLoader();
	}

	private void selecteGenre(int position) {
		Log.d("MyApp", "============================== selecteGenre");
		if (currentVideoData == null) {
			moviesData.setGenre(position);
			moviesData.setKeywords("");
			moviesBtnSelect();
			selectVideoData(moviesData);
		} else {
			searchView.setText("");
			currentVideoData.setPage(1);
			currentVideoData.setGenre(position);
			currentVideoData.setKeywords("");
			mGenreAdapter.notifyDataSetInvalidated();
			searchView.clearFocus();
			restartVideosLoader();
		}
	}

	private void updateSearchCursor(Configuration config) {
		if (Configuration.ORIENTATION_LANDSCAPE == config.orientation) {
			searchView.setCursorVisible(false);
		} else if (Configuration.ORIENTATION_PORTRAIT == config.orientation) {
			searchView.setCursorVisible(true);
		}
	}

	private Handler loaderHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageId.GET_CATELOGY:
				setFilmCatelogy((LoaderResponse) msg.obj);
				break;
			case MessageId.GET_FILM_WITH_CATELOGY:
				videoListFinished((LoaderResponse) msg.obj);
				break;
			case MessageId.LOGIN:
				String data = ((LoaderResponse) msg.obj).data;
				try {
					JSONObject obj = new JSONObject(data);
					UserEntity.getInstant().setSessionKey(
							obj.getString("sessionKey"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				updateLocaleText();
				break;
			default:
				break;
			}
		}
	};

	private void videoListFinished(LoaderResponse response) {
		if (response.error != null) {
			showError();
		} else {
			ArrayList<VideoInfo> data = null;
			try {
				data = JSONHelper.parseMovies(response.data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (data != null) {
				Bundle args = new Bundle();
				args.putParcelableArrayList(
						GridVideoFragment.VIDEO_INFO_LIST_KEY, data);
				videoFragment = new GridVideoFragment();
				videoFragment.setArguments(args);
				videoFragment.setVideoData(currentVideoData);
				showContent();
			} else {
				showError();
			}
		}
	}

	/*
	 * Listeners
	 */

	private OnMenuItemClickListener overflowMenuListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.popup_favorites:
				if (!favoritesFragment.isAdded()) {
					unselectVideoData();
					getLoaderManager().destroyLoader(
							MessageId.GET_FILM_WITH_CATELOGY);
					replaceFragment(favoritesFragment);
				}
				return true;
			case R.id.popup_settings:
				SettingsActivity.start(MainActivity.this);
				return true;
			default:
				return false;
			}
		}
	};

	private DrawerListener drawerListener = new DrawerListener() {

		@Override
		public void onDrawerStateChanged(int arg0) {

		}

		@Override
		public void onDrawerSlide(View arg0, float arg1) {

		}

		@Override
		public void onDrawerOpened(View arg0) {
			mContentFrame.setBlockTouchEvent(true);
		}

		@Override
		public void onDrawerClosed(View arg0) {
			mContentFrame.setBlockTouchEvent(false);
		}
	};

	private OnItemClickListener genreListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selecteGenre(position);
		}
	};

	private OnEditorActionListener searchListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				search(v);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return true;
			}
			return false;
		}
	};

	private OnKeyListener searchKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				search(searchView);
				return true;
			}
			return false;
		}
	};

	private void search(TextView view) {
		if (currentVideoData != null) {
			currentVideoData.setPage(1);
			currentVideoData.setKeywords(view.getText().toString());
			restartVideosLoader();
		}
		view.clearFocus();
	}

}