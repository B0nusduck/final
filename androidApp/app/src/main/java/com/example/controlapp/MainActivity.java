package com.example.controlapp;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{
    //buttons
    private Button connectionBtn;
    private ImageButton wheelForwardBtn;
    private ImageButton wheelBackwardBtn;
    private ImageButton wheelRightBtn;
    private ImageButton wheelLeftBtn;
    private ImageButton liftUpBtn;
    private ImageButton liftDownBtn;

    //connection, transmission, and receiving related variable
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private Thread transmissionThread = null;
    /** @noinspection FieldMayBeFinal*/
    private Queue<String> transmissionQueue = new LinkedList<>();
    private boolean continueRead = false;
    private String recivedString;

    //motor strength
    private final int maxMotorStrength = 500000; //max: 500.000
    private float motorPower = 1;
    private int motorStrength = Math.round(maxMotorStrength * motorPower);


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //lock screen in landscape mode for proper ui display
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //initialize wheel power input
        EditText motorPowerInput = findViewById(R.id.editTextWheelStrength);
        motorPowerInput.addTextChangedListener(new TextWatcher() {
            String OldValidText;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                OldValidText = s.toString();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                try
                {
                    int input = Integer.parseInt(s.toString());
                    if(input > 100){
                        input = 100;
                        motorPowerInput.setText("100");
                    }
                    if(input < 0){
                        input = 0;
                        motorPowerInput.setText("0");
                    }
                    motorPower = (float) input / 100;
                    motorStrength = Math.round(maxMotorStrength * motorPower);
                }
                catch(Exception e)
                {
                    motorPowerInput.setText(OldValidText);
                }

            }
        });

        //get bluetooth adapter for handling connection
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //initialize button and set button click/release event
        connectionBtn = findViewById(R.id.Connection);
        wheelForwardBtn = findViewById(R.id.wheelForward);
        wheelBackwardBtn = findViewById(R.id.wheelBackward);
        wheelRightBtn = findViewById(R.id.wheelRight);
        wheelLeftBtn = findViewById(R.id.wheelLeft);
        liftUpBtn = findViewById(R.id.liftUp);
        liftDownBtn = findViewById(R.id.liftDown);

        connectionBtn.setOnClickListener(v -> ConnectBtnClick(connectionBtn));

        wheelForwardBtn.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                WheelForwardBtnPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                WheelBtnRelease();
            }
            return true;
        });
        wheelBackwardBtn.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                WheelBackwardBtnPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                WheelBtnRelease();
            }
            return true;
        });
        wheelRightBtn.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                WheelRightBtnPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                WheelBtnRelease();
            }
            return true;
        });
        wheelLeftBtn.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                WheelLeftBtnPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                WheelBtnRelease();
            }
            return true;
        });

        liftUpBtn.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                LiftUpBtnPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                LiftBtnRelease();
            }
            return true;
        });
        liftDownBtn.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                LiftDownBtnPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                LiftBtnRelease();
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            Disconnect();
        }
    }

    //handling the return data of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //permission denied
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("required permission");
                builder.setMessage("this app require permission to use the device's" +
                        " bluetooth in order for it to work");
                builder.setNegativeButton("ok", (dialog, which) -> {});
                builder.create().show();
            }
    }

    //get device paired to current device
    /** @noinspection unused*/
    private Set<BluetoothDevice> GetPairedDevice(BluetoothAdapter bluetoothAdapter)
    {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED)
        {
            //request code is used to passed down to onRequestPermissionsResult to customize message
            //but because this app display a general message for unalloyed permissions result
            //these request code are there just to complete the function
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH}, 1);
            return null;
        }
        return bluetoothAdapter.getBondedDevices();
    }

    // Connecting to bluetooth device
    /** @noinspection SameParameterValue*/
    private void Connect(BluetoothAdapter bluetoothAdapter, String targetMacAddress)
    {
        Toast.makeText(this, "Connecting", Toast.LENGTH_LONG).show();
        //obtain the target device
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(targetMacAddress);
        try
        {
            //check and request permission before connect
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {
                //request code is used to passed down to onRequestPermissionsResult to customize message
                //but because this app display a general message for unalloyed permissions result
                //these request code are there just to complete the function
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH}, 2);
                return;
            }

            //id of default SPP socket
            UUID socketUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            //create a socket on the local bluetooth adapter connected to the target device
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(socketUUID);
            bluetoothSocket.connect();

            //will only sent/receive data after this point onward
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            continueRead = true;
            //listen to incoming data from the connected device
            //ReceiveDataListener();

            //notify user of successful connection
            Toast.makeText(this, "Bluetooth successfully connected", Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            //device support bluetooth but bluetooth is not on
            if (!bluetoothAdapter.isEnabled())
            {
                //request permission to turn on bluetooth
                startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
            //target device is off
            else if(!bluetoothSocket.isConnected()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("the target device failed to respond, try restarting the device" +
                                " or get closer for better connection")
                        .setTitle("Unable to connect");
                builder.setNegativeButton(R.string.ok, (dialog, which) -> {
                });
                builder.create().show();
            }
            // Device doesn't support Bluetooth classic
            else //noinspection ConstantValue
                if(bluetoothAdapter == null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("the device you're using do not support this type off bluetooth connection")
                        .setTitle("Bluetooth not supported");
                builder.setNegativeButton(R.string.ok, (dialog, which) -> {
                });
                builder.create().show();
            }
        }
    }

    private void Disconnect()
    {
        Toast.makeText(this, "Disconnecting", Toast.LENGTH_LONG).show();
        try
        {
            //close connection and dispose socket
            if (bluetoothSocket != null){
                bluetoothSocket.close();
                bluetoothSocket = null;
                Toast.makeText(this, "Bluetooth successfully disconnected", Toast.LENGTH_LONG).show();
            }
            //raise the stop flag to stop the data receiver
            continueRead = false;
            //dispose all related variable
            outputStream = null;
            inputStream = null;
            recivedString = null;
        }
        catch (IOException e)
        {
            //cant closing connection
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(e.getMessage())
                    .setTitle("Cant disconnected from remote device");
            builder.setNegativeButton(R.string.ok, (dialog, which) -> {
            });
            builder.create().show();
        }
    }

    //add all input into a queue before sent to avoid race condition on
    //the receiver side
    private void StartTransmission(){
        if(transmissionThread == null)
        {
            transmissionThread = new Thread(new Runnable()
            {
                boolean continueWrite;
                @Override
                public void run()
                {
                    continueWrite = !transmissionQueue.isEmpty();
                    while(continueWrite){
                        String data = transmissionQueue.poll();
                        TransmitData(data);
                        continueWrite = !transmissionQueue.isEmpty();
                        //prevent interleaving by prevent data from being write
                        //too fast 70ms is sufficient on test device, but using
                        //100 to account for potential difference when run in
                        //other device, also to round up number
                        try {
                            //noinspection BusyWait
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}

                    }
                }
            });
            transmissionThread.start();
        }
        if(!transmissionThread.isAlive())
            //noinspection CallToThreadRun
            transmissionThread.run();
    }

    private void TransmitData(String data)
    {
        if (outputStream == null)
        {
            runOnUiThread(()->{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Try connect or re-connect to the vehicle")
                        .setTitle("Cannot transmit data");
                builder.setNegativeButton(R.string.ok, (dialog, which) -> {});
                builder.create().show();
            });
            return;
        }
        data = data + "\r\n";
        byte[] bytes = data.getBytes();
        try
        {
            outputStream.write(bytes);

        }
        catch (IOException e)
        {
            if(Objects.equals(e.getMessage(), "Broken pipe"))
            {
                bluetoothSocket = null;
                //raise the stop flag to stop the data receiver
                continueRead = false;
                //dispose all related variable
                outputStream = null;
                inputStream = null;
                recivedString = null;
                //Disconnect();
                connectionBtn.setText(R.string.ConnectBtnOff);
                runOnUiThread(()->{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("the app has lost connection to the device")
                            .setTitle("Lost connection");
                    builder.setNegativeButton(R.string.ok, (dialog, which) -> {});
                    builder.create().show();
                });
            }
            else
            {
                runOnUiThread(()->{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(e.getMessage())
                            .setTitle("There seem to be some problem with the connection");
                    builder.setNegativeButton(R.string.ok, (dialog, which) -> {});
                    builder.create().show();
                });
            }

        }

    }

    /** @noinspection unused*/
    private void ReceiveDataListener()
    {
        Thread readThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes = 0; // length of messages

            // everytime the app get an incoming message read that message until
            // the first carriage return character indicating end of line
            // since the app transmission is formatted to be contained in 1
            // line the rest is discarded
            while (continueRead) {
                try {
                    buffer[bytes] = (byte) inputStream.read();
                    // locate the end of the sent string
                    //if (bytes > 0 && buffer[bytes-1] == '\r' && buffer[bytes] == '\n') {
                    if (bytes > 0 && buffer[bytes] == '\r') {
                        recivedString = new String(buffer, 0, bytes);
                        //display the received signal via a toast
                        runOnUiThread(()-> Toast.makeText(this, recivedString, Toast.LENGTH_LONG).show());
                        bytes = 0;
                    } else {
                        bytes++;
                    }

                } catch (IOException e) {
                    //input stream disconnected
                    break;
                }
            }
        });
        readThread.start();
    }

    public void ConnectBtnClick(Button connectionBtn)
    {
        if(bluetoothSocket == null || !bluetoothSocket.isConnected())
        {
            //connect to device with the given mac address
            String targetMacAddress = "D8:3A:DD:64:82:28";
            Connect(bluetoothAdapter, targetMacAddress);

            //check if connect success
            if(bluetoothSocket != null && bluetoothSocket.isConnected()){
                connectionBtn.setText(R.string.ConnectBtnOn);
            }
        }
        else
        {
            Disconnect();
            if(bluetoothSocket == null || !bluetoothSocket.isConnected()){
                connectionBtn.setText(R.string.ConnectBtnOff);
            }
        }
    }

    public void WheelBtnRelease()
    {
        wheelForwardBtn.setEnabled(true);
        wheelBackwardBtn.setEnabled(true);
        wheelRightBtn.setEnabled(true);
        wheelLeftBtn.setEnabled(true);
        if(bluetoothSocket != null && bluetoothSocket.isConnected()){
            transmissionQueue.offer("move w 0 0 0 0");
            StartTransmission();
        }
    }
    public void WheelForwardBtnPressed()
    {
        wheelForwardBtn.setEnabled(true);
        wheelBackwardBtn.setEnabled(false);
        wheelRightBtn.setEnabled(false);
        wheelLeftBtn.setEnabled(false);
        transmissionQueue.offer("move w "+motorStrength+" "+motorStrength+" "+motorStrength+" "+motorStrength);
        StartTransmission();
    }
    public void WheelBackwardBtnPressed()
    {
        wheelForwardBtn.setEnabled(false);
        wheelBackwardBtn.setEnabled(true);
        wheelRightBtn.setEnabled(false);
        wheelLeftBtn.setEnabled(false);
        transmissionQueue.offer("move w "+(-motorStrength)+" "+(-motorStrength)+" "+(-motorStrength)+" "+(-motorStrength));
        StartTransmission();
    }
    public void WheelRightBtnPressed()
    {
        wheelForwardBtn.setEnabled(false);
        wheelBackwardBtn.setEnabled(false);
        wheelRightBtn.setEnabled(true);
        wheelLeftBtn.setEnabled(false);
        transmissionQueue.offer("move w " + (-motorStrength) + " "+ motorStrength + " " + (-motorStrength) + " " + motorStrength);
        StartTransmission();
    }
    public void WheelLeftBtnPressed()
    {
        wheelForwardBtn.setEnabled(false);
        wheelBackwardBtn.setEnabled(false);
        wheelRightBtn.setEnabled(false);
        wheelLeftBtn.setEnabled(true);
        transmissionQueue.offer("move w " + motorStrength + " "+ (-motorStrength) + " " + motorStrength + " " + (-motorStrength));
        StartTransmission();
    }


    public void LiftBtnRelease()
    {
        liftUpBtn.setEnabled(true);
        liftDownBtn.setEnabled(true);
        if(bluetoothSocket != null && bluetoothSocket.isConnected())
        {
            transmissionQueue.offer("move l 0");
            StartTransmission();
        }

    }
    public void LiftUpBtnPressed()
    {
        liftUpBtn.setEnabled(true);
        liftDownBtn.setEnabled(false);
        transmissionQueue.offer("move l 1");
        StartTransmission();
    }
    public void LiftDownBtnPressed()
    {
        liftUpBtn.setEnabled(false);
        liftDownBtn.setEnabled(true);
        transmissionQueue.offer("move l -1");
        StartTransmission();
    }
}