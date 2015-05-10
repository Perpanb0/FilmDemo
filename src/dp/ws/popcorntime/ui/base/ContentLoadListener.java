package dp.ws.popcorntime.ui.base;

public interface ContentLoadListener {
	public void showLoading();

	public void showError();

	public void showContent();

	public void retryLoad();
}