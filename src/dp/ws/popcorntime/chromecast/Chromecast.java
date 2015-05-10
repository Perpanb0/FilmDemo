package dp.ws.popcorntime.chromecast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import dp.ws.popcorntime.subtitles.Subtitles;
import eu.sesma.castania.castserver.CastServerService;

public class Chromecast {

	private final String TAG = "tag";
	private final String APP_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;

	private Activity mActivity;
	private PopcornCastListener mPopcornListener;
	private TextTrackStyle mTrackStyle;

	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;
	private CastDevice mSelectedDevice;
	private GoogleApiClient mApiClient;
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;
	private RemoteMediaPlayer mRemoteMediaPlayer;
	private boolean mApplicationStarted;
	private boolean mWaitingForReconnect;

	public Chromecast(Activity activity, PopcornCastListener popcornListener) {
		mActivity = activity;
		mPopcornListener = popcornListener;
		mTrackStyle = TextTrackStyle.fromSystemSettings(activity);
		mTrackStyle.setBackgroundColor(Color.parseColor("#00ffffff"));
		mTrackStyle.setForegroundColor(Subtitles.getFontColor(activity));
		mTrackStyle.setEdgeType(TextTrackStyle.EDGE_TYPE_DROP_SHADOW);
		mTrackStyle.setEdgeColor(Color.parseColor("#bb000000"));
		mTrackStyle.setFontScale(Subtitles.getFontScale(activity));
	}

