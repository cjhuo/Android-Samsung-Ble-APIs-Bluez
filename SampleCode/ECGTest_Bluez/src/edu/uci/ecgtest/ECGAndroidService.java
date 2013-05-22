package edu.uci.ecgtest;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ECGAndroidService extends Service {

	private static final String TAG = "ECGAndroidService";
	private final IBinder mBinder = new ServiceBinder();
	private BLEECGProfile mBluetoothProfile = null;


	public class ServiceBinder extends Binder {
		public ECGAndroidService getService() {
			return ECGAndroidService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void onCreate() {
		mBluetoothProfile = new BLEECGProfile(getBaseContext());
		startForeground(999, new Notification());
	}

	public void onDestroy() {
		mBluetoothProfile.unregister();
		mBluetoothProfile.finish();
		super.onDestroy();
	}

	public void connectLEDevice(BluetoothDevice paramBluetoothDevice) {
		if (mBluetoothProfile != null)
			mBluetoothProfile.connectLEDevice(paramBluetoothDevice);
	}

	public void disconnectLEDevice(BluetoothDevice paramBluetoothDevice) {
		if (mBluetoothProfile != null)
			mBluetoothProfile.disconnectLEDevice(paramBluetoothDevice);
	}

	public void discoverCharacteristics(BluetoothDevice paramBluetoothDevice) {
		mBluetoothProfile.discoverCharacteristics(paramBluetoothDevice);
	}

	public void discoverECGCharByUuid(BluetoothDevice paramBluetoothDevice,
			String paramString) {
		mBluetoothProfile.discoverECGCharByUuid(paramBluetoothDevice,
				paramString);
	}

	public void setRemoteDevice(BluetoothDevice paramBluetoothDevice) {
		mBluetoothProfile.setRemoteDevice(paramBluetoothDevice);
	}

	public BluetoothDevice getConnectedLEDevice() {
		return mBluetoothProfile.getConnectedLEDevice();
	}

	public int getProfileState() {
		return mBluetoothProfile.getLEProfileState();
	}

	public void getRssiValue(BluetoothDevice paramBluetoothDevice) {
		mBluetoothProfile.getRssiValue(paramBluetoothDevice);
	}
	
	public void startECGRecording(BluetoothDevice device, int startFlag){
		mBluetoothProfile.startECGRecording(device, startFlag);	
	}
}
