package dp.ws.popcorntime.database;

import dp.ws.popcorntime.database.tables.Downloads;
import dp.ws.popcorntime.database.tables.Favorites;
import dp.ws.popcorntime.database.tables.Tables;
import dp.ws.popcorntime.utils.Logger;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String NAME = "popcorn.db";
	private static final int VERSION_1 = 1001;
	private static final int VERSION_2 = 1002;
	private static final int VERSION_3 = 1003;
	private static final int CURRENT_VERSION = VERSION_3;

	public DBHelper(Context context) {
		super(context, NAME, null, CURRENT_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Favorites.QUERY_CREATE);
		db.execSQL(Downloads.QUERY_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (VERSION_1 == oldVersion) {
			upgradeVersion_1(db, newVersion);
		} else if (VERSION_2 == oldVersion) {
			upgradeVersion_2(db, newVersion);
		}
	}

	/*
	 * Upgrade version: 1
	 */

	private void upgradeVersion_1(SQLiteDatabase db, int newVersion) {
		if (VERSION_2 == newVersion) {
			upgradeVersion_1_to_2(db);
		} else if (VERSION_3 == newVersion) {
			upgradeVersion_1_to_3(db);
		}
	}

	private void upgradeVersion_1_to_2(SQLiteDatabase db) {
		db.execSQL(Downloads.QUERY_CREATE);
		Logger.debug("upgradeVersion: " + VERSION_1 + " to " + VERSION_2);
	}

	private void upgradeVersion_1_to_3(SQLiteDatabase db) {
		db.execSQL(Downloads.QUERY_CREATE);
		Logger.debug("upgradeVersion: " + VERSION_1 + " to " + VERSION_3);
	}

	/*
	 * Upgrade version: 2
	 */

	private void upgradeVersion_2(SQLiteDatabase db, int newVersion) {
		if (VERSION_3 == newVersion) {
			upgradeVersion_2_to_3(db);
		}
	}

	private void upgradeVersion_2_to_3(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + Tables.DOWNLOADS + " ADD COLUMN " + Downloads._TORRENT_MAGNET + " TEXT");
		Logger.debug("upgradeVersion: " + VERSION_2 + " to " + VERSION_3);
	}
}
