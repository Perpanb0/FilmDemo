package dp.ws.popcorntime.ui.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

abstract class BaseSettingsItem {

	protected View itemView;

	public abstract void inflate(LayoutInflater inflater, ViewGroup parent);

	public abstract void update();

	public abstract boolean isEnabled();

	public View getView() {
		return itemView;
	}

	public void onClick() {

	}

}