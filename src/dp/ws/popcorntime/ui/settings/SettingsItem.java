package dp.ws.popcorntime.ui.settings;

import dp.ws.popcorntime.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

class SettingsItem extends BaseSettingsItem {

	public interface SettingItemListener {

		public CharSequence getTitle();

		public CharSequence getSummary();

		public boolean isEnabled();

		public void onItemClick();

	}

	private SettingItemListener listener;

	private TextView title;
	private TextView summary;

	public SettingsItem(SettingItemListener listener) {
		this.listener = listener;
	}

	@Override
	public void inflate(LayoutInflater inflater, ViewGroup parent) {
		itemView = inflater.inflate(R.layout.item_settings, parent, false);
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
		if (listener != null) {
			return listener.isEnabled();
		}
		return false;
	}

	@Override
	public void onClick() {
		if (listener != null) {
			listener.onItemClick();
		}
	}

}