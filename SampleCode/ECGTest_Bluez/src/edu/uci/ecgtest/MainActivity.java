package edu.uci.ecgtest;

import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.bluetoothle.BluetoothLEClientProfile;

public class MainActivity extends Activity implements View.OnClickListener {

	public static final String TAG = "MainActivity";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_SELECT_DEVICE = 2;
	private BluetoothDevice mDevice = null;
	private BluetoothAdapter mBtAdapter = null;
	private int mDeviceConnectionState = BluetoothLEClientProfile.GATT_STATE_DISCONNECTED;
	private ServiceConnection mServiceConnection = null;
	private ECGAndroidService mMainService = null;
	private TextView deviceName;
	private TextView connectionStatus;
	private TextView rssi;
	private ImageView animatedView;
	private TextView acc_x;
	private TextView acc_y;
	private TextView acc_z;
	private AnimationDrawable mAnimation;
	private Button scanButton;
	private Button startButton;
	private Button stopButton;
	private Button disconnectButton;
	private ProgressBar progressBar;
	private Handler mHandler = new Handler();
	private int START_FLAG = 0x01;
	private int STOP_FLAG = 0x00;
	private float xVal;
	private float yVal;
	private float zVal;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String str = intent.getAction();
			if (str.equals(BLEECGProfile.DEVICE_CONNECTED)) {
				BluetoothDevice localDevice = (BluetoothDevice) intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (MainActivity.this.mDevice != null && localDevice != null
						&& MainActivity.this.mDevice.equals(localDevice)) {
					MainActivity.this.setConnectedStatus(true);
					MainActivity.this
							.setDeviceNameOnScreen(MainActivity.this.mDevice
									.getName());
					MainActivity.this.mDeviceConnectionState = BluetoothLEClientProfile.GATT_STATE_CONNECTED;
					MainActivity.this.mMainService
							.discoverCharacteristics(MainActivity.this.mDevice);
				}
				MainActivity.this.disconnectButton.setVisibility(View.VISIBLE);
				MainActivity.this.scanButton.setVisibility(View.GONE);
				MainActivity.this.startReadingRSSI();
			}
			if (str.equals(BLEECGProfile.DEVICE_DISCONNECTED)) {
				// re-connect if there is any sudden disconnection
				if (MainActivity.this.mDeviceConnectionState == BluetoothLEClientProfile.GATT_STATE_CONNECTED) {
					MainActivity.this.mMainService
							.connectLEDevice(MainActivity.this.mDevice);
					MainActivity.this.progressBar.setVisibility(View.VISIBLE);
				}
				MainActivity.this.setConnectedStatus(false);
				MainActivity.this
						.setDeviceNameOnScreen(getString(R.string.no_device));
				MainActivity.this.disconnectButton.setVisibility(View.GONE);
				MainActivity.this.rssi.setText(getString(R.string.no_rssi));
				MainActivity.this.stopReadingRSSI();
			}
			if (str.equals(BLEECGProfile.CHARACTERISITICS_REFRESH)) {
				BluetoothDevice localDevice = (BluetoothDevice) intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (MainActivity.this.mDevice != null && localDevice != null
						&& MainActivity.this.mDevice.equals(localDevice)) {
					for (ParcelUuid pcb : MainActivity.this.mDevice.getUuids()) {
						String logStr = "Found Service with UUID: "
								+ pcb.toString();
						Log.d(TAG, logStr);
						// found ECG Service
						if (pcb.toString().equals(ECGService.myUuid)) {
							MainActivity.this.progressBar
									.setVisibility(View.GONE);
							MainActivity.this.startButton
									.setVisibility(View.VISIBLE);
							MainActivity.this.stopButton
									.setVisibility(View.GONE);
						}
					}
				}
			}
			if (str.equals(BLEECGProfile.DEVICE_RSSI_VAL)) {
				BluetoothDevice localDevice = (BluetoothDevice) intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (MainActivity.this.mDevice != null && localDevice != null
						&& MainActivity.this.mDevice.equals(localDevice)) {
					String rssiStr = intent
							.getStringExtra(BluetoothDevice.EXTRA_RSSI);
					short rssiVal = Integer.valueOf(rssiStr).shortValue();
					rssiStr = Short.toString(rssiVal);
					MainActivity.this.rssi.setText(rssiStr);
				}
			}
			if(str.equals(ACCService.ACC_VALUE_REFRESH)){
				MainActivity.this.xVal = intent.getFloatExtra("X", 0.0f);
				MainActivity.this.yVal = intent.getFloatExtra("Y", 0.0f);
				MainActivity.this.zVal = intent.getFloatExtra("Z", 0.0f);
				
				runOnUiThread(new Runnable(){
					public void run(){
						MainActivity.this.setACCText();
					}
				});
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		init();
	}

