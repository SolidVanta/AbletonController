package com.example.abletoncontroller;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientService extends Service {
    private ServiceHandler mServiceHandler;
    private Socket sock;
    private static String host = "192.168.1.227";
    private static final int port = 3490;
    private final IBinder binder = new LocalBinder();
    private SocketAddress sockaddr;
    private int threadID;
    private OutputStream oS;
    private DataOutputStream doS;

    public class LocalBinder extends Binder {
        ClientService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ClientService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public Boolean sendMessage(int message) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = message;
        msg.obj = sock;
        Boolean ans = mServiceHandler.sendMessage(msg);
        Log.d("tag", "Successfully sent message " + ans);
        return ans;
    }

    private final class ServiceHandler extends Handler {

        private boolean mConnect;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Define how to handle incoming messages here
        @Override
        public void handleMessage(Message message) {
            switch (message.arg1) {
                case 0:
                    Log.d("tag", "POST: Option 0");
                    break;
                case 1:
                    sendMsgToServer("Hello from the other side!");
                    break;
                case 2:
                    sendMsgToServer("Stop in the name of love!");
                    break;
                case 100:
                    try {
                        if (sock != null) {
                            sock.close();
                        }
                        Log.d("tag", "Closed socket");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (doS != null) {
                        try {
                            doS.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    stopSelf(threadID);
                    Log.d("tag", "Shutting down service: "+ threadID);
                    break;
                default:
            }

        }
    }

    private void sendMsgToServer(String message) {
        try {
            if (!sock.isClosed()) {
                sock.setKeepAlive(true);
                pack(sock, message);
                Log.d("tag", "Socket is live");
            } else {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pack(Socket sock, String msg) {
        try {
            // get the output stream from the socket.
            oS= sock.getOutputStream();
            // create a data output stream from the output stream so we can send data through it
            doS = new DataOutputStream(oS);
            // write the message we want to send
            String str = msg;
            byte[] bytes = str.getBytes("ASCII");
            send(oS, doS, bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void send(OutputStream oS, DataOutputStream doS, byte[] bytes) throws IOException {
        doS.write(bytes, 0, bytes.length);
        doS.flush(); // send the message
        //dataOutputStream.close(); // close the output stream when we're done.
        //Log.d("tag", "Closing socket and terminating program.");
    }

    public void onCreate() {
        super.onCreate();
        HandlerThread mHandlerThread = new HandlerThread("ClientService.HandlerThread");
        mHandlerThread.start();
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        boolean post = mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sock = new Socket(host, port);
                    sock.setKeepAlive(true);
                    sockaddr = sock.getRemoteSocketAddress();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadID = startId;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    public Boolean isConnected() {
        if (sock != null) {
            return sock.isConnected();
        } else return false;

    }
}
