package com.example.controlrotom;

import android.annotation.SuppressLint;
import android.os.Bundle;

//01-----------------------------------------------------------------------------------------------------------------------------------
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

//01-----------------------------------------------------------------------------------------------------------------------------------

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    //02-----------------------------------------------------------------------------------------------------------------------------------
    private static final String TAG = "MainActivity";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice DispositivoSeleccionado;
    private ConnectedThread MyConexionBT;
    private ArrayList<String> mNameDevices = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;

    ImageButton btnBluetooth, btnUp, btnDown, btnLeft, btnRigth, btnDesconectar, btnConectar, btnInfo, btnLight,btnSound;
    FloatingActionButton indicator;
    Spinner devices;

    boolean ligth = false, sound = false;
    //02-----------------------------------------------------------------------------------------------------------------------------------

    //03-----------------------------------------------------------------------------------------------------------------------------------
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestBluetoothConnectPermission();
        requestLocationPermission();

        btnBluetooth = findViewById(R.id.btnBluetooth);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRigth = findViewById(R.id.btnRigth);
        indicator = findViewById(R.id.indicator);
        devices = findViewById(R.id.devices);
        btnConectar = findViewById(R.id.btnConectar);
        btnDesconectar = findViewById(R.id.btnDesconectar);
        btnInfo = findViewById(R.id.btnInfo);
        btnLight = findViewById(R.id.btnLight);
        btnSound = findViewById(R.id.btnSound);


        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mNameDevices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devices.setAdapter(deviceAdapter);

        String mesage = "\nNOMBRE: Control SimiRobots \n\nVERSIÓN: 1.3.0 \n\nFECHA DE VERSIÓN: 29 de Julio 2024 \n\nCREADOR: Oscar Julián Castillo Mateus";

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Informacion de la Aplicación").setMessage(mesage).setPositiveButton("Aceptar", null).setCancelable(false).show();
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Buscando...", Toast.LENGTH_SHORT).show();
                DispositivosVinculados();
            }
        });

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Conectando...", Toast.LENGTH_SHORT).show();
                ConectarDispBT();
            }
        });

        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Desconectando...", Toast.LENGTH_SHORT).show();
                if (btSocket!=null)
                {
                    try {btSocket.close();indicator.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.red));}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();}
                }
            }
        });

        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ligth) {
                    if (MyConexionBT != null) {
                        MyConexionBT.write('X');
                        Toast.makeText(getBaseContext(), "Luces encendidas...", Toast.LENGTH_SHORT).show();
                        ligth = true;
                        btnLight.setImageResource(R.drawable.icon_flashlight_off_24);

                    } else {
                        Log.e("MainActivity", "mConnectedThread is null");
                    }
                }else{
                    if (MyConexionBT != null) {
                        MyConexionBT.write('Y');
                        Toast.makeText(getBaseContext(), "Luces apagadas...", Toast.LENGTH_SHORT).show();
                        ligth = false;
                        btnLight.setImageResource(R.drawable.icon_flashlight_on_24);

                    } else {
                        Log.e("MainActivity", "mConnectedThread is null");
                    }
                }
            }
        });
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sound) {
                    if (MyConexionBT != null) {
                        MyConexionBT.write('W');
                        Toast.makeText(getBaseContext(), "sonido encendido...", Toast.LENGTH_SHORT).show();
                        sound = true;
                        btnSound.setImageResource(R.drawable.icon_music_off_24);
                    } else {
                        Log.e("MainActivity", "mConnectedThread is null");
                    }
                }else{
                    if (MyConexionBT != null) {
                        MyConexionBT.write('Z');
                        Toast.makeText(getBaseContext(), "sonido apagado...", Toast.LENGTH_SHORT).show();
                        sound = false;
                        btnSound.setImageResource(R.drawable.icon_music_note_24);
                    } else {
                        Log.e("MainActivity", "mConnectedThread is null");
                    }
                }
            }
        });

        btnUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // El botón se presiona
                        if (MyConexionBT != null) {
                            MyConexionBT.write('F');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // El botón se suelta
                        if (MyConexionBT != null) {
                            MyConexionBT.write('S');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;
                }

                return false;
            }
        });

        btnDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // El botón se presiona
                        if (MyConexionBT != null) {
                            MyConexionBT.write('B');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // El botón se suelta
                        if (MyConexionBT != null) {
                            MyConexionBT.write('S');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;
                }

                return false;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // El botón se presiona
                        if (MyConexionBT != null) {
                            MyConexionBT.write('L');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // El botón se suelta
                        if (MyConexionBT != null) {
                            MyConexionBT.write('S');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;
                }

                return false;
            }
        });

        btnRigth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // El botón se presiona
                        if (MyConexionBT != null) {
                            MyConexionBT.write('R');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // El botón se suelta
                        if (MyConexionBT != null) {
                            MyConexionBT.write('S');
                        } else {
                            Log.e("MainActivity", "mConnectedThread is null");
                        }
                        return true;
                }

                return false;
            }
        });

        //03-----------------------------------------------------------------------------------------------------------------------------------
    }
    //04-----------------------------------------------------------------------------------------------------------------------------------
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == MainActivity.REQUEST_ENABLE_BT) {
                        Log.d(TAG, "ACTIVIDAD REGISTRADA");
                        //Toast.makeText(getBaseContext(), "ACTIVIDAD REGISTRADA", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public void DispositivosVinculados() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo.");
            finish();
            return;
        }

        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            someActivityResultLauncher.launch(enableBtIntent);
        }

        devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DispositivoSeleccionado = getBluetoothDeviceByName(mNameDevices.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                DispositivoSeleccionado = null;
            }
        });

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mNameDevices.add(device.getName());
            }
            deviceAdapter.notifyDataSetChanged();
        } else {
            showToast("No hay dispositivos Bluetooth emparejados.");
        }
    }

    // Agrega este método para solicitar el permiso
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
    }

    // Agrega este método para solicitar el permiso
    private void requestBluetoothConnectPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso concedido, ahora puedes utilizar funciones de Bluetooth que requieran BLUETOOTH_CONNECT");
            } else {
                Log.d(TAG, "Permiso denegado, debes manejar este caso según tus necesidades");
            }
        }
    }

    private BluetoothDevice getBluetoothDeviceByName(String name) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " ----->>>>> ActivityCompat.checkSelfPermission");
        }
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }
    private void ConectarDispBT() {
        if (DispositivoSeleccionado == null) {
            showToast("Selecciona un dispositivo Bluetooth.");
            return;
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            btSocket = DispositivoSeleccionado.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
            btSocket.connect();
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();
            showToast("Conexión exitosa.");
            indicator.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
        } catch (IOException e) {
            showToast("Error al conectar con el dispositivo.");
            indicator.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.red));
        }
    }

    private class ConnectedThread extends Thread {
        private final OutputStream mmOutStream;
        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                showToast("Error al crear el flujo de datos.");
            }

            mmOutStream = tmpOut;
        }
        public void write(char input) {
            //byte msgBuffer = (byte)input;
            try {
                mmOutStream.write((byte)input);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    //04-----------------------------------------------------------------------------------------------------------------------------------
}