	public void onCreate(MediaRouteButton routeButton) {
		mMediaRouter = MediaRouter.getInstance(mActivity);
		mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID)).build();
		routeButton.setRouteSelector(mMediaRouteSelector);
		mMediaRouterCallback = new MediaRouterCallback();
		stopCastServer();
	}

	public void onResume() {
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
	}

	public void onPause() {
		if (mActivity.isFinishing()) {
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
	}

	public void onDestroy() {
		try {
			teardown();
			mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void play() {
		if (mRemoteMediaPlayer != null && mApiClient != null && mApiClient.isConnected()) {
			mRemoteMediaPlayer.play(mApiClient);
		}
	}

	public void pause() {
		if (mRemoteMediaPlayer != null && mApiClient != null && mApiClient.isConnected()) {
			mRemoteMediaPlayer.pause(mApiClient);
		}
	}

	public void setPosition(long position) {
		if (mRemoteMediaPlayer != null && mApiClient != null && mApiClient.isConnected()) {
			mRemoteMediaPlayer.seek(mApiClient, position);
		}
	}

	public void setVolume(double dmax, double dvolume) {
		if (Cast.CastApi == null || mApiClient == null) {
			return;
		}

		if (mApiClient.isConnected()) {
			double cvolume = dvolume / dmax;
			try {
				Cast.CastApi.setVolume(mApiClient, cvolume);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Callback for MediaRouter events
	 */
	private final class MediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(final MediaRouter router, final RouteInfo info) {
			mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
			launchReceiver();
			if (mPopcornListener != null) {
				mPopcornListener.onCastRouteSelected();
			}
		}

		@Override
		public void onRouteUnselected(final MediaRouter router, final RouteInfo info) {
			if (mPopcornListener != null) {
				if (mRemoteMediaPlayer != null) {
					mPopcornListener.onCastRouteUnselected(mRemoteMediaPlayer.getApproximateStreamPosition());
				} else {
					mPopcornListener.onCastRouteUnselected(0);
				}
			}
			teardown();
			mSelectedDevice = null;
		}
	}

	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(final int errorCode) {
					teardown();
				}

			};

			mConnectionCallbacks = new ConnectionCallbacks();
			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);

			mApiClient = new GoogleApiClient.Builder(mActivity).addApi(Cast.API, apiOptionsBuilder.build()).addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener).build();

			mApiClient.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

		@Override
		public void onConnected(final Bundle connectionHint) {
			if (mApiClient == null) {
				return;
			}

			if (mWaitingForReconnect) {
				mWaitingForReconnect = false;

				// Check if the receiver app is still running
				if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
					teardown();
				} else {
					reattachMediaChannel();
				}
			} else {
				try {
					Cast.CastApi.launchApplication(mApiClient, APP_ID, false).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {

						@Override
						public void onResult(final Cast.ApplicationConnectionResult result) {
							if (result.getStatus().isSuccess()) {
								mApplicationStarted = true;
								attachMediaChannel();
								mPopcornListener.onCastConnection();
								Log.d(TAG, "Chromecast launched");
							} else {
								Log.e(TAG, "application could not launch");
								teardown();
							}
						}
					});

				} catch (Exception e) {
					Log.e(TAG, "Failed to launch application", e);
				}
			}
		}

		@Override
		public void onConnectionSuspended(final int cause) {
			mWaitingForReconnect = true;
			// stopCastServer();
		}

	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(final ConnectionResult result) {
			teardown();
		}
	}

	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown() {
		if (mPopcornListener != null) {
			mPopcornListener.teardown();
		}
		stopCastServer();
		if (mApiClient != null) {
			if (mApplicationStarted) {
				if (mApiClient.isConnected()) {
					Cast.CastApi.stopApplication(mApiClient);
					detachMediaChannel();
					mApiClient.disconnect();
				}
				mApplicationStarted = false;
			}
			mApiClient = null;
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
	}

	/*
	 * Media Channel ===================
	 */

	// The Google Cast SDK supports a media channel to play media on a receiver
	// application. The media channel has a well-known namespace of
	// urn:x-cast:com.google.cast.media.
	// To use the media channel create an instance of RemoteMediaPlayer and set
	// the
	// update listeners to receive media status updates:
	private void attachMediaChannel() {
		if (null == mRemoteMediaPlayer) {
			mRemoteMediaPlayer = new RemoteMediaPlayer();
			mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {

				@Override
				public void onStatusUpdated() {
					MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
					if (mediaStatus != null) {
						switch (mediaStatus.getPlayerState()) {
						case MediaStatus.PLAYER_STATE_PLAYING:
							mPopcornListener.onCastStatePlaying();
							break;
						case MediaStatus.PLAYER_STATE_PAUSED:
							mPopcornListener.onCastStatePaused();
							break;
						case MediaStatus.PLAYER_STATE_IDLE:
							mPopcornListener.onCastStateIdle();
							break;
						case MediaStatus.PLAYER_STATE_BUFFERING:
							mPopcornListener.onCastStateBuffering();
							break;
						default:
							break;
						}
					}
				}
			});

			mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
				@Override
				public void onMetadataUpdated() {
					// MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
				}
			});
		}
		try {
			Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
		} catch (Exception e) {
			Log.e(TAG, "Failed to set up media channel", e);
		}

		// Call RemoteMediaPlayer.requestStatus() and wait for the
		// OnStatusUpdatedListener callback. This will update
		// the internal state of the RemoteMediaPlayer object with the current
		// state of the receiver, including the
		// current session ID.
		if (null != mRemoteMediaPlayer && null != mApiClient) {
			mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
				@Override
				public void onResult(final MediaChannelResult result) {
					if (!result.getStatus().isSuccess()) {
						Log.e(TAG, "Failed to request status.");
					}
				}
			});
		}
	}

	private void reattachMediaChannel() {
		if (null != mRemoteMediaPlayer && null != mApiClient) {
			try {
				Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
			} catch (IOException e) {
				Log.e(TAG, "Exception while creating channel", e);
			}
		}
	}

	private void detachMediaChannel() {
		if (null != mRemoteMediaPlayer) {
			if (null != mApiClient && null != Cast.CastApi) {
				try {
					Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace());
				} catch (Exception e) {
					Log.e(TAG, "Failed to detach media channel", e);
				}
			}
			mRemoteMediaPlayer = null;
		}
	}

	// To load media, the sender application needs to create a MediaInfo
	// instance using MediaInfo.Builder. The MediaInfo
	// instance is then used to load the media with the RemoteMediaPlayer
	// instance:
	public void loadMovieMedia(String mediaName, long playPosition, String title, String subPath) {
		WifiManager wm = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
		@SuppressWarnings("deprecation")
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

		int slash = mediaName.lastIndexOf('/');
		String filename = mediaName.substring(slash + 1);
		String rootDir = mediaName.substring(0, slash);

		startCastServer(ip, rootDir);

		List<MediaTrack> tracks = new ArrayList<MediaTrack>();

		if (subPath != null) {
			String subname = subPath.substring(subPath.lastIndexOf('/') + 1);
			MediaTrack subtitleTrack = new MediaTrack.Builder(1, MediaTrack.TYPE_TEXT).setName("Chromecast Subtitle").setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
					.setContentId("http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + subname).build();
			tracks.add(subtitleTrack);
		}

		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);

		MediaInfo mediaInfo = new MediaInfo.Builder("http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename).setContentType("video/mp4")
				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).setMediaTracks(tracks).build();

		try {
			mRemoteMediaPlayer.load(mApiClient, mediaInfo, true, playPosition).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
				@Override
				public void onResult(final MediaChannelResult result) {
					if (result.getStatus().isSuccess()) {
						mRemoteMediaPlayer.setTextTrackStyle(mApiClient, mTrackStyle);
						mPopcornListener.onCastMediaLoadSuccess();
					} else {
						mPopcornListener.onCastMediaLoadCancelInterrupt();
						mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
						teardown();
					}
				}
			});
		} catch (IllegalStateException e) {
			Log.e(TAG, "Problem occurred with media during loading", e);
		} catch (Exception e) {
			Log.e(TAG, "Problem opening media during loading", e);
		}
	}

	public void reloadMovie(final String mediaName, final String title, final String subPath) {
		final long time = mRemoteMediaPlayer.getApproximateStreamPosition();
		mRemoteMediaPlayer.stop(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

			@Override
			public void onResult(MediaChannelResult result) {
				if (result.getStatus().isSuccess()) {
					loadMovieMedia(mediaName, time, title, subPath);
				} else {
					mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
					teardown();
				}
			}
		});

	}

	public void sendSubtitleVtt(boolean enable) {
		long[] tracks = null;
		if (enable) {
			tracks = new long[] { 1 };
		} else {
			tracks = new long[0];
		}

		mRemoteMediaPlayer.setActiveMediaTracks(mApiClient, tracks).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

			@Override
			public void onResult(MediaChannelResult mediaChannelResult) {
				if (mediaChannelResult.getStatus().isSuccess()) {
					Log.d(TAG, "sendSubtitleVtt: isSuccess");
				} else {
					Log.e(TAG, "sendSubtitleVtt: error");
				}
			}
		});
		;
	}

	// WEB SERVER
	private void startCastServer(final String ip, final String rootDir) {
		Intent castServerService = new Intent(mActivity, CastServerService.class);
		castServerService.putExtra(CastServerService.IP_ADDRESS, ip);
		castServerService.putExtra(CastServerService.ROOT_DIR, rootDir);
		mActivity.startService(castServerService);
	}

	private void stopCastServer() {
		mActivity.stopService(new Intent(mActivity, CastServerService.class));
	}
}
