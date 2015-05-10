package dp.ws.popcorntime.chromecast;

public interface PopcornCastListener {
	public void onCastConnection();

	public void onCastRouteSelected();

	public void onCastRouteUnselected(long position);

	public void onCastStatePlaying();

	public void onCastStatePaused();

	public void onCastStateIdle();

	public void onCastStateBuffering();

	public void onCastMediaLoadSuccess();

	public void onCastMediaLoadCancelInterrupt();

	public void teardown();
}