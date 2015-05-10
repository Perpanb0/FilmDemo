package dp.ws.popcorntime.subtitles;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.universalchardet.UniversalDetector;

import vn.vovi.utils.MessageId;
import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.prefs.Prefs;
import dp.ws.popcorntime.prefs.SettingsPrefs;
import dp.ws.popcorntime.subtitles.format.SRT;
import dp.ws.popcorntime.utils.LanguageUtil;
import dp.ws.popcorntime.utils.Logger;

public class Subtitles {

	public static class FontSize {
		public static final float EXTRA_SMALL = 0.7f;
		public static final float SMALL = 0.85f;
		public static final float NORMAL = 1f;
		public static final float LARGE = 1.25f;
		public static final float EXTRA_LARGE = 1.5f;

		public static final int DEFAULT_POSITION = 2;
		public static final float[] SIZES = new float[] { EXTRA_SMALL, SMALL,
				NORMAL, LARGE, EXTRA_LARGE };
	}

	public static class FontColor {
		public static final String WHITE = "#ffffff";
		public static final String YELLOW = "#ffff00";

		public static final int DEFAULT_POSITION = 0;
		public static final String[] COLORS = new String[] { WHITE, YELLOW };
	}

	public static class Formats {
		public static final String SRT = "srt";
		public static final String VTT = "vtt";
	}

	public static final int DEFAULT_SUBS_COUNT = 2;

	public static final String WITHOUT_SUBS_URL = "video_without_subs";
	public static final String CUSTOM_SUBS_URL = "video_custom_subs";

	public static final int WITHOUT_SUBS_POSITION = 0;
	public static final int CUSTOM_SUBS_POSITION = 1;

	private static final String UTF_8 = "UTF-8";

	public int position = 0;
	public ArrayList<String> data = new ArrayList<String>();
	public ArrayList<String> urls = new ArrayList<String>();
	public String subUrl;
	public String movieUrl;
	private Activity activity;
	private SubtitleListener subtitleListener;

	public Subtitles(Activity activity) {
		this.activity = activity;
	}