	@Override
	public void onDestroy() {
		if (this.mDeviceConnectionState == BluetoothLEClientProfile.GATT_STATE_CONNECTED) {
			stopReadingRSSI();
			this.mMainService.disconnectLEDevice(this.mDevice);
		}
		removeBond();
		unbindService(mServiceConnection);
		stopService(new Intent(this, ECGAndroidService.class));
		this.mMainService = null;

		unregisterReceiver(this.mReceiver);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.popup_title)
				.setMessage(R.string.popup_message)
				.setPositiveButton(R.string.popup_yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MainActivity.this.finish();
							}
						}).setNegativeButton(R.string.popup_no, null).show();

	}

	private void init() {
		isBluetoothEnabled();
		setAndroidService();
		setUI();
		setRegisterReceiver();
	}

	private void removeBond() {

		Iterator<BluetoothDevice> iterator = mBtAdapter.getBondedDevices()
				.iterator();
		while (iterator.hasNext()) {
			BluetoothDevice device = iterator.next();
			if (device.isLEDeviceConnected())
				if (device.getBondState() != BluetoothDevice.BOND_BONDED)
					device.createBond();
			if (mMainService.getConnectedLEDevice() == null)
				mMainService.setRemoteDevice(device);
			mMainService.disconnectLEDevice(device);
			if (device.getBondState() == BluetoothDevice.BOND_BONDED)
				device.removeBond();
		}
		mDeviceConnectionState = BluetoothLEClientProfile.GATT_STATE_DISCONNECTED;
		mDevice = null;
	}

	private void setRegisterReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BLEECGProfile.CHARACTERISITICS_REFRESH);
		filter.addAction(BLEECGProfile.DEVICE_CONNECTED);
		filter.addAction(BLEECGProfile.DEVICE_DISCONNECTED);
		filter.addAction(BLEECGProfile.DEVICE_LINK_LOSS);
		filter.addAction(BLEECGProfile.DEVICE_RSSI_VAL);
		filter.addAction(ACCService.ACC_VALUE_REFRESH);
		registerReceiver(mReceiver, filter);
	}

	private void setUI() {
		deviceName = (TextView) findViewById(R.id.deviceName);

		connectionStatus = (TextView) findViewById(R.id.statusValue);

		rssi = (TextView) findViewById(R.id.rssiValue);

		animatedView = (ImageView) findViewById(R.id.animated_view);
		animatedView.setBackgroundResource(R.drawable.animateion_loader);
		mAnimation = (AnimationDrawable) animatedView.getBackground();
		animatedView.setVisibility(View.GONE);

		scanButton = (Button) findViewById(R.id.btn_select);
		scanButton.setOnClickListener(this);

		startButton = (Button) findViewById(R.id.btn_start);
		startButton.setVisibility(View.GONE);
		startButton.setOnClickListener(this);

		stopButton = (Button) findViewById(R.id.btn_stop);
		stopButton.setVisibility(View.GONE);
		stopButton.setOnClickListener(this);

		disconnectButton = (Button) findViewById(R.id.btn_disconnect);
		disconnectButton.setVisibility(View.GONE);
		disconnectButton.setOnClickListener(this);

		progressBar = (ProgressBar) findViewById(R.id.progressBarECG);
		progressBar.setVisibility(View.GONE);
		
		acc_x = (TextView) findViewById(R.id.acc_x);
		acc_y = (TextView) findViewById(R.id.acc_y);
		acc_z = (TextView) findViewById(R.id.acc_z);
	}
	
	private void setACCText(){
		acc_x.setText(Float.toString(xVal));
		acc_y.setText(Float.toString(yVal));
		acc_z.setText(Float.toString(zVal));
	}

	private void startAnimation() {
		mAnimation.start();
	}

	private void stopAnimation() {
		mAnimation.stop();
	}

	private void setAndroidService() {
		// start service, if not already running (but it is)
		startService(new Intent(this, ECGAndroidService.class));
		mServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder rawBinder) {
				MainActivity.this.mMainService = ((ECGAndroidService.ServiceBinder) rawBinder)
						.getService();
				MainActivity.this.mDeviceConnectionState = MainActivity.this.mMainService
						.getProfileState();
				if (MainActivity.this.mDeviceConnectionState == BluetoothLEClientProfile.GATT_STATE_CONNECTED) {
					MainActivity.this.mDevice = MainActivity.this.mMainService
							.getConnectedLEDevice();
				}
				MainActivity.this.removeBond();
			}

			public void onServiceDisconnected(ComponentName className) {
				MainActivity.this.mMainService = null;
				MainActivity.this.finish();
			}
		};
		bindService(new Intent(this, ECGAndroidService.class),
				mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		case R.id.btn_select:
			if (mDevice == null) {
				Intent newIntent = new Intent(MainActivity.this,
						DeviceListActivity.class);
				startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
			}
			break;
		case R.id.btn_start:
			MainActivity.this.mMainService.startECGRecording(
					MainActivity.this.mDevice, START_FLAG);
			startButton.setVisibility(View.GONE);
			stopButton.setVisibility(View.VISIBLE);
			scanButton.setVisibility(View.GONE);
			animatedView.setVisibility(View.VISIBLE);
			startAnimation();
			break;
		case R.id.btn_stop:
			MainActivity.this.mMainService.startECGRecording(
					MainActivity.this.mDevice, STOP_FLAG);
			startButton.setVisibility(View.VISIBLE);
			stopButton.setVisibility(View.GONE);
			scanButton.setVisibility(View.GONE);
			stopAnimation();
			animatedView.setVisibility(View.GONE);
			break;
		case R.id.btn_disconnect:
			removeBond();
			scanButton.setVisibility(View.VISIBLE);
			startButton.setVisibility(View.GONE);
			stopButton.setVisibility(View.GONE);
			disconnectButton.setVisibility(View.GONE);

			MainActivity.this.setConnectedStatus(false);
			MainActivity.this
					.setDeviceNameOnScreen(getString(R.string.no_device));
			break;
		default:
			break;
		}
	}

	private void isBluetoothEnabled() {
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (this.mBtAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
		}
		if (this.mBtAdapter.isEnabled())
			return;
		startActivityForResult(new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {

		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(this, "Bluetooth has turned on ",
						Toast.LENGTH_SHORT).show();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, "Problem in BT Turning ON ",
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;

		case REQUEST_SELECT_DEVICE:
			if (resultCode == Activity.RESULT_OK && intent != null) {
				String deviceAddress = intent
						.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
						deviceAddress);
				Log.d(TAG, "... onActivityResultdevice.address==" + mDevice
						+ "mserviceValue" + mMainService);
				mMainService.setRemoteDevice(mDevice);

				// mMainService.connectLEDevice(mDevice);

				// if (mDevice.isLEDeviceConnected()){
				// setDeviceNameOnScreen(mDevice.getName()); }

				progressBar.setVisibility(View.VISIBLE);

				if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
					mDevice.removeBond();
				}

				final Handler scanHandler = new Handler();
				final Runnable rnb = new Runnable() {
					public void run() {
						if (MainActivity.this.mMainService != null) {
							//Log.d(TAG, "BondState: " + MainActivity.this.mDevice.getBondState());
							Log.d(TAG, "LE Connected State: " + MainActivity.this.mMainService.getProfileState());
							if (MainActivity.this.mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
								MainActivity.this.mDevice.createBond();
							} 
							else if (MainActivity.this.mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
								if (MainActivity.this.mMainService
										.getProfileState() == BLEECGProfile.GATT_STATE_DISCONNECTED) {
									MainActivity.this.mMainService
											.connectLEDevice(MainActivity.this.mDevice);
								}
								if (MainActivity.this.mMainService.getProfileState() == BLEECGProfile.GATT_STATE_CONNECTING) {
									Log.d(TAG, "Still GATT_CONNECTING");
								}
								if (MainActivity.this.mDevice.isLEDeviceConnected()) {
									scanHandler.removeCallbacks(this);
									return;
								}
							}
						}
						scanHandler.postDelayed(this, 3000L);
					}
				};
				rnb.run();

			}
			break;
		default:
			Log.e(TAG, "wrong request code");
			break;
		}
	}

	private Runnable mRepeatTask = new Runnable() {
		public void run() {
			if (MainActivity.this.mDevice != null)
				MainActivity.this.mMainService
						.getRssiValue(MainActivity.this.mDevice);
			mHandler.postDelayed(MainActivity.this.mRepeatTask, 3000L);
		}
	};

	void startReadingRSSI() {
		mRepeatTask.run();
	}

	void stopReadingRSSI() {
		mHandler.removeCallbacks(mRepeatTask);
	}

	private void setConnectedStatus(boolean connected) {
		if (connectionStatus != null) {
			if (connected == true)
				connectionStatus.setText("Connected");
			else
				connectionStatus.setText(getString(R.string.disconnected));
		}
	}

	private void setDeviceNameOnScreen(String name) {
		if (deviceName != null) {
			if (name.length() > 20)
				name = name.substring(0, 20) + "...";
			deviceName.setText(name);
		}
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */
}
