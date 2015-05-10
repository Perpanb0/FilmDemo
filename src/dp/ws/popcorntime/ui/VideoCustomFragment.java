package dp.ws.popcorntime.ui;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.database.tables.Downloads;
import dp.ws.popcorntime.model.DownloadInfo;
import dp.ws.popcorntime.ui.base.VideoBaseFragment;

public class VideoCustomFragment extends VideoBaseFragment {

	public static final String SCHEME_KEY = "scheme";
	public static final String PATH_KEY = "path";

	public static final String SCHEME_FILE = "file";

	private String scheme;
	private String torrentPath;

	private ArrayAdapter<String> mCustomAdapter;
	private String fileName;

	// view
	private Spinner customSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCustomAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_dropdown_item);
		mCustomAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		scheme = getArguments().getString(SCHEME_KEY);
		torrentPath = getArguments().getString(PATH_KEY);
		if (SCHEME_FILE.equals(scheme)) {
			torrentPath = torrentPath.replace("file://", "");
		} else {
			Log.e("tag", "Unsupported scheme: " + scheme);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_video_custom, container,
				false);
		populateView(view);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		mLocaleHelper.updateLocale();
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_video_custom, container);
		populateView(view);
	}

	@Override
	protected void populateView(View view) {
		Log.d("MyApp", "====================== populateView");
		super.populateView(view);
		customSpinner = (Spinner) view.findViewById(R.id.video_custom);

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		customSpinner.setPrompt("Files");
	}

	@Override
	protected boolean handleWatchDownload() {
		String selection = Downloads._FILE_NAME + "=\"" + fileName + "\"";
		Cursor cursor = Downloads.query(getActivity(), null, selection, null,
				null);
		if (cursor != null && cursor.getCount() == 1) {
			DownloadInfo info = new DownloadInfo();
			try {
				cursor.moveToFirst();
				info.populate(cursor);
				// VLCPlayerActivity.watch(getActivity(), new WatchInfo(info,
				// null, null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			isDownloads = false;
			showDownloadBtn();
		}

		return true;
	}

	@Override
	protected void playMovie() {

	}

	@Override
	protected void playTrailer() {

	}
}