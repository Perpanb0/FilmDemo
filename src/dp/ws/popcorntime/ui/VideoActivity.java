package dp.ws.popcorntime.ui;

import org.json.JSONObject;

import vn.vovi.entity.UserEntity;
import vn.vovi.utils.MessageId;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import dp.ws.popcorntime.ui.base.PopcornLoadActivity;
import dp.ws.popcorntime.ui.base.VideoTypeFragment;
import dp.ws.popcorntime.ui.settings.SettingsActivity;

public class VideoActivity extends PopcornLoadActivity implements
		OnClickListener, LoaderCallbacks<LoaderResponse> {

	public static final String VIDEO_INFO_KEY = "popcorntime_video_info";

	private VideoInfo mVideoInfo;
	private VideoTypeFragment videoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		mVideoInfo = getIntent().getExtras().getParcelable(VIDEO_INFO_KEY);

		// Header
		View header = setPopcornHeaderView(R.layout.header_video);
		header.findViewById(R.id.popcorn_action_back).setOnClickListener(
				VideoActivity.this);
		header.findViewById(R.id.popcorn_action_settings).setOnClickListener(
				VideoActivity.this);

		// Content
		setPopcornContentViewId(R.id.popcorn_content);
		setPopcornContentBackgroundResource(R.color.classic_video_body);

		restartInfoLoader();
	}

	@Override
	public void showContent() {
		replaceFragment(videoFragment);
	}

	@Override
	public void retryLoad() {
		restartInfoLoader();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.popcorn_action_back:
			onBackPressed();
			break;
		case R.id.popcorn_action_settings:
			SettingsActivity.start(VideoActivity.this);
			break;
		default:
			break;
		}
	}

	private void restartInfoLoader() {
		try {
			Bundle data = new Bundle();
			data.putString(URLLoader.URL_KEY,
					getResources().getString(R.string.url));
			JSONObject obj = new JSONObject();
			obj.put("mid", MessageId.GET_VIDEO_INFO);
			obj.put("username", UserEntity.getInstant().getUserName());
			obj.put("sessionKey", UserEntity.getInstant().getSessionKey());
			obj.put("filmId", mVideoInfo.id);
			data.putString(URLLoader.DATA, obj.toString());
			getLoaderManager().restartLoader(MessageId.GET_VIDEO_INFO, data,
					this).forceLoad();
		} catch (Throwable e) {
			Log.e("MyApp", e.getMessage(), e);
		}
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		showLoading();
		switch (id) {
		case MessageId.GET_VIDEO_INFO:
			return new URLLoader(VideoActivity.this, args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {
		switch (loader.getId()) {
		case MessageId.GET_VIDEO_INFO:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					MessageId.GET_VIDEO_INFO, response));
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	private Handler loaderHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageId.GET_VIDEO_INFO:
				infoFinished((LoaderResponse) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	private void infoFinished(LoaderResponse response) {
		if (response.error != null) {
			showError();
		} else {
			videoFragment = new VideoMovieFragment();
			Bundle args = new Bundle();
			args.putString(VideoTypeFragment.RESPONSE_JSON_KEY, response.data);
			videoFragment.setArguments(args);
			showContent();
		}
	}

	@Override
	public void onBackPressed() {
		if (videoFragment != null && videoFragment.isAdded()) {
			videoFragment.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}
}