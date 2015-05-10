package dp.ws.popcorntime.ui;

import java.util.ArrayList;

import org.json.JSONObject;

import vn.vovi.entity.UserEntity;
import vn.vovi.utils.MessageId;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.controller.VideoAdapter;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.model.videodata.VideoData;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import dp.ws.popcorntime.ui.locale.LocaleFragment;
import dp.ws.popcorntime.utils.JSONHelper;

public class GridVideoFragment extends LocaleFragment implements
		LoaderCallbacks<LoaderResponse> {

	public static final String VIDEO_INFO_LIST_KEY = "popcorntime_video_info_list";

	private final int PAGE_ERROR_DELAY = 2500;
	private final int PAGE_LOADER_ID = 1001;
	private final int MAX_PAGE_COUNT = 6;
	private final int ROW_COUNT_FOR_NEXT_PAGE = 4;

	private GridView videoGrid;
	private VideoData mVideoData;
	private VideoAdapter mVideosAdapter;

	private boolean canLoadPage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<VideoInfo> info = getArguments().getParcelableArrayList(
				VIDEO_INFO_LIST_KEY);
		mVideosAdapter = new VideoAdapter(getActivity(), info);
		canLoadPage = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grid_video, container,
				false);
		populateView(view);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_grid_video, container);
		populateView(view);
	}

	private void populateView(View view) {
		videoGrid = (GridView) view.findViewById(R.id.videos_content_view);
		videoGrid.setAdapter(mVideosAdapter);
		videoGrid.setOnScrollListener(contentScrollListener);
	}

	@Override
	public void onDestroy() {
		getLoaderManager().destroyLoader(PAGE_LOADER_ID);
		super.onDestroy();
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case PAGE_LOADER_ID:
			return new URLLoader(getActivity(), args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {
		switch (loader.getId()) {
		case PAGE_LOADER_ID:
			videosPageFinished(response);
			break;
		default:
			break;
		}
	}

	public void setVideoData(VideoData videoData) {
		this.mVideoData = videoData;
	}

	private void videosPageFinished(LoaderResponse response) {
		if (response.error != null) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mVideoData.setPage(mVideoData.getPage() - 1);
					canLoadPage = true;
				}
			}, PAGE_ERROR_DELAY);
		} else {
			ArrayList<VideoInfo> data = null;
			try {
				data = JSONHelper.parseMovies(response.data);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (data != null && data.size() > 0) {
				canLoadPage = true;
				mVideosAdapter.addData(data);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	private void restartVideosPageLoader() {
		mVideoData.setPage(mVideoData.getPage() + 1);
		if (mVideoData.getPage() <= MAX_PAGE_COUNT) {
			try {
				Bundle data = new Bundle();
				data.putString(URLLoader.URL_KEY,
						getResources().getString(R.string.url));
				JSONObject obj = new JSONObject();
				obj.put("mid", MessageId.GET_FILM_WITH_CATELOGY);
				obj.put("sessionKey", UserEntity.getInstant().getSessionKey());
				obj.put("username", UserEntity.getInstant().getUserName());
				obj.put("catId", mVideoData.getCatId());
				obj.put("pageIndex", mVideoData.getPage());
				data.putString(URLLoader.DATA, obj.toString());
				getLoaderManager().restartLoader(PAGE_LOADER_ID, data, this)
						.forceLoad();
			} catch (Throwable e) {
				Log.e("MyApp", e.getMessage(), e);
			}
		}
	}

	private OnScrollListener contentScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (canLoadPage && totalItemCount != 0) {
				int dec = totalItemCount - firstVisibleItem;
				if (dec <= videoGrid.getNumColumns() * ROW_COUNT_FOR_NEXT_PAGE) {
					canLoadPage = false;
					restartVideosPageLoader();
				}
			}
		}
	};
}