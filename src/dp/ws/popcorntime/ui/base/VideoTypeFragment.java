package dp.ws.popcorntime.ui.base;

import java.io.File;

import org.json.JSONObject;

import vn.vovi.utils.MessageId;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.database.tables.Favorites;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.model.WatchInfo;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import dp.ws.popcorntime.prefs.PopcornPrefs;
import dp.ws.popcorntime.prefs.Prefs;
import dp.ws.popcorntime.subtitles.SubtitleListener;
import dp.ws.popcorntime.subtitles.Subtitles;
import dp.ws.popcorntime.ui.VideoActivity;
import dp.ws.popcorntime.ui.widget.FileChooserDialog;
import dp.ws.popcorntime.ui.widget.FileChooserDialog.OnChooserListener;
import dp.ws.popcorntime.ui.widget.ItemSelectButton;
import dp.ws.popcorntime.utils.Logger;
import dp.ws.popcorntime.utils.StorageHelper;

public abstract class VideoTypeFragment extends VideoBaseFragment implements
		LoaderCallbacks<LoaderResponse>, SubtitleListener, OnChooserListener {

	public static final String RESPONSE_JSON_KEY = "popcorntime_response_json";

	private final int WATCH_LOADER_ID = 1001;

	protected PopcornBaseActivity mActivity;
	protected Subtitles mSubtitles;
	protected int torrentPosition = 0;
	private DisplayImageOptions imageOptions;
	protected VideoInfo videoInfo;
	private boolean isFavorites;
	private ArrayAdapter<String> mSubtitleAdapter;

	protected boolean changeOrientation = false;

	// view
	protected View prepare;
	protected ToggleButton favorites;
	protected RatingBar rating;
	protected ItemSelectButton subtitleSelectButton;

	private FileChooserDialog customSubtitleDialog;

	LoaderCallbacks<LoaderResponse> loaderCallbacks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (PopcornBaseActivity) getActivity();
		videoInfo = mActivity.getIntent().getExtras()
				.getParcelable(VideoActivity.VIDEO_INFO_KEY);
		imageOptions = new DisplayImageOptions.Builder().cacheInMemory(false)
				.cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build();
		mSubtitles = new Subtitles(getActivity());
		mSubtitles.setSubtitleListener(VideoTypeFragment.this);
		mSubtitleAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_dropdown_item, mSubtitles.data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = mActivity.setPopcornSplashView(R.layout.view_prepare);
		prepare = view.findViewById(R.id.video_prepare);
		Button close = (Button) view.findViewById(R.id.video_prepare_close);
		close.setOnClickListener(closeListener);
		return view;
	}

	@Override
	protected void populateView(View view) {
		super.populateView(view);
		ImageLoader.getInstance().displayImage(videoInfo.posterBigUrl, poster,
				imageOptions);
		title.setText(Html.fromHtml("<b>" + videoInfo.title + "</b>"));
		rating = (RatingBar) view.findViewById(R.id.video_rating);
		rating.setRating(videoInfo.rating * RATING_COEF);
		favorites = (ToggleButton) view.findViewById(R.id.video_favorites);
		favorites.setChecked(isFavorites);
		favorites.setOnCheckedChangeListener(favoritesListener);

		subtitleSelectButton = (ItemSelectButton) view
				.findViewById(R.id.video_subtitles);
		subtitleSelectButton.init(getFragmentManager(), mSubtitleAdapter);
		subtitleSelectButton.setOnItemSelectedListener(subtitleListener);

	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		subtitleSelectButton.setVisibility(View.INVISIBLE);
		subtitleSelectButton.setPrompt(getString(R.string.subtitles));
		// updateSubtitleData();
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		return new URLLoader(getActivity(), args);
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {
		switch (loader.getId()) {
		case MessageId.GET_VIDEO_STREAM_FILE:
			mSubtitles.onLoadFinished(loader, response);
			break;
		case MessageId.GET_VIDEO_INFO:
			parseInfoResponse(response.data);
			break;
		case WATCH_LOADER_ID:
			loadWatchFinished(response);
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	@Override
	public void onSubtitleLoadError(String message) {
		Logger.error(message);
		updateSubtitleData();
	}

	@Override
	public void onChooserSelected(File file) {
		mSubtitles.urls.set(Subtitles.CUSTOM_SUBS_POSITION, Uri.fromFile(file)
				.toString());
		mSubtitles.position = Subtitles.CUSTOM_SUBS_POSITION;
	}

	@Override
	public void onChooserCancel() {
		subtitleSelectButton.showSelectedItem(mSubtitles.position);
	}

	public void onBackPressed() {
		if (mActivity.isPopcornSplashVisible()) {
			breakPrepare();
		} else {
			getActivity().finish();
		}
	}

	protected void onChangeScreenOrientation(int resourceId) {
		changeOrientation = true;
		if (customSubtitleDialog != null && customSubtitleDialog.isAdded()) {
			customSubtitleDialog.dismiss();
		}
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		mLocaleHelper.updateLocale();
		View view = LayoutInflater.from(getActivity()).inflate(resourceId,
				container);
		populateView(view);
	}

	protected void checkIsFavorites(VideoInfo info) {
		String selection = Favorites._IMDB + "=\"" + info.imdb + "\"";
		Cursor cursor = Favorites.query(mActivity, null, selection, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				isFavorites = true;
				Favorites.update(mActivity, info);
			} else {
				isFavorites = false;
			}
			cursor.close();
		}
	}

	protected void updateSubtitleData() {
		if (mSubtitles.data != null
				&& mSubtitles.data.size() >= Subtitles.DEFAULT_SUBS_COUNT) {
			mSubtitles.data.set(Subtitles.WITHOUT_SUBS_POSITION, getResources()
					.getString(R.string.without_subtitle));
			mSubtitles.data.set(Subtitles.CUSTOM_SUBS_POSITION, getResources()
					.getString(R.string.custom_subtitle));

			subtitleSelectButton.setVisibility(View.VISIBLE);
			subtitleSelectButton.showSelectedItem(mSubtitles.position);
		}
		mSubtitleAdapter.notifyDataSetChanged();
	}

	protected void updatePosterBigUrl(String url) {
		videoInfo.posterBigUrl = url;
	}

	protected void updateRanting(float r) {
		videoInfo.rating = r;
	}

	protected void restartSubtitleLoader() {
		loaderCallbacks = VideoTypeFragment.this;
		Bundle data = new Bundle();
		data.putString(URLLoader.URL_KEY, getResources()
				.getString(R.string.url));
		data.putString(URLLoader.DATA, getMovieDataUrl().toString());
		getLoaderManager().restartLoader(MessageId.GET_VIDEO_STREAM_FILE, data,
				this).forceLoad();

	}

	protected void restartSeasonLoader() {
		Bundle data = new Bundle();
		data.putString(URLLoader.URL_KEY, getResources()
				.getString(R.string.url));
		data.putString(URLLoader.DATA, getNewSeasonData().toString());
		getLoaderManager().restartLoader(MessageId.GET_VIDEO_INFO, data, this)
				.forceLoad();
	}

	private void onFavoritesChecked(boolean isChecked) {
		if (isChecked) {
			Favorites.insert(mActivity, videoInfo);
		} else {
			Favorites.delete(mActivity, videoInfo);
		}
	}

	private void showCustomSubtitleDialog() {
		if (customSubtitleDialog == null) {
			String[] fileExt = new String[] { Subtitles.Formats.SRT };
			customSubtitleDialog = new FileChooserDialog(
					StorageHelper.getSDCardFolder(mActivity), fileExt);
			customSubtitleDialog.setChooserListener(VideoTypeFragment.this);
		}

		if (!customSubtitleDialog.isAdded()) {
			Bundle args = new Bundle();
			args.putString(FileChooserDialog.TITLE_KEY,
					getString(R.string.select_subtitle));
			customSubtitleDialog.setArguments(args);
			customSubtitleDialog.show(mActivity.getFragmentManager(),
					"custom_subtitle_dialog");
		}
	}

	private void loadWatchFinished(LoaderResponse response) {
		if (response.error != null) {
			Toast.makeText(getActivity(), R.string.error_metadata,
					Toast.LENGTH_SHORT).show();
		} else {
			Prefs.getPopcorn().edit()
					.putString(PopcornPrefs.LAST_TORRENT, response.data)
					.commit();

			WatchInfo watchInfo = new WatchInfo();
			watchInfo.torrentFile = response.data;
			watchInfo.type = videoInfo.getType();
			watchInfo.imdb = videoInfo.imdb;
			watchInfo.watchDir = cacheDirectory.getAbsolutePath();
			watchInfo.subtitlesPosition = mSubtitles.position;
			watchInfo.subtitlesData = mSubtitles.data;
			watchInfo.subtitlesUrls = mSubtitles.urls;
		}

		prepare.clearAnimation();
		mActivity.setPopcornSplashVisible(false);
	}

	private void breakPrepare() {
		getLoaderManager().destroyLoader(WATCH_LOADER_ID);
		prepare.clearAnimation();
		mActivity.setPopcornSplashVisible(false);
	}

	protected abstract JSONObject getNewSeasonData();

	protected abstract JSONObject getMovieDataUrl();

	protected abstract void parseInfoResponse(String json);

	/*
	 * TODO: Listeners
	 */

	private OnClickListener closeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			breakPrepare();
		}
	};

	private OnCheckedChangeListener favoritesListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			onFavoritesChecked(isChecked);
			isFavorites = isChecked;
		}

	};

	private ItemSelectButton.OnItemSelectedListener subtitleListener = new ItemSelectButton.OnItemSelectedListener() {

		@Override
		public void onItemSelected(int position) {
			if (Subtitles.CUSTOM_SUBS_POSITION == position) {
				showCustomSubtitleDialog();
			} else {
				mSubtitles.position = position;
			}
		}
	};
}