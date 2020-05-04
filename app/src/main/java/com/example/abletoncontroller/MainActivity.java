package com.example.abletoncontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Socket s;
    private Switch socketSwitch;
    private Intent intent;
    private Thread sockThread;
    ClientService mService;
    boolean mBound = false;

    private enum Buttons {
        PLAY(1), STOP(2), RECORD(3), CUE(4), NUM_BUTTONS(5);

        public final int value;

        private Buttons(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ClientService.LocalBinder binder = (ClientService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socketSwitch = (Switch) findViewById(R.id.switch_sock);
        Boolean switchState = socketSwitch.isChecked();
        intent = new Intent(this, ClientService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void tearDown(){
        mService.sendMessage(100);
        unbindService(connection);
        //stopService(intent);
        mBound = false;
        socketSwitch.setText(R.string.switch_disconnect_text);
        Toast.makeText(this,"Tearing down", Toast.LENGTH_SHORT).show();
    }

    public void connectSock(View view) throws IOException {
        if (socketSwitch.isChecked() && !mService.isConnected()) {
            /**
             * calls SocketService onStartCommand() but this would run on the main thread or UIthread.
             * This is fine but not when using sockets.
             */
            socketSwitch.setChecked(false);
            //tearDown();
        } else if (socketSwitch.isChecked()) {
            intent = new Intent(this, ClientService.class);
            startService(intent);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);

        } else {
           socketSwitch.setChecked(false);
           tearDown();
        }
    }

    private void send(int msg) {
        if (mBound && socketSwitch.isChecked() && mService.isConnected()) {
            mService.sendMessage(msg);
            Log.d("tag", "Sending play message " + mBound);
        } else {
            Log.d("tag", "Not bound to any service connection " + mBound);
            return;
        }
    }

    public void sendPlay(View view) {
        send(Buttons.PLAY.getValue());
    }

    public void sendStop(View view) {
        send(Buttons.STOP.getValue());
    }

    public void sendRecord(View view) {
        send(Buttons.RECORD.getValue());
    }

    public void sendCreateCue(View view) {
        send(Buttons.CUE.getValue());
    }
}
