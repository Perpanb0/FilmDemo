package dp.ws.popcorntime.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dp.ws.popcorntime.model.videoinfo.VideoInfo;

public class JSONHelper {

	public static String getString(JSONObject obj, String key,
			String defaultValue) {
		if (obj != null && obj.has(key)) {
			try {
				return obj.getString(key);
			} catch (JSONException e) {
				// e.printStackTrace();
			}
		}
		return defaultValue;
	}

	public static ArrayList<VideoInfo> parseMovies(String json)
			throws Exception {
		ArrayList<VideoInfo> info = new ArrayList<VideoInfo>();

		JSONArray videos = new JSONObject(json).getJSONArray("Films");
		for (int i = 0; i < videos.length(); i++) {
			JSONObject video = videos.getJSONObject(i);
			VideoInfo movieInfo = new VideoInfo();
			movieInfo.populate(video);
			info.add(movieInfo);
		}

		return info;
	}
}