package kr.re.Dev.ArduinoEcho;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import kr.re.Dev.Bluetooth.BluetoothSerialClient;
import kr.re.Dev.Bluetooth.BluetoothSerialClient.BluetoothStreamingHandler;
import kr.re.Dev.Bluetooth.BluetoothSerialClient.OnBluetoothEnabledListener;
import kr.re.Dev.Bluetooth.BluetoothSerialClient.OnScanListener;
import kr.re.Dev.BluetoothEcho.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


/**
 * Blutooth Arduino Echo.
 * 
 * 문자열의 끝은 '\0' 을 붙여서 구분한다.
 * 
 * www.dev.re.kr
 * @author ice3x2@gmail.com / Beom
 */
public class MainActivity extends Activity {
	
	private LinkedList<BluetoothDevice> mBluetoothDevices = new LinkedList<BluetoothDevice>();
	private ArrayAdapter<String> mDeviceArrayAdapter;
	
	private EditText mEditTextInput;
	private EditText sendText1;
	private EditText sendText2;
	private TextView mTextView;
	private Button mButtonSend;
	private ProgressDialog mLoadingDialog;
	private AlertDialog mDeviceListDialog;
	private Menu mMenu;
	private BluetoothSerialClient mClient;
	private String temp = "00.00";
	private String humi = "00.00";
	private String human_detect = "0";
	private String Door_detect = "0";
	private String PanPower = "0";
	private Button mButtonSend2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.activity_main);
		 mClient = BluetoothSerialClient.getInstance();

		/*
		Button b = (Button)findViewById(R.id.button2);

		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(
						getApplicationContext(), // 현재 화면의 제어권자
						SettingFanPower.class); // 다음 넘어갈 클래스 지정
				startActivity(intent); // 다음 화면으로 넘어간다
			}
		});
		*/


		if(mClient == null) {
			 Toast.makeText(getApplicationContext(), "Cannot use the Bluetooth device.", Toast.LENGTH_SHORT).show();
			 finish();
		 }
		 overflowMenuInActionBar();
		 initProgressDialog();
		 initDeviceListDialog();
		 initWidget();
		 
	}


	
	private void overflowMenuInActionBar(){
		 try {
		        ViewConfiguration config = ViewConfiguration.get(this);
		        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		        if(menuKeyField != null) {
		            menuKeyField.setAccessible(true);
		            menuKeyField.setBoolean(config, false);
		        }
		    } catch (Exception ex) {
		        // 무시한다. 3.x 이 예외가 발생한다.
		    	// 또, 타블릿 전용으로 만들어진 3.x 버전의 디바이스는 보통 하드웨어 버튼이 존재하지 않는다. 
		    }
	}
	
	
	@Override
	protected void onPause() {
		mClient.cancelScan(getApplicationContext());
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	enableBluetooth();

		
	}
	
	private void initProgressDialog() {
		 mLoadingDialog = new ProgressDialog(this);
		 mLoadingDialog.setCancelable(false);
	}
	
	private void initWidget() {
		mTextView = (TextView) findViewById(R.id.textViewTerminal);
		mTextView.setMovementMethod(new ScrollingMovementMethod());

		mEditTextInput = (EditText) findViewById(R.id.editTextInput);
		sendText1 = (EditText) findViewById(R.id.sendText1);
		sendText2 = (EditText) findViewById(R.id.sendText2);

		mButtonSend = (Button) findViewById(R.id.buttonSend);
		mButtonSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendStringData(mEditTextInput.getText().toString());
				mEditTextInput.setText("");
			}
		});

		mButtonSend2 = (Button) findViewById(R.id.buttonSend2);
		mButtonSend2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendStringData(sendText1.getText().toString()+","+sendText2.getText().toString());
				sendText1.setText("");
				sendText2.setText("");
			}
		});


	}
	
	private void initDeviceListDialog() {
		mDeviceArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_device);
		ListView listView = new ListView(getApplicationContext());
		listView.setAdapter(mDeviceArrayAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String item =  (String) parent.getItemAtPosition(position); 
				for(BluetoothDevice device : mBluetoothDevices) {
					if(item.contains(device.getAddress())) {
						connect(device);
						mDeviceListDialog.cancel();
					}
				}
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("연결할 기기를 선택하세요");
		builder.setView(listView);
		builder.setPositiveButton("검색",
		 new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int id) {
			  scanDevices();
		  }
		 });
		mDeviceListDialog = builder.create();
		mDeviceListDialog.setCanceledOnTouchOutside(false);
	}
	
	private void addDeviceToArrayAdapter(BluetoothDevice device) {
		if(mBluetoothDevices.contains(device)) { 
			mBluetoothDevices.remove(device);
			mDeviceArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
		}
			mBluetoothDevices.add(device);
			mDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress() );
			mDeviceArrayAdapter.notifyDataSetChanged();
		
	}
	
	
	
	
	
	private void enableBluetooth() {
		BluetoothSerialClient btSet =  mClient;
		btSet.enableBluetooth(this, new OnBluetoothEnabledListener() {
			@Override
			public void onBluetoothEnabled(boolean success) {
				if(success) {
					getPairedDevices();
				} else {
					finish();
				}
			}
		});
	}
	
	private void addText(String text) {
	    mTextView.append(text);
	    final int scrollAmount = mTextView.getLayout().getLineTop(mTextView.getLineCount()) - mTextView.getHeight();
	    if (scrollAmount > 0)
	    	mTextView.scrollTo(0, scrollAmount);
	    else
	    	mTextView.scrollTo(0, 0);
	}
	
	
	private void getPairedDevices() {
		Set<BluetoothDevice> devices =  mClient.getPairedDevices();
		for(BluetoothDevice device: devices) {
			addDeviceToArrayAdapter(device);
		}
	}
	
	private void scanDevices() {
		BluetoothSerialClient btSet = mClient;
		btSet.scanDevices(getApplicationContext(), new OnScanListener() {
			String message ="";
			@Override
			public void onStart() {
				Log.d("Test", "Scan Start.");
				mLoadingDialog.show();
				message = "Scanning....";
				mLoadingDialog.setMessage("Scanning....");
				mLoadingDialog.setCancelable(true);
				mLoadingDialog.setCanceledOnTouchOutside(false);
				mLoadingDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						BluetoothSerialClient btSet = mClient;
						btSet.cancelScan(getApplicationContext());
					}
				});
			}
			
			@Override
			public void onFoundDevice(BluetoothDevice bluetoothDevice) {
				addDeviceToArrayAdapter(bluetoothDevice);
				message += "\n" + bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress();
				mLoadingDialog.setMessage(message);
			}
			
			@Override
			public void onFinish() {
				Log.d("Test", "Scan finish.");
				message = "";
				mLoadingDialog.cancel();
				mLoadingDialog.setCancelable(false);
				mLoadingDialog.setOnCancelListener(null);
				mDeviceListDialog.show();
			}
		});
	}
	
	
	private void connect(BluetoothDevice device) {
		mLoadingDialog.setMessage("연결중......");
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
		BluetoothSerialClient btSet =  mClient;
		btSet.connect(getApplicationContext(), device, mBTHandler);
	}
	
	private BluetoothStreamingHandler mBTHandler = new BluetoothStreamingHandler() {
		ByteBuffer mmByteBuffer = ByteBuffer.allocate(1024);
		
		@Override
		public void onError(Exception e) {
			mLoadingDialog.cancel();
			addText("Messgae : Connection error - " +  e.toString() + "\n");
			mMenu.getItem(0).setTitle("블루투스 기기 검색");
		}
		
		@Override
		public void onDisconnected() {
			mMenu.getItem(0).setTitle("블루투스 기기 검색");
			mLoadingDialog.cancel();
			addText("Messgae : Disconnected.\n");
		}
		@Override //기기에서 보내는 값을 받는 부분
		public void onData(byte[] buffer, int length) {
			if(length == 0) return;
			if(mmByteBuffer.position() + length >= mmByteBuffer.capacity()) {
				ByteBuffer newBuffer = ByteBuffer.allocate(mmByteBuffer.capacity() * 2);
				newBuffer.put(mmByteBuffer.array(), 0,  mmByteBuffer.position());
				mmByteBuffer = newBuffer;
			}
			mmByteBuffer.put(buffer, 0, length);
			if(buffer[length - 1] == '\0') {
				addText(mClient.getConnectedDevice().getName() + " : " +
						new String(mmByteBuffer.array(), 0, mmByteBuffer.position()) + '\n');
				temp = new String(mmByteBuffer.array(), 0, mmByteBuffer.position()).substring(0,5);
				humi = new String(mmByteBuffer.array(), 0, mmByteBuffer.position()).substring(6,11);
				human_detect = new String(mmByteBuffer.array(), 0, mmByteBuffer.position()).substring(12,13);
				Door_detect = new String(mmByteBuffer.array(), 0, mmByteBuffer.position()).substring(14,15);
				PanPower = new String(mmByteBuffer.array(),0,mmByteBuffer.position()).substring(16);

				TextView textView4 = (TextView)findViewById(R.id.textView4);
				textView4.setText(temp);
				TextView textView = (TextView)findViewById(R.id.textView);
				textView.setText(humi);
				TextView textView6 = (TextView)findViewById(R.id.textView6);
				TextView textView7 = (TextView)findViewById(R.id.textView7);
				TextView textView15 = (TextView)findViewById(R.id.textView15);
				textView15.setText(PanPower);

				if(human_detect.equals("0"))
				{
					textView6.setText("화장실에 사람이 없습니다.");
				}else if(human_detect.equals("1"))
				{
					textView6.setText("화장실에 사람이 있습니다!");
				}

				if(Door_detect.equals("0"))
				{
					textView7.setText("화장실 문이 열려있습니다!");
				}else if(Door_detect.equals("1"))
				{
					textView7.setText("화장실 문이 닫혀 있습니다.");
				}


				mmByteBuffer.clear();
			}
		}
		
		@Override
		public void onConnected() {
			addText("Messgae : Connected. " + mClient.getConnectedDevice().getName() + "\n");
			mLoadingDialog.cancel();
			mMenu.getItem(0).setTitle("연결끊기");
		}
	};
	
	public void sendStringData(String data) {	//휴대폰에서 아두이노로 전송
		data += '\0';
		byte[] buffer = data.getBytes();
		if(mBTHandler.write(buffer)) {
			addText("Me : " + data + '\n');
		}
	}
	
	protected void onDestroy() {
		super.onDestroy();
		mClient.claer();
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean connect = mClient.isConnection();
		if(item.getItemId() == R.id.action_connect) {
			if (!connect) {
				mDeviceListDialog.show();
			} else {
				mBTHandler.close();
			}
			return true;
		} else {
			showCodeDlg();
			return true;
		}
	}

	private void showCodeDlg() {
		TextView codeView = new TextView(this);
		codeView.setText(Html.fromHtml(readCode()));
		codeView.setMovementMethod(new ScrollingMovementMethod());
		codeView.setBackgroundColor(Color.parseColor("#202020"));
		new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_DialogWhenLarge)
		.setView(codeView)
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}
	
	private String readCode() {
		 try {
			InputStream is = getAssets().open("HC_06_Echo.txt");
			int length = is.available();
			byte[] buffer = new byte[length];
			is.read(buffer);
			is.close();
			String code = new String(buffer);
			buffer = null;
			return code;
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return "";
	}
	

}
























