package dp.ws.popcorntime.database.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import dp.ws.popcorntime.database.DBProvider;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;

public class Favorites implements BaseColumns {

	public static final String _TITLE = "_title";
	public static final String _YEAR = "_year";
	public static final String _RATING = "_rating";
	public static final String _IMDB = "_imdb";
	public static final String _ACTORS = "_actors";
	public static final String _TRAILER = "_trailer";
	public static final String _DESCRIPTION = "_description";
	public static final String _POSTER_MEDIUM_URL = "_poster_medium_url";
	public static final String _POSTER_BIG_URL = "_poster_big_url";
	public static final String _TORRENTS_INFO = "_torrents_info";

	private static final String NAME = Tables.FAVORITES;
	public static final Uri CONTENT_URI = DBProvider.BASE_CONTENT_URI
			.buildUpon().appendPath(NAME).build();
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.popcorn."
			+ NAME;
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.popcorn."
			+ NAME;

	public static final String QUERY_CREATE = "CREATE TABLE " + NAME + " ("
			+ _ID + " INTEGER PRIMARY KEY, " + _TITLE + " TEXT, " + _YEAR
			+ " TEXT, " + _RATING + " REAL, " + _IMDB + " TEXT, " + _ACTORS
			+ " TEXT, " + _TRAILER + " TEXT, " + _DESCRIPTION + " TEXT, "
			+ _POSTER_MEDIUM_URL + " TEXT, " + _POSTER_BIG_URL + " TEXT, "
			+ _TORRENTS_INFO + " TEXT, " + "UNIQUE (" + _IMDB
			+ ") ON CONFLICT REPLACE)";

	public static String QUERY_DROP = "DROP TABLE IF EXISTS " + NAME;

	public static Uri buildUri(long id) {
		return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
	}

	public static Cursor query(Context context, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		return context.getContentResolver().query(CONTENT_URI, projection,
				selection, selectionArgs, sortOrder);
	}

	public static Uri insert(Context context, VideoInfo info) {
		return context.getContentResolver().insert(CONTENT_URI,
				buildValues(info));
	}

	public static int update(Context context, VideoInfo info) {
		return context.getContentResolver().update(CONTENT_URI,
				buildValues(info), _IMDB + "=\"" + info.imdb + "\"", null);
	}

	public static int delete(Context context, VideoInfo info) {
		return context.getContentResolver().delete(CONTENT_URI,
				_IMDB + "=\"" + info.imdb + "\"", null);
	}

	private static ContentValues buildValues(VideoInfo info) {
		ContentValues values = new ContentValues();
		values.put(_TITLE, info.title);
		values.put(_YEAR, info.year);
		values.put(_RATING, info.rating);
		values.put(_IMDB, info.imdb);
		values.put(_ACTORS, info.actors);
		values.put(_TRAILER, info.trailer);
		values.put(_DESCRIPTION, info.description);
		values.put(_POSTER_MEDIUM_URL, info.posterMediumUrl);
		values.put(_POSTER_BIG_URL, info.posterBigUrl);
		return values;
	}
}