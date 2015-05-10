package dp.ws.popcorntime.model.videodata;

import android.content.Context;
import dp.ws.popcorntime.R;

public class TVShowData extends VideoData {

	public TVShowData(Context context) {
		sort = Sort.SEEDS;
		format = Format.MP4;// + "," + Format.AVI + "," + Format.MKV;
		requestGenres = context.getResources().getStringArray(
				R.array.request_genres);
	}
}