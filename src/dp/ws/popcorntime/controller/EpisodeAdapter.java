package dp.ws.popcorntime.controller;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.model.videoinfo.Episode;

@SuppressWarnings("unused")
public class EpisodeAdapter extends ArrayAdapter<Episode> {

	private int mPosition = 0;
	private Activity context;
	private List<Episode> data;

	public EpisodeAdapter(Activity context, int resource, List<Episode> data) {
		super(context, resource, data);
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Episode getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(R.layout.item_list_episode, parent, false);
		}
		Episode episode = data.get(position);
		TextView textView = (TextView) row.findViewById(R.id.episode_name);
		textView.setText(episode.getName());

		return row;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(R.layout.item_list_episode, parent, false);
		}
		Episode episode = data.get(position);
		TextView textView = (TextView) row.findViewById(R.id.episode_name);
		textView.setText(episode.getName());

		return row;
	}

	public void setSelectedItem(int position) {
		mPosition = position;
	}

	public void replaceData(List<Episode> data) {
		this.data = data;
		mPosition = 0;
		notifyDataSetChanged();
	}
}