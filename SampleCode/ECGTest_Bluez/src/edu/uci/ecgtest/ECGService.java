package edu.uci.ecgtest;

import com.samsung.bluetoothle.BluetoothLEClientChar;
import com.samsung.bluetoothle.BluetoothLEClientService;
import com.samsung.bluetoothle.BluetoothLENamespace;

public class ECGService extends BluetoothLEClientService {

	public static final String ECG_REFRESH_CHAR = "edu.uci.ecgtest.ECGService.action.ECG_REFRESH_CHAR";
	private static final String TAG = "ECGService";
	public static String myUuid = BluetoothLENamespace.toUuid128StringFormat(0xfec0);//"0000fec0-0000-1000-8000-00805f9b34fb";
	public static String ECG_START_CHAR = BluetoothLENamespace.toUuid128StringFormat(0xfec5);//"0000fec5-0000-1000-8000-00805f9b34fb";

	public ECGService() {
		super(myUuid);
	}

	@Override
	public void onWatcherValueChanged(BluetoothLEClientChar characteristic) {
		super.onWatcherValueChanged(characteristic);
		// TBD
	}
	/*
	 * public void onWatcherBatteryValueChanged( BluetoothLEClientChar
	 * paramBluetoothLEClientChar) { }
	 * 
	 * public boolean registerBatteryWatcher() { return super.registerWatcher();
	 * }
	 * 
	 * public boolean unregisterBatteryWatcher() { return
	 * super.unregisterWatcher(); }
	 */
}
