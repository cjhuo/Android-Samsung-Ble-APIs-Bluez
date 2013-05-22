package edu.uci.ecgtest;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.samsung.bluetoothle.BluetoothLEClientChar;
import com.samsung.bluetoothle.BluetoothLEClientProfile;
import com.samsung.bluetoothle.BluetoothLEClientService;

public class BLEECGProfile extends BluetoothLEClientProfile {
	public static final String CHARACTERISITICS_REFRESH = "edu.uci.ecgtest.bleprofile.action.refresh";
	public static final String DEVICE_CONNECTED = "edu.uci.ecgtest.bleprofile.action.connected";
	public static final String DEVICE_DISCONNECTED = "edu.uci.ecgtest.bleprofile.action.disconnected";
	public static final String DEVICE_LINK_LOSS = "edu.uci.ecgtest.bleprofile.action.linkloss";
	public static final String DEVICE_RSSI_VAL = "edu.uci.ecgtest.bleprofile.rssi";
	private final String TAG = "BLEProximityProfile";
	private ECGService eService = null;
	private ACCService aService = null;
	private Context mContext;

	public BLEECGProfile(Context context) {
		super(context);
		this.mContext = context;
		ArrayList al = new ArrayList();
		eService = new ECGService();
		aService = new ACCService(context);
		al.add(eService);
		al.add(aService);
		registerLEProfile(al);
	}

	public void unregister() {
		unregisterLEProfile();

	}

	@Override
	public void discoverCharacteristics(BluetoothDevice device) {
		super.discoverCharacteristics(device);
	}

	public void discoverECGCharByUuid(BluetoothDevice device, String uuid) {
		eService.discoverCharacteristics(device, uuid);
	}

	@Override
	public void onDiscoverCharacteristics(BluetoothDevice device) {
		super.onDiscoverCharacteristics(device);
		Intent localIntent = new Intent();
		localIntent.setAction(CHARACTERISITICS_REFRESH).putExtra(
				BluetoothDevice.EXTRA_DEVICE, device);
		this.mContext.sendBroadcast(localIntent);

		// enable/disable ACC
		this.enableACCService(device, 0x01);
	}

	@Override
	public void onGetRssiValue(BluetoothDevice device, String value) {
		Intent localIntent = new Intent();
		localIntent.setAction(DEVICE_RSSI_VAL)
				.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
				.putExtra(BluetoothDevice.EXTRA_RSSI, value);
		this.mContext.sendBroadcast(localIntent);
	}

	@Override
	public void onLEDeviceConnected(BluetoothDevice device) {
		super.onLEDeviceConnected(device);
		Intent localIntent = new Intent();
		localIntent.setAction(DEVICE_CONNECTED).putExtra(
				BluetoothDevice.EXTRA_DEVICE, device);
		this.mContext.sendBroadcast(localIntent);
	}

	@Override
	public void onLEDeviceDisconnected(BluetoothDevice device) {
		super.onLEDeviceDisconnected(device);
		Intent localIntent = new Intent();
		localIntent.setAction(DEVICE_DISCONNECTED).putExtra(
				BluetoothDevice.EXTRA_DEVICE, device);
		this.mContext.sendBroadcast(localIntent);
	}

	@Override
	public void onLELinkLoss(BluetoothDevice device) {
		Intent localIntent = new Intent();
		localIntent.setAction(DEVICE_LINK_LOSS).putExtra(
				BluetoothDevice.EXTRA_DEVICE, device);
		this.mContext.sendBroadcast(localIntent);
	}

	/*
	 * public BluetoothLEClientChar getCharbyUUID(BluetoothDevice device, String
	 * uuid) { return eService.getCharbyUUID(device, uuid); }
	 */

	/*
	 * public void writeCharValue(BluetoothLEClientChar bleChar, int value) {
	 * eService.writeCharValue(bleChar, value); }
	 */

	public void startECGRecording(BluetoothDevice device, int startFlag) {
		byte[] arrayOfByte = new byte[1];
		arrayOfByte[0] = ((byte) startFlag);
		BluetoothLEClientChar localBluetoothLEClientChar = eService
				.getCharbyUUID(device, ECGService.ECG_START_CHAR);
		localBluetoothLEClientChar.setCharValue(arrayOfByte);
		eService.writeCharValue(localBluetoothLEClientChar,
				BluetoothLEClientService.GATT_WRITE_CMD);
	}

	public void enableACCService(BluetoothDevice device, int enable) {
		byte[] arrayOfByte = new byte[1];
		arrayOfByte[0] = ((byte) enable);
		BluetoothLEClientChar localBluetoothLEClientChar = aService
				.getCharbyUUID(device, ACCService.ACC_ENABLE_CHAR);
		localBluetoothLEClientChar.setCharValue(arrayOfByte);
		aService.writeCharValue(localBluetoothLEClientChar,
				BluetoothLEClientService.GATT_WRITE_CMD);

		// set notify
		if (enable == 0x01) {
			this.aService.registerWatcher();
			// set client description
			writeACCForNotification(device);
			// writeACCForIndication(device);
		} else {
			this.aService.unregisterWatcher();
		}

	}

	public void writeACCForIndication(BluetoothDevice paramBluetoothDevice) {
		byte[] arrayOfByte = { 2, 0 };
		BluetoothLEClientChar localBluetoothLEClientChar = aService
				.getCharbyUUID(paramBluetoothDevice, ACCService.ACC_VALUE_CHAR);
		localBluetoothLEClientChar.setClientConfigDesc(arrayOfByte);
		aService.writeClientConfigDesc(localBluetoothLEClientChar);
	}

	public void writeACCForNotification(BluetoothDevice paramBluetoothDevice) {
		byte[] arrayOfByte = { 1, 0 };
		BluetoothLEClientChar localBluetoothLEClientChar = aService
				.getCharbyUUID(paramBluetoothDevice, ACCService.ACC_VALUE_CHAR);
		localBluetoothLEClientChar.setClientConfigDesc(arrayOfByte);
		aService.writeClientConfigDesc(localBluetoothLEClientChar);
	}
}
