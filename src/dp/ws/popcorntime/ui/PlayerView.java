package dp.ws.popcorntime.ui;

import dp.ws.popcorntime.R;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnTimedTextListener;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class PlayerView extends Activity {
	private VideoView mVideoView;
	private TextView mSubtitleView;
	private ProgressDialog prodlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		Intent intent = getIntent();
		String movieUrl = intent.getStringExtra("movieUrl");
		final String subUrl = intent.getStringExtra("subUrl");
		Log.d("MyApp", "=========== SubUrl:" + subUrl);
		setContentView(R.layout.player_view);
		prodlg = new ProgressDialog(this);
		prodlg.show();

		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mSubtitleView = (TextView) findViewById(R.id.subtitle_view);
		mVideoView.setVideoPath(movieUrl);
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();

		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				// optional need Vitamio 4.0
				mediaPlayer.setPlaybackSpeed(1.0f);
				mVideoView.addTimedTextSource(subUrl);
				mVideoView.setTimedTextShown(true);

			}
		});
		mVideoView.setOnTimedTextListener(new OnTimedTextListener() {

			@Override
			public void onTimedText(String text) {
				mSubtitleView.setText(text);
			}

			@Override
			public void onTimedTextUpdate(byte[] pixels, int width, int height) {

			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d("MyApp", "==================== do nothing");
	}
}
