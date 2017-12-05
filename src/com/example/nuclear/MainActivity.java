package com.example.nuclear;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.example.nuclear.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	CountDownTimer timer;
	int overheat;
	private static final String TAG = "Nuklir";
	private SeekBar fuelBar,tempBar,wafoBar,powerBar,coolBar;
	private TextView textfuel,texttemp,textwafo,textpwr,textcool;
	private TextView fuel,temp,wafo,pwr,cool;
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	
	// Well known SPP UUID
	private static final UUID MY_UUID =	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	 
	//private static String address = "B4:82:FE:5E:F7:33"; habib
	//private static String address = "20:16:D8:D3:EA:AE";	//micheal laptop
	//private static String address = "E0:CA:94:CA:21:30";	//suryo laptop
	//private static String address = "20:14:11:28:23:59";	//HC-05 mic
	private static String address = "20:14:11:26:03:94";	//HC-05 ct
	//private static String address = "A4:DB:30:AF:6D:89";	//Andre laptop
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setDisplayShowHomeEnabled(false);
		fuelBar = (SeekBar) findViewById(R.id.seekfuel);
		tempBar = (SeekBar) findViewById(R.id.seektemp);
		wafoBar = (SeekBar) findViewById(R.id.seekwater);
		powerBar = (SeekBar) findViewById(R.id.seekpower);
		coolBar = (SeekBar) findViewById(R.id.seekcooler);
		textfuel = (TextView) findViewById(R.id.txtfuel);
		texttemp = (TextView) findViewById(R.id.txttemp);
		textwafo = (TextView) findViewById(R.id.txtwater);
		textpwr = (TextView) findViewById(R.id.txtpower);
		cool = (TextView) findViewById(R.id.cooler);
		fuel = (TextView) findViewById(R.id.fuel);
		temp = (TextView) findViewById(R.id.temp);
		wafo = (TextView) findViewById(R.id.water);
		pwr = (TextView) findViewById(R.id.power);
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	    checkBTState();
	    
	    //Fuel Level
	    fuelBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar seekBar) {
	    		int status=seekBar.getProgress()*10;
	    		fuel.setText(""+status);
	    		if		(status<=9)	sendData("f00"+status);
	    		else if	(status<=99)	sendData("f0"+status);
	    		else	sendData("f"+status);
	    	}
			
	    	@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
	    });
	    
	    //Temperature Level 
	    tempBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
	    	int status=seekBar.getProgress()*6;
	    	temp.setText(""+status);
	    	if(status<=9)	sendData("t00"+status);
	    	else if	(status<=99)	sendData("t0"+status);
	    	else	sendData("t"+status);
	    	if(status>500 && overheat==0){
	    		timer = new CountDownTimer(10000, 2000){
	    		public void onTick(long milisUntilFinished){
	    			Toast.makeText(getApplicationContext(), "Overtheat! System Shutdown in "+ milisUntilFinished/1000 +"s",Toast.LENGTH_SHORT).show();
	    		}
	    		public void onFinish(){
	    			Toast.makeText(getApplicationContext(), "System Shutdown",Toast.LENGTH_SHORT).show();
	    			sendData("a");	overheat=0;
	    		}
	    		}.start();
    			overheat=1;
	    	}
	    	else if(status<500 && overheat==1){
	    		timer.cancel();	timer=null;	overheat=0;
	    	}
	    }
	    @Override
		public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
	    });
	    
	    //Water Flow
	    wafoBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar seekBar) {
	    		int status=seekBar.getProgress();
	    		wafo.setText(""+status);
	    		if		(status<=9)	sendData("w00"+status);
	    		else if	(status<=99)	sendData("w0"+status);
	    		else	sendData("w"+status);
	    	}			
	    	@Override
			public void onProgressChanged(SeekBar seekBar, int progress,	boolean fromUser) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
	    });
	    
	    //Power Out
	    powerBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar seekBar) {
	    		int status=seekBar.getProgress();
	    		pwr.setText(""+status);
	    		if		(status<=9)	sendData("p00"+status);
	    		else if	(status<=99)	sendData("p0"+status);
	    		else	sendData("p"+status);
	    	}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
	    });
	    
	    //Cooler Flow
	    coolBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    	@Override
	    	public void onStopTrackingTouch(SeekBar seekBar) {
	    		int status=seekBar.getProgress();
	    		cool.setText(""+status);
	    		if		(status<=9)	sendData("c00"+status);
	    		else if	(status<=99)	sendData("c0"+status);
	    		else	sendData("c"+status);
	    	}
	    	@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
	    });
	}
	
	@Override
	  public void onResume() {
	    super.onResume();
	    Log.d(TAG, "...In onResume - Attempting client connect...");
	    BluetoothDevice device = btAdapter.getRemoteDevice(address);
	    try {btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);}
	    catch (IOException e) {
	    	errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
	    }
	    btAdapter.cancelDiscovery();
	    Log.d(TAG, "...Connecting to Remote...");
	    try {	btSocket.connect();
	    		Log.d(TAG, "...Connection established and data link opened...");
	    }
	    catch (IOException e) {
	      try {btSocket.close();}
	      catch (IOException e2) {
	        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
	      }
	    }
	    Log.d(TAG, "...Creating Socket...");
	    try{outStream = btSocket.getOutputStream();}
	    catch (IOException e) {
	      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
	    }
	  }
	 
	  @Override
	  public void onPause() {
	    super.onPause();
	    Log.d(TAG, "...In onPause()...");
	    if (outStream != null) {
	      try {outStream.flush();}
	      catch (IOException e) {
	        errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
	      }
	    }
	    try{btSocket.close();}
	    catch (IOException e2) {
	      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
	    }
	  }
	   
	  private void checkBTState() {
	    // Check for Bluetooth support and then check to make sure it is turned on
	    // Emulator doesn't support Bluetooth and will return null
	    if(btAdapter==null) { 
	      errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
	    }
	    else{if (btAdapter.isEnabled()) {Log.d(TAG, "...Bluetooth is enabled...");} 
	    else {//Prompt user to turn on Bluetooth
	        	Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
	        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	    }
	    }
	  }
	 
	  private void errorExit(String title, String message){
	    Toast msg = Toast.makeText(getBaseContext(),title + " - " + message, Toast.LENGTH_SHORT);
	    msg.show();	finish();
	  }
	 
	  private void sendData(String message) {
	    byte[] msgBuffer = message.getBytes();
	    Log.d(TAG, "...Sending data : " + message + "...");
	    try {outStream.write(msgBuffer);}
	    catch (IOException e){}
	  }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {return true;}
		return super.onOptionsItemSelected(item);
	}
}
