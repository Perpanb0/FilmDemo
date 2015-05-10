package dp.ws.popcorntime.subtitles;

public interface SubtitleListener {

	public void onSubtitleLoadSucces(int id, String data);

	public void onSubtitleLoadError(String message);
}
