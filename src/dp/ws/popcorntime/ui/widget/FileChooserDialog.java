package dp.ws.popcorntime.ui.widget;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.utils.StorageHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FileChooserDialog extends DialogFragment implements FileFilter {

	public interface OnChooserListener {

		public void onChooserSelected(File file);

		public void onChooserCancel();
	}

	private class ChooserData {

		public ChooserData(ChooserFileType type, File file) {
			this.type = type;
			this.file = file;
		}

		public ChooserFileType type;
		public File file;

	}

	private enum ChooserFileType {
		DIRECTORY, FILE, UP_ACTION
	}

	private class ChooserAdapter extends BaseAdapter {

		private Context context;
		private LayoutInflater inflater;
		private ArrayList<ChooserData> data;

		public ChooserAdapter(Context context) {
			this.context = context;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.data = new ArrayList<ChooserData>();
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public ChooserData getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ChooserData chooserData = getItem(position);
			if (convertView == null) {
				convertView = inflater.inflate(
						android.R.layout.simple_list_item_1, parent, false);
			}

			TextView view = (TextView) convertView;
			view.setText(chooserData.file.getName());
			if (ChooserFileType.DIRECTORY.equals(chooserData.type)) {
				view.setTextColor(context.getResources().getColor(
						android.R.color.white));
			} else if (ChooserFileType.FILE.equals(chooserData.type)) {
				view.setTextColor(context.getResources().getColor(
						android.R.color.holo_blue_light));
			} else if (ChooserFileType.UP_ACTION.equals(chooserData.type)) {
				view.setTextColor(context.getResources().getColor(
						android.R.color.white));
			}

			return view;
		}

		public ArrayList<ChooserData> getData() {
			return data;
		}

	}

	public static final String TITLE_KEY = "chooser-title";

	private final String DEFAULT_TITLE = "File Chooser";

	private File initDir;
	private String[] extensions;
	private OnChooserListener listener;

	private LayoutInflater inflater;
	private ChooserAdapter folderAdapter;
	private File selectedFolder;
	private File[] files;

	private TextView vCurrentFolder;
	private ListView vFolderList;

	public FileChooserDialog() {
		this(new File("/sdcard"), null);
	}

	public FileChooserDialog(String[] extensions) {
		this(new File("/sdcard"), extensions);
	}

	public FileChooserDialog(File initDir, String[] extensions) {
		this.initDir = initDir;
		this.extensions = extensions;
	}

	public void setChooserListener(OnChooserListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		inflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		folderAdapter = new ChooserAdapter(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (getArguments() != null && getArguments().containsKey(TITLE_KEY)) {
			builder.setTitle(getArguments().getString(TITLE_KEY));
		} else {
			builder.setTitle(DEFAULT_TITLE);
		}
		builder.setNeutralButton(R.string.cancel, cancelListener);
		builder.setView(createView());
		changeDirectory(initDir);

		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (listener != null) {
			listener.onChooserCancel();
		}
	}

	@Override
	public boolean accept(File pathname) {
		if (!pathname.isHidden()) {
			if (pathname.isDirectory()) {
				if (!StorageHelper.ROOT_FOLDER_NAME.equals(pathname.getName())) {
					return true;
				}
			} else {
				if (extensions == null || extensions.length == 0) {
					return true;
				} else {
					for (String ext : extensions) {
						if (pathname.getName().endsWith(ext)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private View createView() {
		LinearLayout parent = new LinearLayout(getActivity());
		LinearLayout.LayoutParams parent_params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		parent.setLayoutParams(parent_params);
		parent.setOrientation(LinearLayout.VERTICAL);

		vCurrentFolder = (TextView) inflater.inflate(
				android.R.layout.simple_list_item_1, parent, false);
		vCurrentFolder.setTextColor(getResources().getColor(
				android.R.color.holo_blue_light));
		parent.addView(vCurrentFolder);

		vFolderList = new ListView(getActivity());
		vFolderList.setOnItemClickListener(folderListener);
		vFolderList.setAdapter(folderAdapter);
		vFolderList.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		parent.addView(vFolderList, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		return parent;
	}

	private void changeDirectory(File dir) {
		if (dir != null && dir.isDirectory()) {
			selectedFolder = dir;
			File[] file_list = selectedFolder.listFiles(FileChooserDialog.this);
			if (file_list != null) {
				Arrays.sort(file_list, file—omparator);
				files = file_list;
				folderAdapter.getData().clear();
				if (selectedFolder.getParentFile() != null) {
					folderAdapter.getData().add(
							new ChooserData(ChooserFileType.UP_ACTION,
									new File("...")));
				}
				for (File f : files) {
					if (f.isDirectory()) {
						folderAdapter.getData().add(
								new ChooserData(ChooserFileType.DIRECTORY, f));
					} else {
						folderAdapter.getData().add(
								new ChooserData(ChooserFileType.FILE, f));
					}
				}

				folderAdapter.notifyDataSetChanged();
				vCurrentFolder.setText(selectedFolder.getAbsolutePath());
				vFolderList.post(new Runnable() {

					@Override
					public void run() {
						vFolderList.setSelection(0);
					}
				});
			}
		}
	}

	private void upFolder() {
		File parent;
		if (selectedFolder != null
				&& (parent = selectedFolder.getParentFile()) != null) {
			changeDirectory(parent);
		}
	}

	private Comparator<File> file—omparator = new Comparator<File>() {

		@Override
		public int compare(File lhs, File rhs) {
			if (lhs.isDirectory()) {
				if (rhs.isDirectory()) {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				} else {
					return -1;
				}
			} else {
				if (rhs.isDirectory()) {
					return 1;
				} else {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			}
		}
	};

	private OnItemClickListener folderListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			ChooserData chooserData = (ChooserData) adapter
					.getItemAtPosition(position);
			if (ChooserFileType.DIRECTORY.equals(chooserData.type)) {
				changeDirectory(chooserData.file);
			} else if (ChooserFileType.FILE.equals(chooserData.type)) {
				if (listener != null) {
					listener.onChooserSelected(chooserData.file);
					getDialog().dismiss();
				}
			} else if (ChooserFileType.UP_ACTION.equals(chooserData.type)) {
				upFolder();
			}
		}

	};

	private OnClickListener cancelListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (listener != null) {
				listener.onChooserCancel();
			}
		}

	};

}