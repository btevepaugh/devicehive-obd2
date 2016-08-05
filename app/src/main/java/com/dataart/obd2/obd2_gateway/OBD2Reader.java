package com.dataart.obd2.obd2_gateway;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.google.common.util.concurrent.Runnables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Nikolay Khabarov on 8/5/16.
 */

public abstract class OBD2Reader implements Runnable{
    private final static int READ_INTERVAL = 1000;
    private final static int READ_ERROR_INTERVAL = 5000;
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private boolean isStarted = false;
    private String mDeviceMac;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mSocket = null;
    private boolean mObd2Init = false;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ObdResetCommand mObdResetCommand = new ObdResetCommand();
    private EchoOffCommand mEchoOffCommand = new EchoOffCommand();
    private LineFeedOffCommand mLineFeedOffCommand = new LineFeedOffCommand();
    private SelectProtocolCommand mSelectProtocolCommand = new SelectProtocolCommand(ObdProtocols.AUTO);
    private RPMCommand mRPMCommand = new RPMCommand();

    final Handler mHandler = new Handler();

    public enum Status {
        STATUS_DISCONNECTED,
        STATUS_BLUETOOTH_CONNECTING,
        STATUS_OBD2_CONNECTING,
        STATUS_OBD2_LOOPING_DATA
    }

    public OBD2Reader(String mac) {
        mDeviceMac = mac;
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = btAdapter.getRemoteDevice(mDeviceMac);
    }

    public synchronized void start() {
        if (isStarted == false) {
            mHandler.postDelayed(this, READ_INTERVAL);
            isStarted = true;
            statusCallback(Status.STATUS_OBD2_CONNECTING);
        }
    }

    public synchronized void stop() {
        isStarted = false;
        mHandler.removeCallbacks(this);
    }

    private synchronized void nextItteration(boolean success) {
        if (!isStarted) {
            return;
        }
        mHandler.postDelayed(this, success ? READ_INTERVAL : READ_ERROR_INTERVAL);
    }

    private void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
            statusCallback(Status.STATUS_DISCONNECTED);
        }
    }

    private boolean intteration() {
        if (mSocket == null) {
            statusCallback(Status.STATUS_BLUETOOTH_CONNECTING);
            try {
                mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                mSocket.connect();
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
                closeSocket();
                return false;
            }
            mObd2Init = false;
            statusCallback(Status.STATUS_OBD2_CONNECTING);
        }

        if (!mObd2Init) {
            try {
                mObdResetCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "ObdResetCommand " + mObdResetCommand.getResult());
                mEchoOffCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "EchoOffCommand " + mEchoOffCommand.getResult());
                mLineFeedOffCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "LineFeedOffCommand " + mLineFeedOffCommand.getResult());
                mSelectProtocolCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "SelectProtocolCommand " + mSelectProtocolCommand.getResult());
            } catch (Exception e) {
                e.printStackTrace();
                closeSocket();
                return false;
            }
            mObd2Init = true;
            statusCallback(Status.STATUS_OBD2_LOOPING_DATA);
        }

        try {
            mRPMCommand.run(mInputStream,  mOutputStream);
            Log.i("tag", "RPMCommand " + mRPMCommand.getResult());
            testCallback("RPM: " + String.valueOf(mRPMCommand.getRPM()));
        } catch (Exception e) {
            e.printStackTrace();
            closeSocket();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        nextItteration(intteration());
    }

    protected abstract void statusCallback(Status status);
    protected abstract void testCallback(String text);
}