package dp.ws.popcorntime.controller;

import java.util.ArrayList;
import java.util.List;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.model.videoinfo.Episode;
import dp.ws.popcorntime.model.videoinfo.Season;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressWarnings("unused")
public class SeasonAdapter extends ArrayAdapter<Season> {

	private int mPosition = 0;
	private Activity context;
	private List<Season> data;

	public SeasonAdapter(Activity context, int resource, List<Season> data) {
		super(context, resource, data);
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Season getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(R.layout.item_list_season, parent, false);
		}
		Season season = data.get(position);
		TextView textView = (TextView) row.findViewById(R.id.season_name);
		textView.setText(season.getName());

		return row;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(R.layout.item_list_season, parent, false);
		}
		Season season = data.get(position);
		TextView textView = (TextView) row.findViewById(R.id.season_name);
		textView.setText(season.getName());

		return row;
	}

	public void setSelectedItem(int position) {
		mPosition = position;
	}

	public void replaceData(List<Season> data) {
		this.data = data;
		mPosition = 0;
		notifyDataSetChanged();
	}
}