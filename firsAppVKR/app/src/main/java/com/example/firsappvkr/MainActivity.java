package com.example.firsappvkr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ImageButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemClickListener,
        View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_CODE_LOC = 1;
    private static final int REQ_ENABLE_BT = 10;
    private static final int BT_BOUNDED = 21; //сопряженные устройства
    private static final int BT_SEARCH = 22; // найденные устройства
    private static final int LED_RED = 30;
    private static final int LED_GREEN = 31;
    private static final long DELAY_TIMER = 1000;

    private FrameLayout frameMessage;
    private LinearLayout frameControl;

    private RelativeLayout frameDataControl;
    private Switch switchRedLed;
    private Switch switchGreenLed;
    private Button btnDisconnect;

    private Switch switchEnableBt;
    private Button btnEnableSearch;
    private ProgressBar pbProgress;
    private ListView listBtDevice;

    private LinearLayout frameListDevices;
    public ListView lvListDevicesAnalis;

    private LinearLayout frame_control_list;
    private EditText textNameDevices;
    private ImageButton btnAddDev;
    private ImageButton btnDeleteDev;
    private ImageButton btnNext;

    private ImageButton btnNextStep;

    //test_test_test_test_test
    private Button btnTest;

    private BluetoothAdapter bluetoothAdapter;

    private BtListAdapter listAdapter;

    private ArrayList<BluetoothDevice> bluetoothDevice;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private ProgressDialog progressDialog;

    private GraphView gvGraph;
    private LineGraphSeries series; //saving point graph
    private LineGraphSeries seriesSave;
    private String lastSensorValue = ""; //последние значения с устройства
    private int xLastValue = 0;

    private Handler handler;
    private Runnable timer;
    private FunctionSumSin functionSumSin;
    private DevListAnalis devListAnalisl;
    private ArrayAdapter<String> adapter;

    public String chooseDev = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devListAnalisl = new DevListAnalis();

        frameMessage = findViewById(R.id.frame_message);
        frameControl = findViewById(R.id.frame_control);
        switchEnableBt = findViewById(R.id.switch_enable_bt);
        btnEnableSearch = findViewById(R.id.btn_enable_search);
        pbProgress = findViewById(R.id.pb_progress);
        listBtDevice = findViewById(R.id.lv_bt_device);

        frameListDevices = findViewById(R.id.frame_list_devices);
        lvListDevicesAnalis = findViewById(R.id.lv_list_devices_analis);

        frame_control_list = findViewById(R.id.frame_control_list);
        textNameDevices = findViewById(R.id.text_name_devices);
        btnAddDev = findViewById(R.id.ibtn_add_dev);
        btnDeleteDev = findViewById(R.id.ibtn_delete_dev);
        btnNext = findViewById(R.id.ibtn_next);
        btnNextStep = findViewById(R.id.btn_next_step);


        //test_test_test_test_test_test_test_test_test_test_test_test_
        btnTest = findViewById(R.id.btn_test);


        frameDataControl = findViewById(R.id.frameDataControl);
        switchRedLed = findViewById(R.id.switch_led_red);
        switchGreenLed = findViewById(R.id.switch_led_green);
        btnDisconnect = findViewById(R.id.btn_disconnect); //инициалиация объектов в activity main
        //объявление
        gvGraph = findViewById(R.id.gv_graph);
        series = new LineGraphSeries();
        seriesSave = new LineGraphSeries();

        gvGraph.addSeries(series);
        gvGraph.addSeries(seriesSave);
        gvGraph.getViewport().setMinX(0);
        gvGraph.getViewport().setMaxX(50);

        gvGraph.getViewport().setXAxisBoundsManual(true);
        gvGraph.getViewport().setYAxisBoundsManual(true);


        //присвоение обработчика событий
        btnAddDev.setOnClickListener(this);
        btnDeleteDev.setOnClickListener(this);
        lvListDevicesAnalis.setOnItemClickListener(this);


        switchEnableBt.setOnCheckedChangeListener(this);
        btnEnableSearch.setOnClickListener(this);
        listBtDevice.setOnItemClickListener(this);

        btnDisconnect.setOnClickListener(this);
        switchGreenLed.setOnCheckedChangeListener(this);
        switchRedLed.setOnCheckedChangeListener(this);


        btnAddDev.setOnClickListener(this);
        btnDeleteDev.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        btnTest.setOnClickListener(this);
        bluetoothDevice = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Соединение ...");
        progressDialog.setMessage("Пожалуйста, подождите ...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate" + getString(R.string.bluetooth_not_supported));
            finish();
        }
        if (bluetoothAdapter.isEnabled()) {
            showFrameControls();
            switchEnableBt.setChecked(true);
            setListAdapter(BT_BOUNDED);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connectedThread != null) {
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        //метод жизненного цикла
        super.onDestroy();
        cancelTimer();
        unregisterReceiver(receiver);
        if (connectThread != null) {
            connectThread.cancel();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(btnEnableSearch)) {
            enabledSearch();
        } else if (v.equals(btnDisconnect)) {
            cancelTimer();
            //TODO отключение от устройства
            if (connectedThread != null) {
                connectedThread.cancel();
            }
            if (connectThread != null) {
                connectThread.cancel();
            }
            showFrameControls();//показываем список сопряженный устройств

        } else if (v.equals(btnTest)) {
            btnTest.setText("Стоп");
            testStart();
        } else if (v.equals(btnAddDev)) {
            String str = textNameDevices.getText().toString();
            if (str.equals(null) || str.equals("")) {
                Toast.makeText(MainActivity.this, "Неверный ввод данных", Toast.LENGTH_SHORT).show();
            } else {
                devListAnalisl.addDev(str);
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devListAnalisl.nameDev);
                lvListDevicesAnalis.setAdapter(adapter);
            }

        } else if (v.equals(btnDeleteDev)) {
            devListAnalisl.deleteDev(chooseDev);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devListAnalisl.nameDev);
            lvListDevicesAnalis.setAdapter(adapter);
            lvListDevicesAnalis.clearChoices();
            adapter.notifyDataSetChanged();
        } else if (v.equals(btnNext)) {
            if (!(chooseDev.equals(null) || chooseDev.equals(""))) {
                showFrameChart();
            } else {
                Toast.makeText(MainActivity.this, "Устройство не было выбрано из списка", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // отработка нажатия элемента списка
        if (parent.equals(listBtDevice)) {
            BluetoothDevice device = bluetoothDevice.get(position);
            if (device != null) {
                connectThread = new ConnectThread(device);
                connectThread.start();
                startTimer();
                // FastFourierTransformer fft =new FastFourierTransformer();
            }
        } else if (parent.equals(lvListDevicesAnalis)) {
            chooseDev = adapter.getItem(position);
            lvListDevicesAnalis.deferNotifyDataSetChanged();
            lvListDevicesAnalis.requestFocusFromTouch();
            lvListDevicesAnalis.setSelection(position);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(switchEnableBt)) {
            enableBT(isChecked);
            showFrameControls();
            if (!isChecked) {
                showFrameMessage();
            }
//            else if (buttonView.equals(switchRedLed)) {
//                //TODO включение/отключение диода red
//                //отправка команд
//                //enabled(LED_RED, isChecked);
//            } else if (buttonView.equals(switchGreenLed)) {
//                //TODO включение/отключение диода green
//               // enabled(LED_GREEN, isChecked);
//            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_ENABLE_BT) {
            if (requestCode == RESULT_OK && bluetoothAdapter.isEnabled()) {
                showFrameControls();
                setListAdapter(BT_BOUNDED);
            } else if (resultCode == RESULT_CANCELED) {
                enableBT(true);
            }
        }
    }

    private void showFrameMessage() {
        frameMessage.setVisibility(View.VISIBLE);
        frameControl.setVisibility(View.GONE);
        frameDataControl.setVisibility(View.GONE);
        frame_control_list.setVisibility(View.GONE);
        frameListDevices.setVisibility(View.GONE);
    }

    private void showFrameControls() {
        frameMessage.setVisibility(View.GONE);
        frameControl.setVisibility(View.VISIBLE);
        frameDataControl.setVisibility(View.GONE);
        frame_control_list.setVisibility(View.GONE);
        frameListDevices.setVisibility(View.GONE);
    }

    private void showFrameLedControls() {
        frameDataControl.setVisibility(View.VISIBLE);
        frameMessage.setVisibility(View.GONE);
        frameControl.setVisibility(View.GONE);
        frame_control_list.setVisibility(View.GONE);
        frameListDevices.setVisibility(View.GONE);
    }

    public void showFrameChooseDev() {
        frame_control_list.setVisibility(View.VISIBLE);
        frameListDevices.setVisibility(View.VISIBLE);
        frameControl.setVisibility(View.GONE);
        frameDataControl.setVisibility(View.GONE);
        frameMessage.setVisibility(View.GONE);
    }

    public void showFrameChart() {
        frameDataControl.setVisibility(View.VISIBLE);
        frame_control_list.setVisibility(View.GONE);
        frameListDevices.setVisibility(View.GONE);
        frameControl.setVisibility(View.GONE);
        frameMessage.setVisibility(View.GONE);

    }

    private void enableBT(boolean flag) {
        if (flag) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT);
        } else {
            bluetoothAdapter.disable();
        }
    }

    private void setListAdapter(int type) {
        bluetoothDevice.clear();
        int iconType = R.drawable.ic_bluetooth_bounded_devices;
        switch (type) {
            case BT_BOUNDED:
                bluetoothDevice = getBoundedDevices();
                iconType = R.drawable.ic_bluetooth_bounded_devices;
                break;
            case BT_SEARCH:
                iconType = R.drawable.ic_bluetooth_search_device;
                break;
        }
        listAdapter = new BtListAdapter(this, bluetoothDevice, iconType);
        listBtDevice.setAdapter(listAdapter);
    }

    private ArrayList<BluetoothDevice> getBoundedDevices() {
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> tmpArrayList = new ArrayList<>();
        if (deviceSet.size() > 0) {
            for (BluetoothDevice device : deviceSet) {
                tmpArrayList.add(device);
            }
        }
        return tmpArrayList;
    }

    private void enabledSearch() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        } else {
            accessLocationPermission();
            bluetoothAdapter.startDiscovery();
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    btnEnableSearch.setText("Остановить поиск"); //hardCod убрать
                    pbProgress.setVisibility(View.VISIBLE);
                    setListAdapter(BT_SEARCH);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    btnEnableSearch.setText("Начать поиск"); //hardCod убрать
                    pbProgress.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        bluetoothDevice.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private void accessLocationPermission() {
        int accessCoarseLocation = this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation = this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            this.requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOC:

                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                }
                break;
            default:
                return;
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private boolean success = false;

        public ConnectThread(BluetoothDevice device) {

            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);

                progressDialog.show();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {// catch(Exception e)
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                bluetoothSocket.connect();
                success = true;
                progressDialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Не могу соединиться!", Toast.LENGTH_SHORT).show();
                    }
                });
                cancel();
            }
            if (success) {
                //TODO создадим экзампляр класс приема и отправки сообщения
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFrameChart();
                        showFrameLedControls();
                    }
                });

            }
        }

        public boolean isConnected() {
            return bluetoothSocket.isConnected();
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean isConnected = false;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            isConnected = true;
        }


        @Override
        public void run() {
            // TODO считывание информации с устройства
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            StringBuffer buffer = new StringBuffer();
            StringBuffer sbConsole = new StringBuffer();
            while (isConnected) {
                try {
                    int bytes = bis.read();
                    buffer.append((char) bytes);
                    int eof = buffer.indexOf("\r\n");
                    if (eof > 0) {
                        sbConsole.append(buffer.toString());
                        lastSensorValue = buffer.toString();
                        buffer.delete(0, buffer.length());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (IOException e) {

                }
            }

            try { // разгрузка буферв
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write(String command) {
            byte[] bytes = command.getBytes();
            if (outputStream != null) {
                try {
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                isConnected = false;
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enabled(int led, boolean state) {
        if (connectedThread != null && connectThread.isConnected()) {
            String command = "";
            switch (led) {
                case LED_RED:
                    command = (state) ? "red on#" : "red off#";
                    break;
                case LED_GREEN:
                    command = (state) ? "green on#" : "green off#";
                    break;
            }
            connectedThread.write(command);
        }
    }

    private HashMap parseData(String data) {
        //TODO парсим входную информацию в мапу для дальнейгего вывода графиков
        if (data.indexOf('|') > 0) {
            HashMap map = new HashMap<>();
            String[] pairs = data.split("\\|");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                map.put(keyValue[0], keyValue[1]);
            }
            return map;
        }
        return null;
    }


    private void startTimer() {

        handler = new Handler();
        handler.postDelayed(timer = new Runnable() {
            @Override
            public void run() {
                HashMap dataSensor = parseData(lastSensorValue);
                if (dataSensor != null) {
                    if (dataSensor.containsKey("Temp") && dataSensor.containsKey("millis")) {
                        int temp = Integer.parseInt(dataSensor.get("Temp").toString());
                        series.appendData(new DataPoint(xLastValue, temp), true, 40);
                    }
                    xLastValue++;
                }
                handler.postDelayed(this, DELAY_TIMER);
            }
        }, DELAY_TIMER);
    }

    private void cancelTimer() {
        if (handler != null) {
            handler.removeCallbacks(timer);
        }
    }

    private void testStart() {
        functionSumSin = new FunctionSumSin();
        double[] spectr2 = functionSumSin.createSpectr(2);
        for (int i = 0; i < spectr2.length / 2; i++) {
            seriesSave.appendData(new DataPoint(i + 1, 0), false, 7500);
            seriesSave.appendData(new DataPoint(i + 1, spectr2[i]), false, 7500);
            seriesSave.appendData(new DataPoint(i + 1, 0), false, 7500);
        }
        gvGraph.addSeries(seriesSave);
        double[] spectr = functionSumSin.createSpectr(1);
        for (int i = 0; i < spectr.length / 2; i++) {
            series.appendData(new DataPoint(i + 1, 0), false, 7500);
            series.appendData(new DataPoint(i + 1, spectr[i]), false, 7500);
            series.appendData(new DataPoint(i + 1, 0), false, 7500);
        }
        gvGraph.getViewport().setScalable(true);
        gvGraph.addSeries(series);
        series.setColor(Color.GREEN);
        double max = 0;
        for (int i = 0; i < spectr.length; i++) {
            if (spectr[i] > max) {
                max = spectr[i];
            }
        }
        gvGraph.getViewport().setMaxY(max + 5);
    }
}
