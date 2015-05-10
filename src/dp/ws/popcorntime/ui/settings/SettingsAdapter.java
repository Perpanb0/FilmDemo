package dp.ws.popcorntime.ui.settings;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class SettingsAdapter extends BaseAdapter {

	private ArrayList<BaseSettingsItem> data = new ArrayList<BaseSettingsItem>();
	private LayoutInflater inflater;

	public SettingsAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public BaseSettingsItem getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BaseSettingsItem item = getItem(position);
		if (item.getView() == null) {
			item.inflate(inflater, parent);
		}

		item.update();

		return item.getView();
	}

	@Override
	public boolean isEnabled(int position) {
		return getItem(position).isEnabled();
	}

	public void add(BaseSettingsItem item) {
		data.add(item);
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}
}