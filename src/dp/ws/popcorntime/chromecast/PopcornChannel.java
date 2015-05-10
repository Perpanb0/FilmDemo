package dp.ws.popcorntime.chromecast;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;

public class PopcornChannel implements MessageReceivedCallback {

	public String getNamespace() {
		return "urn:x-cast:dp.ws.popcorntime";
	}

	@Override
	public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
//		Log.d("tag", "onMessageReceived: " + message);
	}
}