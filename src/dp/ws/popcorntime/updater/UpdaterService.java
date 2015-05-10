package dp.ws.popcorntime.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.telephony.TelephonyManager;
import android.util.Log;
import dp.ws.popcorntime.config.Configuration;
import dp.ws.popcorntime.prefs.PopcornPrefs;
import dp.ws.popcorntime.prefs.Prefs;
import dp.ws.popcorntime.utils.StorageHelper;

public class UpdaterService extends IntentService {

	public static final String RESULT_RECEIVER = "updater_result_receiver";
	public static final String NAME_OF_ACTION = "updeter_action";

	public static final int ACTION_CHECK = 101;

	private final int maxTries = 30;
	private DownloadManager downloadManager;
	private UpdateInfo updateInfo;
	private long downloadId;
	private File apkFile;
	private Uri apkUri;
	private boolean isCancelled;

	public UpdaterService() {
		super(UpdaterService.class.getName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		isCancelled = false;
		downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
	}

	@Override
	public void onDestroy() {
		isCancelled = true;
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.hasExtra(NAME_OF_ACTION)) {
			ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
			if (resultReceiver == null) {
				return;
			}
			int action = intent.getIntExtra(NAME_OF_ACTION, -1);
			switch (action) {
			case ACTION_CHECK:
				check(resultReceiver);
				break;
			default:
				break;
			}
		}
	}

	/*
	 * TODO: check
	 */

	private void check(ResultReceiver resultReceiver) {
		String versionRequestParams = generateVersionRequestParams();
		if ("".equals(versionRequestParams)) {
			return;
		}
		if (Configuration.UPDATE_DOMAINS == null || Configuration.UPDATE_DOMAINS.length == 0) {
			return;
		}
		int tryCount = 0;
		DefaultHttpClient client = new DefaultHttpClient();
		Random rnd = new Random();

		while (true) {
			tryCount += 1;
			String url = Configuration.UPDATE_DOMAINS[rnd.nextInt(Configuration.UPDATE_DOMAINS.length)] + "/?" + versionRequestParams;
			// Log.d("tag", "updater: " + url);
			try {
				HttpResponse httpResponse = client.execute(new HttpGet(url));
				HttpEntity entity = httpResponse.getEntity();
				String response = EntityUtils.toString(entity);
				updateInfo = parseUpdateInfo(response);
				break;
			} catch (Exception ex) {
				// ex.printStackTrace();
			}

			if (maxTries == tryCount) {
				return;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			if (isCancelled) {
				return;
			}
		}

		// check version
		String filePath = Prefs.getPopcorn().getString(PopcornPrefs.UPDATE_APK_PATH, "");
		if (updateInfo != null) { // new version
			Log.d("tag", "Have new version: " + updateInfo.version);
			apkFile = new File(StorageHelper.getDownloadFolderPath() + "/popcorntime_" + updateInfo.version + ".apk");
			if (apkFile.exists() && apkFile.getAbsolutePath().equals(filePath)) {
				apkUri = Uri.parse("file://" + apkFile.getAbsolutePath());
				downloadComplete(resultReceiver);
				return;
			} else if (apkFile.exists()) {
				apkFile.delete();
			} else {
				Prefs.getPopcorn().edit().putString(PopcornPrefs.UPDATE_APK_PATH, "").commit();
			}
		} else { // current version
			Log.d("tag", "Current version");
			if (!"".equals(filePath)) {
				apkFile = new File(filePath);
				if (apkFile.exists()) {
					apkFile.delete();
				}
				Prefs.getPopcorn().edit().putString(PopcornPrefs.UPDATE_APK_PATH, "").commit();
			}
			return;
		}

		if (isCancelled) {
			return;
		}

		// download
		apkUri = Uri.parse("file://" + apkFile.getAbsolutePath());
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updateInfo.downloadUrl));
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
		request.setDestinationUri(apkUri);
		request.setVisibleInDownloadsUi(false);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

		try {
			downloadId = downloadManager.enqueue(request);
		} catch (Exception ex) {
			downloadId = -1;
		}

		if (downloadId == -1) {
			download(resultReceiver, updateInfo.downloadUrl);
		} else {
			downloadManager(resultReceiver);
		}
	}

	private String generateVersionRequestParams() {
		String params;
		try {
			params = "app_id=T4P_AND";
			TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			params += "&hid=" + tManager.getDeviceId();
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			params += "&ver=" + pInfo.versionName;
			params += "&os=ANDROID" + android.os.Build.VERSION.RELEASE.replace(".", "");
		} catch (Exception e) {
			params = "";
		}
		return params;
	}

	private UpdateInfo parseUpdateInfo(String json) {
		UpdateInfo info = null;
		// Log.d("tag", "updater response: " + json);
		try {
			JSONObject jsonInfo = new JSONObject(json);
			if (!jsonInfo.isNull("downloadUrl")) {
				info = new UpdateInfo();
				info.downloadUrl = jsonInfo.getString("downloadUrl");
				info.version = jsonInfo.getString("version");
				if (!jsonInfo.isNull("size")) {
					info.size = jsonInfo.getLong("size");
				}
			}
		} catch (JSONException e) {
			info = null;
		}
		return info;
	}

	private void download(ResultReceiver resultReceiver, String downloadUrl) {
		InputStream input = null;
		OutputStream output = null;
		while (true) {
			try {
				URL url = new URL(downloadUrl);
				URLConnection connection = url.openConnection();
				connection.connect();
				input = new BufferedInputStream(connection.getInputStream());
				output = new FileOutputStream(apkFile);

				byte data[] = new byte[1024];
				int count;
				while ((count = input.read(data)) != -1) {
					if (isCancelled) {
						break;
					}
					if (count != 0) {
						output.write(data, 0, count);
					}
				}
				if (isCancelled) {
					if (apkFile.exists()) {
						apkFile.delete();
					}
					break;
				}
				output.flush();
				downloadComplete(resultReceiver);
				break;
			} catch (Exception e) {

			}
		}
		try {
			if (output != null) {
				output.close();
			}
			if (input != null) {
				input.close();
			}
		} catch (Exception ex) {

		}
	}

	private void downloadManager(ResultReceiver resultReceiver) {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception ex) {

			} finally {
				if (isCancelled) {
					downloadManager.remove(downloadId);
					return;
				}
			}
			Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
			if (cursor != null) {
				cursor.moveToFirst();
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				cursor.close();
				switch (status) {
				case DownloadManager.STATUS_SUCCESSFUL:
					downloadComplete(resultReceiver);
					return;
				case DownloadManager.STATUS_FAILED:
					return;
				}
			}
		}
	}

	private void downloadComplete(ResultReceiver resultReceiver) {
		Prefs.getPopcorn().edit().putString(PopcornPrefs.UPDATE_APK_PATH, apkFile.getAbsolutePath()).commit();
		Bundle data = new Bundle();
		data.putString(Updater.DATA_APK_URI_KEY, apkUri.toString());
		data.putString(Updater.DATA_VERSION_KEY, updateInfo.version);
		resultReceiver.send(Updater.RESULT_HAVE_UPDATE, data);
	}

	public class UpdateInfo {
		public String downloadUrl;
		public String version;
		public long size;
	}
}