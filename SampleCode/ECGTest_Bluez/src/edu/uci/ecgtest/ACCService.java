package edu.uci.ecgtest;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.bluetoothle.BluetoothLEClientChar;
import com.samsung.bluetoothle.BluetoothLEClientService;
import com.samsung.bluetoothle.BluetoothLENamespace;

public class ACCService extends BluetoothLEClientService {

	public static final String ACC_REFRESH_CHAR = "edu.uci.ecgtest.ACCService.action.ACC_REFRESH_CHAR";
	public static final String ACC_VALUE_REFRESH = "edu.uci.ecgtest.ACCService.action.ACC_VALUE_CHAR";
	private static final String TAG = "ACCService";
	public static String myUuid = BluetoothLENamespace
			.toUuid128StringFormat(0xffa0);// "0000fec0-0000-1000-8000-00805f9b34fb";
	public static String ACC_ENABLE_CHAR = BluetoothLENamespace
			.toUuid128StringFormat(0xffa1);// "0000fec5-0000-1000-8000-00805f9b34fb";
	public static String ACC_VALUE_CHAR = BluetoothLENamespace
			.toUuid128StringFormat(0xffa6);// "0000fec5-0000-1000-8000-00805f9b34fb";
	private Context mContext = null;
	private float x, y, z;

	public ACCService(Context context) {
		super(myUuid);
		this.mContext = context;
	}

	@Override
	public void onWatcherValueChanged(
			BluetoothLEClientChar paramBluetoothLEClientChar) {
		String uuid = paramBluetoothLEClientChar.getCharUUID();

		// Note: You have to get the changed characteristic value out from
		// buffer by triggering .getCharValue, otherwise any new incoming value
		// of this characteristic won't be pushed in next time. In other words,
		// onWatcherValueChanged method won't be triggered if you didn't call
		// getCharValue on this callback function. Tested
		byte[] data = paramBluetoothLEClientChar.getCharVaule(); 
		
		// decode data first before process, data was encoded using UTF-8
		String decodedString = null;
		try {
			decodedString = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] decodedData = new BigInteger(decodedString, 16).toByteArray();
		processXYZ(decodedData);
		

		Log.d(TAG, "Value changed event caught from characteristic: " + uuid
				+ " of value: " + decodedString);//data.toString());
		Intent localIntent = new Intent(ACC_VALUE_REFRESH);
		localIntent.putExtra("X", this.x);
		localIntent.putExtra("Y", this.y);
		localIntent.putExtra("Z", this.z);
		this.mContext.sendBroadcast(localIntent);
	}
	
	private void processXYZ(byte [] data){
		ByteBuffer bf = ByteBuffer.wrap(data);
		bf.order(ByteOrder.LITTLE_ENDIAN);
		short temp = bf.getShort();
		this.x = (0.0f + (temp >> 4))/1000;  //first 12 bits
		temp = bf.getShort();
		this.y = (0.0f + (temp >> 4))/1000;
		temp = bf.getShort();
		this.z = (0.0f + (temp >> 4))/1000;
	}
	
	private int toInt(char charValue){
		if(charValue>='0' && charValue<='9')
			return charValue - '0';
		if(charValue>='a' && charValue<='f')
			return 10 + (charValue - 'a');
		return 0;
	}
	
	private short[] toShortArray(byte [] arrayOfByte){
		char[] charArray = new String(arrayOfByte).toCharArray();
		short[] shortArray = new short[charArray.length/2]; //length must be an even number
		int i=0;
		int j=0;
		while(true){
			if(j>=charArray.length){
				return shortArray;
			}
			int n = 16*toInt(charArray[j++]);
			shortArray[i++] = ((short)(n + toInt(charArray[j++])));
		}				
	}
	
	private int[] toIntArray(byte [] arrayOfByte){
		char[] charArray = new String(arrayOfByte).toCharArray();
		int[] intArray = new int[charArray.length/4]; //length must be an even number
		int i=0;
		int j=0;
		while(true){
			if(j>=charArray.length){
				return intArray;
			}
			int n = 16*16*16*toInt(charArray[j++]);
			n = n + 16*16*toInt(charArray[j++]);
			n = n + 16*toInt(charArray[j++]);
			intArray[i++] = n + toInt(charArray[j++]);
		}				
	}

	@Override
	public boolean registerWatcher() {
		return super.registerWatcher();
	}

	@Override
	public boolean unregisterWatcher() {
		return super.unregisterWatcher();
	}
}