	public void setSubtitleListener(SubtitleListener subtitleListener) {
		this.subtitleListener = subtitleListener;
	}

	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		return new URLLoader(activity, args);
	}

	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {
		switch (loader.getId()) {
		case MessageId.GET_VIDEO_STREAM_FILE:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					MessageId.GET_VIDEO_STREAM_FILE, response));
			break;
		case MessageId.GET_VIDEO_INFO:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					MessageId.GET_VIDEO_INFO, response));
			break;
		}
	}

	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	public String getUrl() {
		if (urls != null && urls.size() > position) {
			return urls.get(position);
		}

		return null;
	}

	public void parseMovie(JSONObject jsonSubtitles) throws JSONException {
		JSONObject jdata = jsonSubtitles.getJSONObject("data");
		movieUrl = jdata.getString("filmUrl");

		JSONArray jArr = jdata.getJSONArray("subs");
		for (int i = 0; i < jArr.length(); i++) {
			JSONObject jsO = jArr.getJSONObject(i);
			if (jsO.getString("code").equals("VIE")) {
				subUrl = jsO.getString("url");
			}
		}
		position = jArr.length() - 1;
	}

	public void parseTVShows(JSONObject jsonSubtitles) throws JSONException {
		int subtitlesCount = jsonSubtitles.getInt("subtitles");
		if (subtitlesCount > 0) {
			JSONObject subs = jsonSubtitles.getJSONObject("subs");
			Iterator<String> iter = subs.keys();
			String subLang = Prefs.getSettngs().getString(
					SettingsPrefs.SUBTITLE_LANGUAGE,
					LanguageUtil.DEFAULT_SUBTITLE_LOCALE);
			while (iter.hasNext()) {
				String key = iter.next();
				JSONArray subInfos = subs.getJSONArray(key);
				int subRating = Integer.MIN_VALUE;
				String subUrl = "";
				for (int i = 0; i < subInfos.length(); i++) {
					JSONObject subInfo = subInfos.getJSONObject(i);
					int rating = subInfo.getInt("rating");
					String format = subInfo.getString("format");
					if (rating > subRating && Formats.SRT.equals(format)) {
						subRating = rating;
						subUrl = subInfo.getString("url");
					}
				}
				if (!"".equals(subUrl)) {
					urls.add(subUrl);
					key = LanguageUtil.subtitleIsoToName(key);
					data.add(LanguageUtil.subtitleNameToNative(key));
					if (subLang.equals(key)) {
						position = data.size() - 1;
					}
				}
			}
		}
	}

	private Handler loaderHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			loaderFinished(msg.what, (LoaderResponse) msg.obj);
			removeMessages(msg.what);
		}
	};

	private void loaderFinished(int id, LoaderResponse response) {
		if (subtitleListener != null) {
			if (response.error == null) {
				subtitleListener.onSubtitleLoadSucces(id, response.data);
			} else {
				subtitleListener.onSubtitleLoadError(response.error);
			}
		}
	}

	public static float getFontScale(Context context) {
		int pos = Prefs.getSettngs().getInt(SettingsPrefs.SUBTITLE_FONT_SIZE,
				FontSize.DEFAULT_POSITION);
		if (pos < FontSize.SIZES.length) {
			return FontSize.SIZES[pos];
		} else {
			return FontSize.SIZES[FontSize.DEFAULT_POSITION];
		}
	}

	public static int getFontColor(Context context) {
		int pos = Prefs.getSettngs().getInt(SettingsPrefs.SUBTITLE_FONT_COLOR,
				FontColor.DEFAULT_POSITION);
		String color = FontColor.WHITE;
		if (pos < FontSize.SIZES.length) {
			color = FontColor.COLORS[pos];
		} else {
			color = FontColor.COLORS[FontColor.DEFAULT_POSITION];
		}
		return Color.parseColor(color);
	}

	public static void loadUrl(String url, String savePath) throws Exception {
		URLConnection connectionSubtitle = new URL(url).openConnection();
		connectionSubtitle.connect();
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
				connectionSubtitle.getInputStream()));

		ZipEntry zi = zis.getNextEntry();
		while (zi != null) {
			String[] part = zi.getName().split("\\.");
			if (part.length == 0) {
				zi = zis.getNextEntry();
				continue;
			}

			String extension = part[part.length - 1];
			if (Formats.SRT.equals(extension)) {
				writeSub(zis, savePath);
				break;
			} else {
				Logger.debug("Not supported subtitle extension: " + extension);
			}
			zi = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	public static void loadFile(String filePath, String savePath)
			throws Exception {
		FileInputStream fis = new FileInputStream(new File(filePath));
		writeSub(fis, savePath);
		fis.close();
	}

	private static void writeSub(InputStream inputStream, String savePath)
			throws Exception {
		UniversalDetector detector = new UniversalDetector(null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int count = 0;
		byte[] buffer = new byte[1024];
		while ((count = inputStream.read(buffer)) > 0) {
			if (!detector.isDone()) {
				detector.handleData(buffer, 0, count);
			}
			baos.write(buffer, 0, count);
		}
		detector.dataEnd();

		String subtitleEncoding = detector.getDetectedCharset();
		detector.reset();
		if (subtitleEncoding == null || "".equals(subtitleEncoding)) {
			subtitleEncoding = UTF_8;
		} else if ("MACCYRILLIC".equals(subtitleEncoding)) {
			subtitleEncoding = "Windows-1256"; // for arabic
		}

		byte[] subtitle_utf_8 = new String(baos.toByteArray(),
				Charset.forName(subtitleEncoding)).getBytes(UTF_8);
		String color = FontColor.COLORS[Prefs.getSettngs().getInt(
				SettingsPrefs.SUBTITLE_FONT_COLOR, FontColor.DEFAULT_POSITION)];
		String subtitle = SRT.convert(
				new String(subtitle_utf_8, Charset.forName(UTF_8)), color);
		FileUtils.write(new File(savePath), subtitle, Charset.forName(UTF_8));
	}
}
