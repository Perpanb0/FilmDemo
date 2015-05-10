package dp.ws.popcorntime.controller;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;

public class VideoAdapter extends BaseAdapter {

	private Activity activity;
	private List<VideoInfo> data;
	private LayoutInflater inflater;
	private DisplayImageOptions options;

	public VideoAdapter(Activity context, List<VideoInfo> data) {
		this.activity = context;
		this.data = data;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = new DisplayImageOptions.Builder().cacheInMemory(false)
				.cacheOnDisk(true).showImageOnLoading(R.drawable.poster)
				.showImageForEmptyUri(R.drawable.poster).build();
	}

	public VideoAdapter(Activity context) {
		this(context, new ArrayList<VideoInfo>());
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public VideoInfo getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		VideoHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_grid_video, parent,
					false);
			holder = new VideoHolder();
			holder.poster = (RoundedImageView) convertView
					.findViewById(R.id.video_poster);
			holder.name = (TextView) convertView.findViewById(R.id.video_name);
			holder.year = (TextView) convertView.findViewById(R.id.video_year);
			convertView.setTag(holder);
		} else {
			holder = (VideoHolder) convertView.getTag();
		}

		VideoInfo info = getItem(position);
		holder.poster.setOnClickListener(new VideoItemListener(activity, info));
		holder.poster.setOnLongClickListener(new FavoritesListener(activity,
				info));
		ImageLoader.getInstance().displayImage(info.posterMediumUrl,
				holder.poster, options);
		holder.name.setText(Html.fromHtml("<b>" + info.title + "</b>"));
		holder.year.setText(info.year);

		return convertView;
	}

	public void addData(List<VideoInfo> data) {
		this.data.addAll(data);
		notifyDataSetChanged();
	}
}
