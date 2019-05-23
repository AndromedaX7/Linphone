package org.linphone.activity;
/*
AboutFragment.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.linphone.FragmentsAvailable;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.LogCollectionUploadState;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.mediastream.Log;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Sylvain Berfini
 */
public class PhoneFragment extends Fragment implements OnClickListener {
    View sendLogButton = null;
    View resetLogButton = null;
    ImageView cancel;
    LinphoneCoreListenerBase mListener;
    private ProgressDialog progress;
    private boolean uploadInProgress;
    private static Session session;
    private static ChannelShell channelSell;
    private static List<String> commands;
    private static String TAG = "sshLog";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.phone, container, false);

        TextView aboutVersion = (TextView) view.findViewById(R.id.about_android_version);
        TextView aboutLiblinphoneVersion = (TextView) view.findViewById(R.id.about_liblinphone_version);
        aboutLiblinphoneVersion.setText(String.format(getString(R.string.about_liblinphone_version), LinphoneManager.getLc().getVersion()));
        try {
            aboutVersion.setText(String.format(getString(R.string.about_version), getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
        } catch (NameNotFoundException e) {
            Log.e(e, "cannot get version name");
        }

        cancel = (ImageView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        sendLogButton = view.findViewById(R.id.send_log);
        sendLogButton.setOnClickListener(this);
        sendLogButton.setVisibility(LinphonePreferences.instance().isDebugEnabled() ? View.VISIBLE : View.GONE);

        resetLogButton = view.findViewById(R.id.reset_log);
        resetLogButton.setOnClickListener(this);
        resetLogButton.setVisibility(LinphonePreferences.instance().isDebugEnabled() ? View.VISIBLE : View.GONE);

        mListener = new LinphoneCoreListenerBase() {
            @Override
            public void uploadProgressIndication(LinphoneCore lc, int offset, int total) {
            }

            @Override
            public void uploadStateChanged(LinphoneCore lc, LogCollectionUploadState state, String info) {
                if (state == LogCollectionUploadState.LogCollectionUploadStateInProgress) {
                    displayUploadLogsInProgress();
                } else if (state == LogCollectionUploadState.LogCollectionUploadStateDelivered || state == LogCollectionUploadState.LogCollectionUploadStateNotDelivered) {
                    uploadInProgress = false;
                    if (progress != null) progress.dismiss();
                    if (state == LogCollectionUploadState.LogCollectionUploadStateDelivered) {
                        sendLogs(LinphoneService.instance().getApplicationContext(), info);
                    }
                }
            }
        };
        commands = new ArrayList<String>();
        commands.add("export DISPLAY=:0 ");
        Button btnUrl = (Button) view.findViewById(R.id.btnURL);
        final EditText etUrt = (EditText) view.findViewById(R.id.etUrl);

        btnUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUrt.getText().toString();
                if (!url.isEmpty()) {
//                    commands.add("gnome-open "+url);
                    commands.add("config modify subscriber dn 4089 longdn "+url);
                    new AsyncSession().execute();

                }

            }
        });
        return view;
    }


    class AsyncSession extends AsyncTask<Void, String, ChannelShell> {

        private Session getSession() {
            if (session == null || !session.isConnected()) {
                android.util.Log.i(TAG, "begin creat session");
                session = connect("219.149.195.145", "admin", "Change_Me");
                return session;
            }
            android.util.Log.i(TAG, "session exist");
            return session;
        }

        private Session connect(String hostname, String username, String password) {

            JSch jSch = new JSch();

            try {

                session = jSch.getSession(username, hostname, 5022);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);


                android.util.Log.i(TAG, "begin connect");
                session.connect();
                android.util.Log.i(TAG, "connected");
                android.util.Log.i(TAG, "status session: " + session.isConnected() + " || " + session.hashCode());
            } catch (Exception e) {
                android.util.Log.e(TAG, "An error occurred while connecting to " + hostname + ": " + e);
            }
            android.util.Log.i(TAG, "server Info: getServerVersion =" + session.getServerVersion());
            android.util.Log.i(TAG, "server Info: getServerAliveCountMax =" + session.getServerAliveCountMax());
            return session;

        }

        private Channel getChannel() throws JSchException {

            if (channelSell == null || !channelSell.isConnected()) {
                try {
                    channelSell = (ChannelShell) getSession().openChannel("shell");
                    android.util.Log.i(TAG, "begin connect channel");

                    channelSell.connect();
                    android.util.Log.i(TAG, "channel Info: getID =" + channelSell.getId());

                    android.util.Log.i(TAG, "connected channel");
                    android.util.Log.i(TAG, "status channel: " + channelSell.isConnected() + " || " + channelSell.hashCode());

                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error while opening channelSell: " + e);


                }
            }
            return channelSell;
        }

        private void executeCommands(List<String> commands) {

            try {
                Channel channel = getChannel();
                android.util.Log.i(TAG, "Sending commands...");
                sendCommands(channel, commands);

                readChannelOutput(channel);
                android.util.Log.i(TAG, "Finished sending commands! ");

            } catch (Exception e) {
                android.util.Log.i(TAG, "An error ocurred during executeCommands: " + e);
            }
        }

        private void sendCommands(Channel channel, List<String> commands) {

            try {
                PrintStream out = new PrintStream(channel.getOutputStream());
                android.util.Log.i(TAG, "send command in chanel " + channel.hashCode());
                out.println("#!/bin/bash");
                for (String command : commands) {
                    out.println(command);
                }
                out.println("exit");

                out.flush();
                out.close();


            } catch (Exception e) {
                android.util.Log.e(TAG, "Error while sending commands: " + e);
            }

        }

        private void readChannelOutput(Channel channel) {

            byte[] buffer = new byte[1024];

            String line = "";
            try {
                InputStream in = channel.getInputStream();
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(buffer, 0, 1024);
                        if (i < 0) {
                            break;
                        }
                        line = new String(buffer, 0, i);
                        android.util.Log.w(TAG, " line " + line);
                    }

                    if (line.contains("logout")) {
                        break;
                    }

                    if (channel.isClosed()) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                    }
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error while reading channel output: " + e);
            }
        }

        public void close() {
            channelSell.disconnect();
            // session.disconnect();
            System.out.println("Disconnected channel and session");
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getActivity(), "设置成功，请拨打电话", Toast.LENGTH_LONG).show();

                }

            });
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected ChannelShell doInBackground(Void... params) {
            executeCommands(commands);
            close();
            return null;
        }


    }

    private void displayUploadLogsInProgress() {
        if (uploadInProgress) {
            return;
        }
        uploadInProgress = true;

        progress = ProgressDialog.show(LinphoneActivity.instance(), null, null);
        Drawable d = new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorE));
        d.setAlpha(200);
        progress.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        progress.getWindow().setBackgroundDrawable(d);
        progress.setContentView(R.layout.progress_dialog);
        progress.show();
    }

    private void sendLogs(Context context, String info) {
        final String appName = context.getString(R.string.app_name);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getString(R.string.about_bugreport_email)});
        i.putExtra(Intent.EXTRA_SUBJECT, appName + " Logs");
        i.putExtra(Intent.EXTRA_TEXT, info);
        i.setType("application/zip");

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onPause() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }

        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.PHONE);
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (LinphoneActivity.isInstanciated()) {
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (v == sendLogButton) {
                if (lc != null) {
                    lc.uploadLogCollection();
                }
            } else if (v == resetLogButton) {
                if (lc != null) {
                    lc.resetLogCollection();
                }
            } else if (v == cancel) {
                LinphoneActivity.instance().goToDialerFragment();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
