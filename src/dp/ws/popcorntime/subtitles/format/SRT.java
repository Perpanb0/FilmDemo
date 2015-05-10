package dp.ws.popcorntime.subtitles.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import android.text.TextUtils;
import android.util.Log;

public class SRT extends Format {

	private static int index;

	public static String convert(String str, String color) {
		BufferedReader reader = new BufferedReader(new StringReader(str));
		StringBuilder builder = new StringBuilder();
		index = 1;
		try {
			reader.mark(MARK);
			if (65279 == reader.read()) {
				Log.w("tag", "SRT: UTF-8 with BOM!");
			} else {
				reader.reset();
			}

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.contains("-->")) {
					builder.append(index);
					builder.append("\n");
					index++;
					builder.append(line);
					builder.append("\n");
					readSrtCue(reader, builder, color);
				}
			}
		} catch (Exception ex) {
			Log.e("tag", "SRT convert: " + ex.getMessage());
			builder.setLength(0);
		}
		try {
			reader.close();
		} catch (IOException e) {
		}

		return builder.toString();
	}

	private static void readSrtCue(BufferedReader reader, StringBuilder builder, String color) throws Exception {
		String text = "";
		while (true) {
			String _text = reader.readLine();
			if (TextUtils.isEmpty(_text)) {
				break;
			} else {
				text += _text.trim() + "\n";
			}
		}

		if (text.contains("<font")) {
			text = text.replaceAll("<font[^>]*>", "<font color=\"" + color + "\">");
		} else {
			text = "<font color=\"" + color + "\">" + text.trim() + "</font>\n";
		}

		builder.append(text);
		builder.append("\n");
	}
}