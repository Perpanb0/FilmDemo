package dp.ws.popcorntime.updater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import dp.ws.popcorntime.R;

public class Updater implements UpdaterReceiver {

	private static Updater INSTANCE = new Updater();

	public static final int RESULT_HAVE_UPDATE = 111;

	public static final String DATA_APK_URI_KEY = "apk_uri";
	public static final String DATA_VERSION_KEY = "version";

	private UpdaterResultReceiver mResultReceiver;
	private Intent intent;
	private Activity mActivity;
	private Uri apkUri;
	private String version;
	private boolean haveUpdate = false;

	private UpdateDialog updateDialog;

	private Updater() {
		mResultReceiver = new UpdaterResultReceiver(new Handler());
		mResultReceiver.setReceiver(Updater.this);
	}

	public static Updater getInstance() {
		return INSTANCE;
	}

	public void setCurrentActivity(Activity activity) {
		this.mActivity = activity;
		if (haveUpdate) {
			showUpdateDialog();
			haveUpdate = false;
		}
	}

	public void init(Context context) {

		intent = new Intent(Intent.ACTION_SYNC, null, context,
				UpdaterService.class);
		intent.putExtra(UpdaterService.RESULT_RECEIVER, mResultReceiver);
		intent.putExtra(UpdaterService.NAME_OF_ACTION,
				UpdaterService.ACTION_CHECK);
		context.startService(intent);
	}

	public void destroy(Context context) {
		if (intent != null) {
			context.stopService(intent);
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		if (RESULT_HAVE_UPDATE == resultCode) {
			apkUri = Uri.parse(resultData.getString(DATA_APK_URI_KEY));
			version = resultData.getString(DATA_VERSION_KEY);
			if (mActivity != null) {
				showUpdateDialog();
			} else {
				haveUpdate = true;
			}
		}
	}

	/*
	 * Dialog
	 */

	private void showUpdateDialog() {
		if (null == updateDialog) {
			updateDialog = new UpdateDialog();
		}
		if (!updateDialog.isAdded()) {
			updateDialog.show(mActivity.getFragmentManager(), "update_dialog");
		}
	}

	private class UpdateDialog extends DialogFragment {

		public UpdateDialog() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.app_name));
			builder.setMessage(getResources().getString(
					R.string.available_new_version)
					+ " "
					+ version
					+ "\n"
					+ getResources().getString(R.string.update_now));
			builder.setPositiveButton(
					getResources().getString(R.string.update), null);
			builder.setNegativeButton(getResources().getString(R.string.later),
					null);

			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(apkUri,
									"application/vnd.android.package-archive");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
			dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dismiss();
						}
					});

			return dialog;
		}
	}

}