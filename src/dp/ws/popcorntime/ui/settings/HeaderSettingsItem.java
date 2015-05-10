package dp.ws.popcorntime.ui.settings;

import dp.ws.popcorntime.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

class HeaderSettingsItem extends BaseSettingsItem {

	private int titleTextId;

	public HeaderSettingsItem(int titleTextId) {
		this.titleTextId = titleTextId;
	}

	@Override
	public void inflate(LayoutInflater inflater, ViewGroup parent) {
		itemView = inflater.inflate(R.layout.item_settings_header, parent, false);
	}

	@Override
	public void update() {
		((TextView) itemView).setText(titleTextId);
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}