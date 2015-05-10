package dp.ws.popcorntime.controller;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.database.tables.Favorites;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import android.app.Activity;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class FavoritesListener implements OnLongClickListener {

	private Activity activity;
	private VideoInfo info;

	public FavoritesListener(Activity activity, VideoInfo info) {
		this.activity = activity;
		this.info = info;
	}

	@Override
	public boolean onLongClick(View v) {
		PopupMenu popup = new PopupMenu(activity, v);
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.favorites_remove:
					Favorites.delete(activity, info);
					return true;
				case R.id.favorites_add:
					Favorites.insert(activity, info);
					return true;
				default:
					return false;
				}
			}
		});

		popup.inflate(R.menu.popup_favorites);
		popup.show();
		Cursor cursor = Favorites.query(activity, null, Favorites._IMDB + "=\"" + info.imdb + "\"", null, null);
		if (cursor != null && cursor.getCount() > 0) {
			popup.getMenu().findItem(R.id.favorites_add).setVisible(false);
		} else {
			popup.getMenu().findItem(R.id.favorites_remove).setVisible(false);
		}
		cursor.close();
		return true;
	}

}