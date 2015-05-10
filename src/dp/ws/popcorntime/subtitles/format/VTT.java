package dp.ws.popcorntime.subtitles.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import android.text.TextUtils;
import android.util.Log;

public class VTT extends Format {

	public static boolean convert(String srcPath, String destPath) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(srcPath));
			BufferedWriter writer = new BufferedWriter(new FileWriter(destPath));

			writer.write("WEBVTT\n");
			writer.newLine();

			reader.mark(MARK);
			if (65279 == reader.read()) {
				Log.w("tag", "VTT: UTF-8 with BOM!");
			} else {
				reader.reset();
			}

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.contains("-->")) {
					writer.write(line.replaceAll(",", "."));
					writer.newLine();
					readVttCue(reader, writer);
				}
			}

			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void readVttCue(BufferedReader reader, BufferedWriter writer) throws Exception {
		while (true) {
			String _text = reader.readLine();
			if (TextUtils.isEmpty(_text)) {
				break;
			} else {
				_text = _text.trim().replaceAll("</*font[^>]*>", "");
				writer.write(_text);
				writer.newLine();
			}
		}
		writer.newLine();
	}
}
