package dp.ws.popcorntime.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import vn.vovi.utils.Base64;
import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import dp.ws.popcorntime.model.LoaderResponse;

public class URLLoader extends Loader<LoaderResponse> {

	public static final String URL_KEY = "popcorntime_url";
	public static final String DATA = "popcorntime_data";

	private Bundle data = null;
	private URLTask task = null;
	private LoaderResponse response = null;

	public URLLoader(Context context, Bundle data) {
		super(context);
		this.data = data;
	}

	@Override
	protected void onStartLoading() {
		if (response != null) {
			deliverResult(response);
			response = null;
		}
		super.onStartLoading();
	}

	@Override
	protected void onReset() {
		if (task != null && AsyncTask.Status.FINISHED != task.getStatus()) {
			task.cancel(true);
		}
		super.onReset();
	}

	@Override
	protected void onForceLoad() {
		super.onForceLoad();
		task = new URLTask();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				data.getString(URL_KEY), data.getString(DATA));
	}

	private void setResponse(LoaderResponse response) {
		this.response = response;
	}

	private class URLTask extends AsyncTask<String, Void, LoaderResponse> {

		@Override
		protected LoaderResponse doInBackground(String... params) {
			LoaderResponse response = new LoaderResponse();
			try {
				Log.d("MyApp", "=================== URLLoader: " + params[0]
						+ " / " + params[1]);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(params[0]);
				String request = Base64.encode(params[1].getBytes());
				StringEntity se = new StringEntity(request);
				httppost.setEntity(se);
				se.setContentType("application/json;charset=UTF-8");
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
						"application/json;charset=UTF-8"));

				// Execute HTTP Post Request
				HttpResponse httpResponse = client.execute(httppost);
				HttpEntity entity = httpResponse.getEntity();
				String data = EntityUtils.toString(entity);
				data = new String(Base64.decode(data));
				response.data = data;
				Log.d("MyApp", "=========== return: " + response.data);
				JSONObject jsO = new JSONObject(response.data);
				String code = jsO.getString("code");
				if (code.equals("0")) {
					response.error = jsO.getString("msg");
				}
			} catch (Exception ex) {
				Log.e("MyApp", ex.getMessage(), ex);
				response.error = ex.getMessage();
			}

			if (isCancelled()) {
				response = null;
			}

			return response;
		}

		@Override
		protected void onPostExecute(LoaderResponse result) {
			if (isStarted()) {
				deliverResult(result);
			} else {
				setResponse(result);
			}
		}
	}
}