package dp.ws.popcorntime.ui.settings;

import dp.ws.popcorntime.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

class ChooserSettingsItem extends BaseSettingsItem {

	public interface ChooserItemListener {

		public CharSequence getTitle();

		public CharSequence getSummary();

		public CharSequence[] getItems();

		public int getItemPosition();

		public void onItemChoose(int position);

	}

	private FragmentManager fragmentManager;
	private ChooserItemListener listener;

	private TextView title;
	private TextView summary;

	private ChooserDialog chooserDialog;

	public ChooserSettingsItem(FragmentManager fragmentManager, ChooserItemListener listener) {
		this.fragmentManager = fragmentManager;
		this.listener = listener;
	}

	@Override
	public void inflate(LayoutInflater inflater, ViewGroup parent) {
		itemView = inflater.inflate(R.layout.item_settings_chooser, parent, false);
		title = (TextView) itemView.findViewById(R.id.item_settings_title);
		summary = (TextView) itemView.findViewById(R.id.item_settings_summary);
	}

	@Override
	public void update() {
		if (listener != null) {
			title.setText(listener.getTitle());
			summary.setText(listener.getSummary());
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void onClick() {
		showChooserDialog();
	}

	private void showChooserDialog() {
		if (listener == null) {
			return;
		}
		if (chooserDialog == null) {
			chooserDialog = new ChooserDialog();
		}
		if (!chooserDialog.isAdded()) {
			chooserDialog.show(fragmentManager, "chooser_dialog_" + this.hashCode());
		}
	}

	public class ChooserDialog extends DialogFragment {

		public ChooserDialog() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(listener.getTitle());
			builder.setSingleChoiceItems(listener.getItems(), listener.getItemPosition(), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null) {
						listener.onItemChoose(which);
						update();
					}
					dialog.dismiss();
				}
			});
			builder.setNeutralButton(R.string.cancel, null);

			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			final ListView list = dialog.getListView();
			if (list != null) {
				list.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
			}

			return dialog;
		}
	}

}
