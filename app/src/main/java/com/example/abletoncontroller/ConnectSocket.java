package com.example.abletoncontroller;

import android.os.AsyncTask;
import android.widget.Switch;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectSocket extends AsyncTask <Void, Void, String> {
    private Socket sock;
    private WeakReference<Switch> socketSwitch;

    ConnectSocket(Switch sw) {

        socketSwitch = new WeakReference<>(sw);
    }
    @Override
    protected String doInBackground(Void...voids) {

            try {
                sock = new Socket("192.168.1.227", 3490);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Successfully connected!";
    }

    protected void onPostExecute(String sw) {
        socketSwitch.get().setText(sw);
    }
    protected void onCancelled() {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
