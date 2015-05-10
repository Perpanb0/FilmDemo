package dp.ws.popcorntime.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SettingsView extends ListView {

	private SettingsAdapter adapter;

	public SettingsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SettingsView(Context context) {
		super(context);
	}

	public void setAdapter(SettingsAdapter adapter) {
		super.setAdapter(adapter);
		this.adapter = adapter;
		setOnItemClickListener(itemListener);
	}

	private OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			adapter.getItem(position).onClick();
		}
	};

}
