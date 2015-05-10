package dp.ws.popcorntime.ui.settings;

import dp.ws.popcorntime.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

class CheckerSettingsItem extends BaseSettingsItem {

	public interface CheckerItemListener {

		public CharSequence getTitle();

		public CharSequence getSummary();

		public boolean isChecked();

		public void onItemChecked(boolean isChecked);

	}

	private CheckerItemListener listener;

	private TextView title;
	private TextView summary;
	private CheckBox checkBox;

	public CheckerSettingsItem(CheckerItemListener listener) {
		this.listener = listener;
	}

	@Override
	public void inflate(LayoutInflater inflater, ViewGroup parent) {
		itemView = inflater.inflate(R.layout.item_settings_checker, parent, false);
		title = (TextView) itemView.findViewById(R.id.item_settings_title);
		summary = (TextView) itemView.findViewById(R.id.item_settings_summary);
		checkBox = (CheckBox) itemView.findViewById(R.id.item_settings_checkbox);
		checkBox.setOnCheckedChangeListener(checkboxListener);
	}

	@Override
	public void update() {
		if (listener != null) {
			title.setText(listener.getTitle());
			summary.setText(listener.getSummary());
			boolean isChecked = listener.isChecked();
			if (isChecked != checkBox.isChecked()) {
				checkBox.setChecked(isChecked);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void onClick() {
		checkBox.setChecked(!checkBox.isChecked());
	}

	private OnCheckedChangeListener checkboxListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (listener != null) {
				listener.onItemChecked(isChecked);
			}
			update();
		}

	};

}