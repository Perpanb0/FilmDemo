package dp.ws.popcorntime.controller;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.model.videodata.VideoData;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GenreAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private VideoData videoData;
	private boolean active = false;

	public GenreAdapter(Context context) {
		this(context, null);
	}

	public GenreAdapter(Context context, VideoData videoData) {
		this.videoData = videoData;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void replaceData(VideoData videoData) {
		this.videoData = videoData;
		this.active = true;
		notifyDataSetChanged();
	}

	public void inactive() {
		this.active = false;
		notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {
		if (videoData != null) {
			return videoData.getLocaleGenres().size();
		}

		return 0;
	}

	@Override
	public String getItem(int position) {
		if (videoData != null) {
			return videoData.getLocaleGenres().get(position).getCatName();
		}
		return "";
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GenreHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_genre, parent,
					false);
			holder = new GenreHolder();
			holder.name = (TextView) convertView.findViewById(R.id.genre_name);
			convertView.setTag(holder);
		} else {
			holder = (GenreHolder) convertView.getTag();
		}

		String name = getItem(position);
		if (active && videoData.getGenrePosition() == position) {
			holder.name.setText(Html.fromHtml("<b>" + name + "</b>"));
		} else {
			holder.name.setText(name);
		}

		return convertView;
	}

	private class GenreHolder {
		public TextView name;
	}

}