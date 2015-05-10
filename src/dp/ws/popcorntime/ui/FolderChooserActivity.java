package dp.ws.popcorntime.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.ui.base.PopcornBaseActivity;
import dp.ws.popcorntime.utils.StorageHelper;

public class FolderChooserActivity extends PopcornBaseActivity implements FileFilter {

	public static final String SELECTED_DIR = "selected_dir";

	private TextView mSelectedFolder;
	private ListView mFolderList;
	private Button mCancel;
	private Button mConfirm;

	private ArrayAdapter<String> mFolderAdapter;
	private ArrayList<String> mFilenames;
	private File mSelectedDir;
	private File[] mFilesInDir;
	private FileObserver mFileObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		// Header
		getPopcornLogoView().setVisibility(View.GONE);
		View header = setPopcornHeaderView(R.layout.header_folder_chooser);
		header.findViewById(R.id.popcorn_action_upfolder).setOnClickListener(mUpListener);
		mSelectedFolder = (TextView) header.findViewById(R.id.folder_chooser_selected_folder);

		// Content
		View content = setPopcornContentView(R.layout.activity_folder_chooser);

		mFolderList = (ListView) content.findViewById(R.id.folder_chooser_list);
		mFolderList.setOnItemClickListener(mFolderListener);
		mFilenames = new ArrayList<String>();
		mFolderAdapter = new ArrayAdapter<String>(FolderChooserActivity.this, android.R.layout.simple_list_item_1, mFilenames);
		mFolderList.setAdapter(mFolderAdapter);

		mCancel = (Button) content.findViewById(R.id.folder_chooser_cancel);
		mCancel.setOnClickListener(mCancelListener);

		mConfirm = (Button) content.findViewById(R.id.folder_chooser_confirm);
		mConfirm.setOnClickListener(mConfirmListener);

		final File initialDir;
		String initialDirPath = StorageHelper.getInstance().getRootDirectoryPath();
		if (TextUtils.isEmpty(initialDirPath)) {
			initialDir = new File("/");
		} else {
			initialDirPath = initialDirPath.substring(0, initialDirPath.lastIndexOf("/"));
			initialDir = new File(initialDirPath);
		}

		changeDirectory(initialDir);

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		mCancel.setText(R.string.cancel);
		mConfirm.setText(R.string.confirm);
	}

	@Override
	public boolean accept(File pathname) {
		if (!pathname.isHidden()) {
			if (pathname.isDirectory() && !StorageHelper.ROOT_FOLDER_NAME.equals(pathname.getName())) {
				return true;
			}
		}
		return false;
	}

	private void changeDirectory(File dir) {
		if (dir != null && dir.isDirectory()) {
			File[] contents = dir.listFiles(FolderChooserActivity.this);
			if (contents != null) {
				Arrays.sort(contents, file—omparator);
				mFilesInDir = contents;
				mFilenames.clear();
				for (File f : mFilesInDir) {
					mFilenames.add(f.getName());
				}

				mSelectedDir = dir;
				mSelectedFolder.setText(dir.getAbsolutePath());
				mFolderAdapter.notifyDataSetChanged();
				mFileObserver = createFileObserver(dir.getAbsolutePath());
				mFileObserver.startWatching();
				mFolderList.post(new Runnable() {

					@Override
					public void run() {
						mFolderList.setSelection(0);
					}
				});
			}
		}

		if (mSelectedDir != null) {
			mConfirm.setEnabled(isValidFile(mSelectedDir));
		}
	}

	private boolean isValidFile(File file) {
		return (file != null && file.isDirectory() && file.canRead() && file.canWrite());
	}

	private FileObserver createFileObserver(String path) {
		return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

			@Override
			public void onEvent(int event, String path) {
				final Activity activity = FolderChooserActivity.this;

				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							refreshDirectory();
						}
					});
				}
			}
		};
	}

	private void refreshDirectory() {
		if (mSelectedDir != null) {
			changeDirectory(mSelectedDir);
		}
	}

	private boolean createDirectory() {
		int text_id = -1;
		if (mSelectedDir != null && mSelectedDir.canWrite()) {
			File newDir = new File(mSelectedDir, StorageHelper.ROOT_FOLDER_NAME);
			if (!newDir.exists()) {
				boolean result = newDir.mkdir();
				if (result) {
					mSelectedDir = newDir;
				} else {
					text_id = R.string.no_write_access;
				}
			} else {
				mSelectedDir = newDir;
			}
		} else if (mSelectedDir != null && !mSelectedDir.canWrite()) {
			text_id = R.string.no_write_access;
		} else {
			text_id = R.string.no_write_access;
		}

		if (-1 != text_id) {
			Toast.makeText(FolderChooserActivity.this, R.string.no_write_access, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}

	private Comparator<File> file—omparator = new Comparator<File>() {

		@Override
		public int compare(File lhs, File rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	};

	private OnClickListener mUpListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			File parent;
			if (mSelectedDir != null && (parent = mSelectedDir.getParentFile()) != null) {
				changeDirectory(parent);
			}
		}
	};

	private OnItemClickListener mFolderListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			if (mFilesInDir != null && position >= 0 && position < mFilesInDir.length) {
				changeDirectory(mFilesInDir[position]);
			}
		}
	};

	private OnClickListener mCancelListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener mConfirmListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (createDirectory()) {
				final Intent intent = new Intent();
				intent.putExtra(SELECTED_DIR, mSelectedDir.getAbsolutePath());
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	};

}