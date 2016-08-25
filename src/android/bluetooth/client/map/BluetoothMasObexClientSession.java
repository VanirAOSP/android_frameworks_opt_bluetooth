/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.bluetooth.client.map;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.ObexTransport;
import javax.obex.ObexHelper;
import javax.obex.ResponseCodes;

class BluetoothMasObexClientSession {
    private static final String TAG = "BluetoothMasObexClientSession";

    private static final byte[] MAS_TARGET = new byte[] {
            (byte) 0xbb, 0x58, 0x2b, 0x40, 0x42, 0x0c, 0x11, (byte) 0xdb, (byte) 0xb0, (byte) 0xde,
            0x08, 0x00, 0x20, 0x0c, (byte) 0x9a, 0x66
    };

    private boolean DBG = true;

    static final int MSG_OBEX_CONNECTED = 100;
    static final int MSG_OBEX_DISCONNECTED = 101;
    static final int MSG_REQUEST_COMPLETED = 102;

    private static final int CONNECT = 0;
    private static final int DISCONNECT = 1;
    private static final int REQUEST = 2;

    private final ObexTransport mTransport;

    private final Handler mSessionHandler;

    private ClientSession mSession;

    private HandlerThread mThread;
    private Handler mHandler;

    private boolean mConnected;

    private static class ObexClientHandler extends Handler {
       WeakReference<BluetoothMasObexClientSession> mInst;

       ObexClientHandler(Looper looper, BluetoothMasObexClientSession inst) {
          super(looper);
          mInst = new WeakReference<BluetoothMasObexClientSession>(inst);
       }

       @Override
       public void handleMessage(Message msg) {
          BluetoothMasObexClientSession inst = mInst.get();
          if (!inst.connected() && msg.what != CONNECT) {
              Log.w(TAG, "Cannot execute " + msg + " when not CONNECTED.");
              return;
          }

          switch (msg.what) {
              case CONNECT:
                  inst.connect();
                  break;

              case DISCONNECT:
                  inst.disconnect();
                  break;

              case REQUEST:
                  inst.executeRequest((BluetoothMasRequest) msg.obj);
                  break;
          }
       }
    }

    public BluetoothMasObexClientSession(ObexTransport transport, Handler handler) {
        mTransport = transport;
        mSessionHandler = handler;
    }

    public void start() {
        if (DBG) Log.d(TAG, "start called.");
        if (mConnected) {
            if (DBG) Log.d(TAG, "Already connected, nothing to do.");
            return;
        }

        // Start a thread to handle messages here.
        mThread = new HandlerThread("BluetoothMasObexClientSessionThread");
        mThread.start();
        mHandler = new ObexClientHandler(mThread.getLooper(), this);

        // Connect it to the target device via OBEX.
        mHandler.obtainMessage(CONNECT).sendToTarget();
    }

    public boolean makeRequest(BluetoothMasRequest request) {
        if (DBG) Log.d(TAG, "makeRequest called with: " + request);

        boolean status = mHandler.sendMessage(mHandler.obtainMessage(REQUEST, request));
        if (!status) {
            Log.e(TAG, "Adding messages failed, state: " + mConnected);
            return false;
        }
        return true;
    }

    public void stop() {
        if (DBG) Log.d(TAG, "stop called...");

        mThread.quit();
        disconnect();
    }

    private void connect() {
        try {
            mSession = new ClientSession(mTransport);

            HeaderSet headerset = new HeaderSet();
            headerset.setHeader(HeaderSet.TARGET, MAS_TARGET);

            headerset = mSession.connect(headerset);

            if (headerset.getResponseCode() == ResponseCodes.OBEX_HTTP_OK) {
                mConnected = true;
                mSessionHandler.obtainMessage(MSG_OBEX_CONNECTED).sendToTarget();
            } else {
                disconnect();
            }
        } catch (IOException e) {
            disconnect();
        }
    }

<<<<<<< HEAD
        private void disconnect() {
            Log.w(TAG, "disconnect: ");
=======
    private void disconnect() {
        if (mSession != null) {
>>>>>>> android-7.0.0_r1
            try {
                mSession.disconnect(null);
            } catch (IOException e) {
            }

            try {
                mSession.close();
            } catch (IOException e) {
                Log.w(TAG, "handled disconnect exception:", e);
            }
        }
<<<<<<< HEAD

        private void shutdown() {
            Log.w(TAG, "shutdown ");
            mInterrupted = true;
            interrupt();
        }
    }
=======
>>>>>>> android-7.0.0_r1

        mConnected = false;
        mSessionHandler.obtainMessage(MSG_OBEX_DISCONNECTED).sendToTarget();
    }

    private void executeRequest(BluetoothMasRequest request) {
        try {
            request.execute(mSession);
            mSessionHandler.obtainMessage(MSG_REQUEST_COMPLETED, request).sendToTarget();
        } catch (IOException e) {
            if (DBG) Log.d(TAG, "Request failed: " + request);

<<<<<<< HEAD
    public void stop() {
        if (mClientThread != null) {
            mClientThread.shutdown();

            Thread t = new Thread(new Runnable() {
                public void run () {
                    Log.d(TAG, "Spawning a new thread for stopping obex session");
                    try {
                        mClientThread.join();
                        mClientThread = null;
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Interrupted while waiting for thread to join");
                    }
                }
            });
            t.start();
            Log.d(TAG, "Exiting from the stopping thread");
=======
            // Disconnect to cleanup.
            disconnect();
>>>>>>> android-7.0.0_r1
        }
    }


<<<<<<< HEAD
        if (((BluetoothMapTransport)mTransport).isSrmSupported()) {
            Log.d(TAG, "Client is srm capable");
            if (request instanceof BluetoothMasRequestGetFolderListing ||
                request instanceof BluetoothMasRequestGetFolderListingSize ||
                request instanceof BluetoothMasRequestGetMessagesListing ||
                request instanceof BluetoothMasRequestGetMessage ||
                request instanceof BluetoothMasRequestPushMessage ||
                request instanceof BluetoothMasRequestGetMessagesListingSize) {
                mClientThread.mSession.setLocalSrmStatus(true);
            }

            if (request instanceof BluetoothMasRequestSetMessageStatus ||
                request instanceof BluetoothMasRequestUpdateInbox||
                request instanceof BluetoothMasRequestSetNotificationRegistration) {
                mClientThread.mSession.setLocalSrmStatus(false);
            }
        } else {
                Log.d(TAG, "Client is not srm capable");
        }

        return mClientThread.schedule(request);
=======
    private boolean connected() {
        return mConnected;
>>>>>>> android-7.0.0_r1
    }
}
