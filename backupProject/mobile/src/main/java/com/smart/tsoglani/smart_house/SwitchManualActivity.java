package com.smart.tsoglani.smart_house;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class SwitchManualActivity extends AppCompatActivity {
    DB_SwitchCommand db;
    boolean isCommandMode = false;
    DatagramSocket clientSocket;
    static SwitchManualActivity switchActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DB_SwitchCommand(this);
        Button refresh_button = (Button) findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        Toast.makeText(SwitchManualActivity.this, "Wait 3 seconds to receive the data from device.", Toast.LENGTH_SHORT).show();

                                LinearLayout connection_history_linear = (LinearLayout) findViewById(R.id.switch_linear);

                                connection_history_linear.removeAllViews();
                                if (clientSocket != null) {
                                    clientSocket.disconnect();
                                    clientSocket.close();
                                    clientSocket = null;
                                }


                        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {

                            if (!isCommandMode) {
                                        sendData("getAllOutput", inetAddress, AutoConnection.port);
                                    } else {
                                        sendData("getAllCommandsOutput", inetAddress, AutoConnection.port);

                                    }}

                    }
                });

            }
        });

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.switch1);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCommandMode = isChecked;
                if (clientSocket != null) {
                    clientSocket.disconnect();
                    clientSocket.close();
                    clientSocket = null;
                }

                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        Toast.makeText(SwitchManualActivity.this, "Wait 3 seconds to receive the data from device.", Toast.LENGTH_SHORT).show();
                        Toast.makeText(SwitchManualActivity.this, "Wait 3 seconds to receive the data from device.", Toast.LENGTH_SHORT).show();

                                storeBoolean("isCommandMode", isCommandMode);
                                LinearLayout connection_history_linear = (LinearLayout) findViewById(R.id.switch_linear);
                                connection_history_linear.removeAllViews();


                                for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
                                    if (!isCommandMode) {
                                        sendData("getAllOutput", inetAddress, AutoConnection.port);
                                    } else {
                                        sendData("getAllCommandsOutput", inetAddress, AutoConnection.port);

                                    }

                            }

                    }
                });

            }
        });
        isCommandMode = receiveBoolean("isCommandMode", true);
        toggleButton.setChecked(isCommandMode);
//        isAutoMode = receiveBoolean("isAutoMode", false);
//        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
//        toggleButton.setChecked(isAutoMode);
//        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                LinearLayout connection_history_linear = (LinearLayout) findViewById(R.id.switch_linear);
//
//                connection_history_linear.removeAllViews();
//                isAutoMode = isChecked;
//                storeBoolean("isAutoMode", isChecked);
//
//                if (isAutoMode) {
//                    for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
//                        sendData("getAllOutput", inetAddress, AutoConnection.port);
//                    }
//                }
//
//                else {
//                    runOnUiThread(new Thread() {
//                        @Override
//                        public void run() {
//                            loadManualMode();
//
//                        }
//                    });
//                }
//            }
//        });

//        if(!isAutoMode) {
//            loadManualMode();

//        }else {

        if (isCommandMode)
            for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
                if (!isCommandMode) {
                    sendData("getAllOutput", inetAddress, AutoConnection.port);
                } else {
                    sendData("getAllCommandsOutput", inetAddress, AutoConnection.port);

                }
            }
//        }


    }

    protected void removeAll() {

        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(SwitchManualActivity.this, "Wait 3 seconds to receive the data from device.", Toast.LENGTH_SHORT).show();
                LinearLayout connection_history_linear = (LinearLayout) findViewById(R.id.switch_linear);

                connection_history_linear.removeAllViews();

            }
        });
    }

    public static final String MY_PREFS_NAME = "SmartHouseTsoglani";


    private void storeBoolean(String dataID, boolean data) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(dataID, data);
        editor.commit();
    }

    private boolean receiveBoolean(String dataID, boolean defaultReturningBoolean) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean restoredText = prefs.getBoolean(dataID, defaultReturningBoolean);
        return restoredText;

    }

    private void storeString(String dataID, String data) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(dataID, data);
        editor.commit();
    }

    private String receiveData(String dataID, String defaultReturningString) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(dataID, defaultReturningString);
        return restoredText;

    }

