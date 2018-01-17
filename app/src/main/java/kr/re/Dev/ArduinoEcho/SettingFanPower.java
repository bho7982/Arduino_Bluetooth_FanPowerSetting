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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by DDil on 2016-12-15.
 */

public class SettingFanPower extends MainActivity {
    @Override
    public void sendStringData(String data) {
        super.sendStringData(data);
    }

    private EditText editText2;
    private EditText editText;
    private Button SendCommand;
    private Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fan_power);
        b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(), // 현재 화면의 제어권자
                        MainActivity.class); // 다음 넘어갈 클래스 지정
                startActivity(intent); // 다음 화면으로 넘어간다
            }
        });
        initWidget();
    }
    private void initWidget() {

        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);

        SendCommand = (Button) findViewById(R.id.button3);

        SendCommand.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){

                sendStringData(editText.getText().toString() + editText2.getText().toString());
                editText.setText("");
                editText2.setText("");
            }
        });


    }
}
