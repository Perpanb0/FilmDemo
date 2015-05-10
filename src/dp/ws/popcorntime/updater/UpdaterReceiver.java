package dp.ws.popcorntime.updater;

import android.os.Bundle;

public interface UpdaterReceiver {

	public void onReceiveResult(int resultCode, Bundle resultData);
}