//    private void loadManualMode() {
//        LinearLayout connection_history_linear = (LinearLayout) findViewById(R.id.switch_linear);
////        connection_history_linear.removeAllViews();
//        connection_history_linear.setGravity(Gravity.CENTER);
//        ArrayList<String> list = db.getAllCotacts();
//
//        for (int i = 0; i < list.size(); i++) {
//            RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.switch_history_tab, null);
//            final String value = list.get(i);
//            Button delete = (Button) rl.findViewById(R.id.delete);
//            delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    db.deleteContact(value);
//                }
//            });
//            final Switch switchButton = (Switch) rl.findViewById(R.id.switch1);
//            switchButton.setText(value);
//
//            switchButton.setOnClickListener(null);
//            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
//                        String state;
//
//                        if (isChecked) {
//                            state = "on";
//                        } else {
//                            state = "off";
//                        }
//                        String output = buttonView.getText().toString();
//                        if (output.charAt(output.length() - 1) == ' ') {
//                            output = output.substring(0, output.length() - 1);
//                        }
//
//                        sendData(SpeechActivity.greekToGreeklish(output + " " + state), inetAddress, AutoConnection.port);
//                    }
//                }
//            });
//
//            connection_history_linear.addView(rl);
//            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(5));
//            View v = new View(this);
//            v.setLayoutParams(params);
//            connection_history_linear.addView(v);
//
//        }
//        Button button = new Button(this);
//        ViewGroup.LayoutParams addNewParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        button.setLayoutParams(addNewParams);
//        button.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(50), dpToPx(50)));
//        button.setBackgroundResource(R.drawable.add2);
//        connection_history_linear.addView(button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addNewTab();
//
//            }
//        });
//        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
//            sendData("update_manual_mode", inetAddress, AutoConnection.port);
//        }
//    }

    boolean isReadyToSwitch = false;

    private void loadAutoMode(ArrayList<String> list) {

        LinearLayout connection_history_linear = (LinearLayout) findViewById(R.id.switch_linear);


        for (int i = 0; i < list.size(); i++) {
            RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.switch_history_tab, null);
            final String value = list.get(i);
            Button delete = (Button) rl.findViewById(R.id.delete);
            delete.setVisibility(View.INVISIBLE);
            final CheckBox switchButton = (CheckBox) rl.findViewById(R.id.switch1);

            String textForEachSell = value;

            if (textForEachSell.substring(textForEachSell.length() - 2, textForEachSell.length()).equalsIgnoreCase("on")) {
                textForEachSell = textForEachSell.substring(0, textForEachSell.length() - 3);
            } else if (textForEachSell.substring(textForEachSell.length() - 3, textForEachSell.length()).equalsIgnoreCase("off")) {
                textForEachSell = textForEachSell.substring(0, textForEachSell.length() - 4);
            }
            changeSwitchMode(textForEachSell);
            switchButton.setText(textForEachSell);

//            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (!isReadyToSwitch) {
//                        switchButton.setChecked(!isChecked);
//                    }
//                    isReadyToSwitch = false;
//
//                }
//            });
//            switchButton.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//
//                    if(event.getAction()==MotionEvent.ACTION_UP){
//                        if (!isReadyToSwitch) {
//                            switchButton.setChecked(!switchButton.isChecked());
//                            isReadyToSwitch = false;
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//            });
            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    for (InetAddress inetAddress : AutoConnection.usingInetAddress) {

                        String state;
//                        if (switchButton.isChecked()) {
//                            state = "on";
//                        } else {
//                            state = "off";
//                        }
                        switchButton.setChecked(!switchButton.isChecked());
                        if (!switchButton.isChecked()) {
                            state = "on";
                        } else {
                            state = "off";
                        }
                        String output = switchButton.getText().toString();
                        if (output.charAt(output.length() - 1) == ' ') {
                            output = output.substring(0, output.length() - 1);
                        }
                        sendData("switch " + SpeechActivity.greekToGreeklish(output + " " + state), inetAddress, AutoConnection.port);
                    }
                }
            });
            connection_history_linear.addView(rl);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(5));

            View v = new View(this);
            v.setLayoutParams(params);
            connection_history_linear.addView(v);

        }
    }


    private void sendData(final String sendData, final InetAddress IPAddress, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID + sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID + sendData).length(), IPAddress, port);

                    if (clientSocket == null || clientSocket.isClosed()) {
                        clientSocket = new DatagramSocket();
                    receiver();
                    }
                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void addNewTab() {

        try {


            final EditText editCommandText = new EditText(this);

            LinearLayout ll = new LinearLayout(getApplicationContext());
            final LinearLayout showLayout = new LinearLayout(getApplicationContext());


            showLayout.setOrientation(LinearLayout.VERTICAL);
            TextView txtView1 = new TextView(getApplicationContext());
            txtView1.setText("Command text.");

            ll.addView(editCommandText);
            editCommandText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            editCommandText.setGravity(Gravity.CENTER);
            final LinearLayout ll2 = new LinearLayout(getApplicationContext());
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 50));
            showLayout.addView(ll);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 200));
            showLayout.addView(ll2);
            new AlertDialog.Builder(this)
                    .setTitle("Add new command.")
                    .setMessage("This command will send to Ruspberry side as it is (when press switch button), plus on or off at the end (switch button on or off).")

                    .setView(showLayout)

                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.insertContact(editCommandText.getText().toString());
                            Intent intent = new Intent(SwitchManualActivity.this, SwitchManualActivity.class);
                            startActivity(intent);

                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).show();
        } catch (final Exception e) {
            e.printStackTrace();

        }


    }


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    boolean isReceiving = true;
    private Thread thread;
    boolean isFinishedNormal = true;

    private void receiver() {
        isFinishedNormal = true;
        if ((thread == null) || !thread.isAlive()) {
            thread = new Thread() {
                @Override
                public void run() {
                    isReceiving = true;
                    while (isReceiving) {
                        try {

                            byte[] receiveData = new byte[1024];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                            if (clientSocket == null || clientSocket.isClosed())
                                clientSocket = new DatagramSocket();
                            clientSocket.receive(receivePacket);

                            String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            Log.e("switch receiver", sentence);
                            receiveStringProcess(sentence);

                        } catch (Exception e) {
                            try {
                                thread = null;
                                if (clientSocket != null) {
                                    clientSocket.disconnect();
                                    clientSocket.close();
                                }
                                isReceiving = false;

                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }
                    thread = null;

                }
            };
            thread.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchActivity = this;
    }


    private void receiveStringProcess(String input) {

//    final    String in2=input;
//runOnUiThread(new Thread() {
//    @Override
//    public void run() {
//        Toast.makeText(SwitchManualActivity.this, in2, Toast.LENGTH_SHORT).show();
//    }
//});

        Log.e("receiveStringProcess", input);
        if (input.startsWith("respondGetAllOutput")) {
            input = input.substring("respondGetAllOutput".length(), input.length());
            final String[] pinax = input.split("@@@");

            if (pinax.length > 1) {
                changeSwitchMode(pinax);
            } else {
                changeSwitchMode(input);
            }

        } else if (input.startsWith("respondGetAllCommandsOutput")) {
            input = input.substring("respondGetAllCommandsOutput".length(), input.length());
            final String[] pinax = input.split("@@@");


            if (pinax.length > 1) {
                changeSwitchMode(pinax);
            } else {
                changeSwitchMode(input);
            }
        } else {


            if (input.startsWith("switch ")) {
                input = input.substring("switch ".length(), input.length());

            } else if (input.startsWith("switch")) {
                input = input.substring("switch".length(), input.length());

            }

            isReadyToSwitch = true;
            changeSwitchMode(input);
            if (WearService.service != null) {
                WearService.service.sendMessage("/switch", input.getBytes());
            }
        }

//        else if (input.startsWith("update_manual_mode")) {
//            input = input.substring("update_manual_mode".length(), input.length());
////            changeSwitchMode(input);
//            final String[] pinax = input.split("@@@");
//
//            if (pinax.length > 1) {
//                changeSwitchMode(pinax);
//            } else {
//                changeSwitchMode(input);
//            }
//        } else {
//            changeSwitchMode(input);
//        }

    }


//    private void update_manual_mode(String input){}

    private void changeSwitchMode(final ArrayList<String> list) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {

                loadAutoMode(list);
                for (int i = 0; i < list.size(); i++) {
                    changeSwitchMode(list.get(i));
                }
            }
        });

    }

    private void changeSwitchMode(final String[] list) {
        ArrayList arrayList = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            String textForEachSell = list[i];
            if (textForEachSell.startsWith("unknown")) {
                continue;
            }

            arrayList.add(textForEachSell);
        }
        changeSwitchMode(arrayList);
    }

    void changeSwitchMode(String input) {
        final String finalInpit = input;

        if (input.length() > 2 && input.substring(input.length() - 2, input.length()).equalsIgnoreCase("on") || input.length() > 3 && input.substring(input.length() - 3, input.length()).equalsIgnoreCase("off")) {

            String cutString = null;
            boolean state = false;
            if (input.substring(input.length() - 2, input.length()).equalsIgnoreCase("on")) {
                cutString = input.substring(0, input.length() - 3);
                final String finalcutString = cutString;

                state = true;

            }
            if (input.length() > 4 && input.substring(input.length() - 3, input.length()).equalsIgnoreCase("off")) {
                cutString = input.substring(0, input.length() - 4);
                state = false;
            }
            if (cutString == null) {
                return;
            }
            final boolean finalState = state;
            LinearLayout switch_linear = (LinearLayout) findViewById(R.id.switch_linear);
            for (int i = 0; i < switch_linear.getChildCount(); i++) {
                View v = switch_linear.getChildAt(i);
                if (v instanceof RelativeLayout) {
                    RelativeLayout rl = (RelativeLayout) v;
                    for (int j = 0; j < rl.getChildCount(); j++) {
                        View rlChild = rl.getChildAt(j);

                        if (rlChild instanceof CheckBox) {
                            final CheckBox aSwitch = (CheckBox) rlChild;

                            if (SpeechActivity.greekToGreeklish(aSwitch.getText().toString()).equals(cutString)) {


                                runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        aSwitch.setChecked(finalState);
                                    }
                                });

                            } else if (SpeechActivity.greekToGreeklish(aSwitch.getText().toString()).startsWith(cutString)) {

                                final String parseToBool = SpeechActivity.greekToGreeklish(aSwitch.getText().toString()).substring(cutString.length(), SpeechActivity.greekToGreeklish(aSwitch.getText().toString()).length()).replaceAll(" ", "");
                                runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        aSwitch.setChecked(Boolean.parseBoolean(parseToBool));
                                    }
                                });

                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public void goBack(View v) {
        onBackPressed();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isReceiving = false;
        switchActivity = null;
        if (clientSocket != null) {
            clientSocket.disconnect();
            clientSocket.close();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        isReceiving = true;
        receiver();
    }


}

