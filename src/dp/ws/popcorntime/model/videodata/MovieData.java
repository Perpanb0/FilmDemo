package dp.ws.popcorntime.model.videodata;

import dp.ws.popcorntime.R;
import android.content.Context;

public class MovieData extends VideoData {

	public MovieData(Context context) {
		sort = Sort.SEEDS;
		format = Format.MP4;
		requestGenres = context.getResources().getStringArray(
				R.array.request_genres);
	}

}