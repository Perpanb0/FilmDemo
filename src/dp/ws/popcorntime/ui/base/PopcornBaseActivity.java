package dp.ws.popcorntime.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.ui.locale.LocaleFragmentActivity;
import dp.ws.popcorntime.ui.widget.BlockTouchFrameLayout;
import dp.ws.popcorntime.updater.Updater;

public abstract class PopcornBaseActivity extends LocaleFragmentActivity {

	private ImageView logo;
	private BlockTouchFrameLayout header;
	private View headerLine;
	private BlockTouchFrameLayout content;
	private FrameLayout splash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		logo = (ImageView) findViewById(R.id.popcorn_logo);
		header = (BlockTouchFrameLayout) findViewById(R.id.popcorn_header);
		headerLine = findViewById(R.id.popcorn_header_line);
		content = (BlockTouchFrameLayout) findViewById(R.id.popcorn_content);
		splash = (FrameLayout) findViewById(R.id.popcorn_splash);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Updater.getInstance().setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Updater.getInstance().setCurrentActivity(null);
	}

	public ImageView getPopcornLogoView() {
		return logo;
	}

	public View setPopcornHeaderView(int layoutResID) {
		LayoutInflater inflater = LayoutInflater.from(PopcornBaseActivity.this);
		View view = inflater.inflate(layoutResID, null, false);
		header.addView(view);
		return view;
	}

	public View setPopcornContentView(int layoutResID) {
		LayoutInflater inflater = LayoutInflater.from(PopcornBaseActivity.this);
		View view = inflater.inflate(layoutResID, null, false);
		content.addView(view);
		return view;
	}

	public void setPopcornContentBackgroundResource(int resid) {
		content.setBackgroundResource(resid);
	}

	public View setPopcornSplashView(int layoutResID) {
		LayoutInflater inflater = LayoutInflater.from(PopcornBaseActivity.this);
		View view = inflater.inflate(layoutResID, null, false);
		splash.addView(view);
		return view;
	}

	public void setPopcornHeaderVisibility(boolean visibility) {
		if (visibility) {
			header.setVisibility(View.VISIBLE);
			headerLine.setVisibility(View.VISIBLE);
			logo.setVisibility(View.VISIBLE);
		} else {
			header.setVisibility(View.GONE);
			headerLine.setVisibility(View.GONE);
			logo.setVisibility(View.GONE);
		}
	}

	public void setPopcornSplashVisible(boolean visibility) {
		if (visibility) {
			splash.setVisibility(View.VISIBLE);
			header.setBlockTouchEvent(true);
			content.setBlockTouchEvent(true);
		} else {
			splash.setVisibility(View.GONE);
			header.setBlockTouchEvent(false);
			content.setBlockTouchEvent(false);
		}
	}

	public boolean isPopcornSplashVisible() {
		if (View.VISIBLE == splash.getVisibility()) {
			return true;
		}

		return false;
	}
}