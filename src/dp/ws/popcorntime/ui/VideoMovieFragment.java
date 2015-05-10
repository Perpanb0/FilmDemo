package dp.ws.popcorntime.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import vn.vovi.entity.UserEntity;
import vn.vovi.utils.MessageId;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.EpisodeAdapter;
import dp.ws.popcorntime.controller.SeasonAdapter;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.model.videoinfo.Episode;
import dp.ws.popcorntime.model.videoinfo.Season;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import dp.ws.popcorntime.ui.base.VideoTypeFragment;

@SuppressWarnings("unused")
public class VideoMovieFragment extends VideoTypeFragment implements
		LoaderCallbacks<LoaderResponse> {

	private Spinner seasonsView;
	private Spinner episodesView;

	private SeasonAdapter mSeasonAdapter;
	private EpisodeAdapter mEpisodeAdapter;

	private ArrayList<Season> listSeason = new ArrayList<Season>();
	private ArrayList<Episode> listEpisode = new ArrayList<Episode>();

	private Season currentSeason = null;
	private Episode currentEpisode = null;

	private TextView actors;

	private String additionalDescription = "";

	private View currentView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		videoInfo = mActivity.getIntent().getExtras()
				.getParcelable(VideoActivity.VIDEO_INFO_KEY);
		parseInfoResponse(getArguments().getString(RESPONSE_JSON_KEY));
		mSeasonAdapter = new SeasonAdapter(getActivity(),
				android.R.layout.simple_spinner_item, listSeason);
		mEpisodeAdapter = new EpisodeAdapter(getActivity(),
				android.R.layout.simple_spinner_item, listEpisode);
		checkIsFavorites(videoInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_video_movie, container,
				false);
		currentView = view;
		populateView(view);
		// restartSubtitleLoader();
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		onChangeScreenOrientation(R.layout.fragment_video_movie);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		actors.setText(getString(R.string.actors) + ":\n"
				+ Html.fromHtml(videoInfo.actors));
		trailerText.setText(R.string.trailer);
	}

	@Override
	protected void populateView(View view) {
		super.populateView(view);
		description.setText(Html.fromHtml(videoInfo.description
				+ additionalDescription));
		actors = (TextView) view.findViewById(R.id.video_movie_actors);
		seasonsView = (Spinner) view.findViewById(R.id.video_seasons);
		episodesView = (Spinner) view.findViewById(R.id.video_episodes);
		if (!listSeason.isEmpty()) {
			seasonsView.setVisibility(View.VISIBLE);
			episodesView.setVisibility(View.VISIBLE);
			seasonsView.setAdapter(mSeasonAdapter);
			seasonsView.setOnItemSelectedListener(seasonListener);

			episodesView.setAdapter(mEpisodeAdapter);
			episodesView.setOnItemSelectedListener(episodeListener);
		} else {
			seasonsView.setVisibility(View.INVISIBLE);
			episodesView.setVisibility(View.INVISIBLE);
		}
		updateLocaleText();
		restartSubtitleLoader();
	}

	private OnItemSelectedListener seasonListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				final int position, long id) {
			currentSeason = mSeasonAdapter.getItem(position);
			mSeasonAdapter.setSelectedItem(position);
			seasonsView.post(new Runnable() {
				@Override
				public void run() {
					if (currentSeason.id != videoInfo.id) {
						videoInfo.id = currentSeason.id;
						restartSeasonLoader();
					}
					seasonsView.setSelection(position);
				}
			});
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnItemSelectedListener episodeListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				final int position, long id) {
			currentEpisode = mEpisodeAdapter.getItem(position);
			Log.d("MyApp", "======================== " + currentEpisode.seq);
			mEpisodeAdapter.setSelectedItem(position);
			episodesView.setSelection(position);
			episodesView.post(new Runnable() {
				@Override
				public void run() {
					if (currentEpisode.seq != videoInfo.seq) {
						videoInfo.seq = currentEpisode.seq;
						restartSubtitleLoader();
					}
				}
			});
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	@Override
	protected void parseInfoResponse(String json) {
		try {
			JSONObject info = new JSONObject(json);
			JSONObject jsO = info.getJSONObject("Film");
			videoInfo.id = jsO.getString("id");
			videoInfo.title = jsO.getString("MovieName");
			videoInfo.actors = jsO.getString("Cast");
			videoInfo.description = jsO.getString("PlotEN");
			videoInfo.actors = jsO.getString("PlotVI");
			videoInfo.posterBigUrl = jsO.getString("poster100x149");
			videoInfo.rating = Float.valueOf(jsO.getString("ImdbRating"));
			videoInfo.id = jsO.getString("id");
			videoInfo.seq = "0";
			updatePosterBigUrl(videoInfo.posterBigUrl);
			updateRanting(videoInfo.rating);
			videoInfo.trailer = jsO.getString("Trailer");
			int seq = Integer.valueOf(jsO.getString("Sequence"));
			if (seq != 0) {
				String st = jsO.getString("Season");
				String[] s = st.split(";");
				listSeason.clear();
				listSeason.add(new Season(videoInfo.id, videoInfo.title));
				listEpisode.clear();
				for (String se : s) {
					listSeason.add(new Season(se.split("#")[1],
							se.split("#")[0]));
				}
				for (int i = 1; i <= seq; i++) {
					listEpisode.add(new Episode(videoInfo.id,
							String.valueOf(i), getString(R.string.episode)
									+ " " + i));
				}
			} else {
				listSeason.clear();
				listEpisode.clear();
			}
			if (currentView != null)
				populateView(currentView);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSubtitleLoadSucces(int id, String data) {
		try {
			JSONObject subs = new JSONObject(data);
			switch (id) {
			case MessageId.GET_VIDEO_STREAM_FILE:
				mSubtitles.parseMovie(subs);
				break;
			case MessageId.GET_VIDEO_INFO:
				parseInfoResponse(data);
			default:
				break;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		updateSubtitleData();
	}

	@Override
	protected JSONObject getNewSeasonData() {
		JSONObject data = new JSONObject();
		try {
			data.put("mid", MessageId.GET_VIDEO_INFO);
			data.put("sessionKey", UserEntity.getInstant().getSessionKey());
			data.put("username", UserEntity.getInstant().getUserName());
			data.put("filmId", videoInfo.id);
		} catch (Throwable e) {
			Log.d("MyApp", e.getMessage(), e);
		}
		return data;
	}

	@Override
	protected JSONObject getMovieDataUrl() {
		JSONObject data = new JSONObject();
		try {
			data.put("mid", MessageId.GET_VIDEO_STREAM_FILE);
			data.put("sessionKey", UserEntity.getInstant().getSessionKey());
			data.put("username", UserEntity.getInstant().getUserName());
			data.put("filmId", videoInfo.id);
			data.put("seq", videoInfo.seq);
		} catch (Throwable e) {
			Log.d("MyApp", e.getMessage(), e);
		}
		return data;
	}

	@Override
	protected void playMovie() {
		try {
			Intent i = new Intent(getActivity(), PlayerView.class);
			i.putExtra("movieUrl", mSubtitles.movieUrl);
			i.putExtra("subUrl", mSubtitles.subUrl);
			startActivity(i);
		} catch (Throwable e) {
			Log.e("MyApp", e.getMessage(), e);
		}
	}

	@Override
	protected void playTrailer() {
		try {
			Intent intent = new Intent(getActivity(), TrailerActivity.class);
			intent.putExtra(TrailerActivity.TRAILER_URL_KEY, videoInfo.trailer);
			startActivity(intent);
		} catch (Throwable e) {
			Log.e("MyApp", e.getMessage(), e);
		}
	}
}