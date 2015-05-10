package dp.ws.popcorntime.ui.base;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.ui.locale.LocaleFragment;
import dp.ws.popcorntime.utils.StorageHelper;

public abstract class VideoBaseFragment extends LocaleFragment {

	private final int HANDLER_DOWNLOAD = 1;
	private final int HANDLER_WATCH_DOWNLOAD = 2;
	private final int HANDLER_WATCH = 3;

	private final int STARS_COUNT = 5;
	private final float MAX_RATING = 10;

	protected final float RATING_COEF = STARS_COUNT / MAX_RATING;
	protected File cacheDirectory;
	protected boolean isDownloads;

	// view
	protected ImageView poster;
	protected TextView title;
	protected TextView description;
	protected Button downloadOpenBtn;
	protected Button watchItNow;
	protected TextView trailerText;
	protected ImageButton trailer;

	private NoFreeSpaceDialog noFreeSpaceDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cacheDirectory = StorageHelper.getInstance().getCacheDirectory();
	}

	protected void populateView(View view) {
		poster = (ImageView) view.findViewById(R.id.video_poster);
		title = (TextView) view.findViewById(R.id.video_title);
		description = (TextView) view.findViewById(R.id.video_description);
		downloadOpenBtn = (Button) view.findViewById(R.id.video_download_open);
		watchItNow = (Button) view.findViewById(R.id.video_watchitnow);
		watchItNow.setOnClickListener(watchItNowListener);
		trailer = (ImageButton) view.findViewById(R.id.video_trailer);
		trailer.setOnClickListener(trailerListener);
		trailer.setOnTouchListener(trailerTouchListener);
		trailerText = (TextView) view.findViewById(R.id.video_trailer_text);
		trailerText.setOnClickListener(trailerListener);
		trailerText.setOnTouchListener(trailerTouchListener);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		watchItNow.setText(R.string.watch_it_now);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void showDownloadBtn() {
		downloadOpenBtn.setBackgroundResource(R.drawable.download_btn_selector);
		downloadOpenBtn.setText(R.string.download);
		downloadOpenBtn.setOnClickListener(downloadListener);
		downloadOpenBtn.setVisibility(View.VISIBLE);
	}

	protected void showOpenBtn() {
		downloadOpenBtn.setBackgroundResource(R.drawable.open_btn_selector);
		downloadOpenBtn.setText(R.string.open);
	}

	protected boolean checkFreeSpace(String path, long size) {
		long freeSpace = StorageHelper.getAvailableSpaceInBytes(path);
		if (freeSpace <= size) {
			showNoFreeSpaceDialog();
			return false;
		}
		return true;
	}

	protected abstract void playMovie();

	protected abstract void playTrailer();

	/*
	 * TODO: Listeners
	 */

	private OnClickListener downloadListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			handler.sendEmptyMessage(HANDLER_DOWNLOAD);
		}
	};

	private OnClickListener watchItNowListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			playMovie();
		}
	};

	private OnClickListener trailerListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			playTrailer();
		}
	};

	private OnTouchListener trailerTouchListener = new OnTouchListener() {

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			if (MotionEvent.ACTION_DOWN == action) {
				trailerTouch(v.getId(), true);
			} else if (MotionEvent.ACTION_UP == action) {
				trailerTouch(v.getId(), false);
			}

			return false;
		}
	};

	private void trailerTouch(int id, boolean isPressed) {
		if (R.id.video_trailer == id) {
			trailerText.setPressed(isPressed);
		} else if (R.id.video_trailer_text == id) {
			trailer.setPressed(isPressed);
		}
	}

	/*
	 * TODO: Handler
	 */

	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_WATCH_DOWNLOAD:
				handleWatchDownload();
				break;
			case HANDLER_WATCH:
				handleWatch();
				break;
			default:
				break;
			}
		}

	};

	protected boolean handleWatchDownload() {
		return true;
	}

	protected boolean handleWatch() {
		if (null == cacheDirectory) {
			Toast.makeText(getActivity(), R.string.cache_folder_not_selected,
					Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!cacheDirectory.exists()) {
			if (!cacheDirectory.mkdirs()) {
				Log.e("tag",
						"Cannot crate dir: " + cacheDirectory.getAbsolutePath());
				return false;
			}
		}

		return true;
	}

	/*
	 * TODO: Dialogs
	 */

	protected void showNoFreeSpaceDialog() {
		if (noFreeSpaceDialog == null) {
			noFreeSpaceDialog = new NoFreeSpaceDialog();
		}
		if (!noFreeSpaceDialog.isAdded()) {
			noFreeSpaceDialog
					.show(getFragmentManager(), "no_free_space_dialog");
		}
	}

	protected class NoFreeSpaceDialog extends DialogFragment {

		public NoFreeSpaceDialog() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.app_name);
			builder.setMessage(R.string.no_free_space);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							StorageHelper.getInstance().clearCacheDirectory();
						}
					});

			return builder.create();
		}
	}
}