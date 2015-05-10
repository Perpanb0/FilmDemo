package dp.ws.popcorntime.model.videodata;

import java.util.ArrayList;

import android.util.Log;

public abstract class VideoData {

	public static final String REQUEST_URL_KEY = "popcorntime_video_request";
	public static final String TYPE_KEY = "popcorntime_video_type";

	protected final String TORRENTAPI_URL = "http://api.torrentsapi.com/";

	protected StringBuilder sb = new StringBuilder();

	protected String type;
	protected String sort;
	protected String format;
	protected int page = 1;
	protected String catId = "30";
	protected String[] requestGenres;
	protected int currentGenrePosition = 0;
	protected String keywords;

	private ArrayList<CatInfo> localeGenres;

	public String getCatId() {
		return catId;
	}

	public String getCatId(int position) {
		if (localeGenres == null) {
			return "30";
		} else {
			try {
				CatInfo catInfo = localeGenres.get(position);
				return catInfo.getCatId();
			} catch (Throwable e) {
				Log.d("MyApp", e.getMessage(), e);
				return "30";
			}
		}
	}

	public void setCatId(String catId) {
		this.catId = catId;
	}

	public String getType() {
		return type;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setGenre(int position) {
		if (requestGenres != null
				&& (position >= 0 || position < requestGenres.length)) {
			currentGenrePosition = position;
		}
	}

	public void setLocaleGenres(ArrayList<CatInfo> cats) {
		localeGenres = cats;
	}

	public ArrayList<CatInfo> getLocaleGenres() {
		return localeGenres;
	}

	public int getGenrePosition() {
		return currentGenrePosition;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getKeywords() {
		return keywords;
	}

	public class Sort {
		public static final String SEEDS = "seeds";
	}

	public class Format {
		public static final String MP4 = "mp4";
		public static final String AVI = "avi";
		public static final String MKV = "mkv";
	}

	public class Quality {
		public static final String P_720 = "720p";
		public static final String P_1080 = "1080p";
	}
}