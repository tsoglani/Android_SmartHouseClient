package com.smart.tsoglani.smart_house;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;

public class UpdateSceduleActivity extends AppCompatActivity {
    private Spinner devideIDSpinner, tab_command_text_view, command_mode;
    private ArrayList<String> ids_list = new ArrayList<String>();
    private ArrayList<String> commands_list = new ArrayList<String>();
    private TextView hourTextView, minutesTextView;
    private TextView sunday_tab, monday_tab, tuesday_tab, wednesday_tab, thursday_tab, friday_tab, saturday_tab;
    private CheckBox weekly, active;
    private RelativeLayout relative_update;
    Button sendButton;
    String defauldCommandID, defauldDeviceID, defaulActiveDays, defaultIsWeekly, defaultIsActive, defaultTime, defaultCommandText; // use default commandText
    private final String EXTRA = "extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scedule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        ids_list.removeAll(ids_list);
        relative_update = (RelativeLayout) findViewById(R.id.relative_update);
        devideIDSpinner = (Spinner) findViewById(R.id.deviceID);
        tab_command_text_view = (Spinner) findViewById(R.id.tab_command_text_view);
        command_mode = (Spinner) findViewById(R.id.command_mode);
        hourTextView = (TextView) findViewById(R.id.hour_text);
        minutesTextView = (TextView) findViewById(R.id.minutes_text);
        sunday_tab = (TextView) findViewById(R.id.sunday_tab);
        monday_tab = (TextView) findViewById(R.id.monday_tab);
        tuesday_tab = (TextView) findViewById(R.id.tuesday_tab);
        wednesday_tab = (TextView) findViewById(R.id.wednesday_tab);
        thursday_tab = (TextView) findViewById(R.id.thursday_tab);
        friday_tab = (TextView) findViewById(R.id.friday_tab);
        saturday_tab = (TextView) findViewById(R.id.saturday_tab);
        active = (CheckBox) findViewById(R.id.active);
        sendButton = (Button) findViewById(R.id.save);
        weekly = (CheckBox) findViewById(R.id.weekly);
        sendButton.setEnabled(false);
        devideIDSpinner.setEnabled(false);

//        tab_command_text_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                defaultCommandText=tab_command_text_view.getSelectedItem().toString();
//            }
//        });
        String extra = getIntent().getStringExtra(EXTRA);

        String[] extraList = extra.split(AddSceduleActivity.COMMAND_SPLIT_STRING);
        Log.e("extraList lingth ", Integer.toString(extraList.length));
        defauldDeviceID = extraList[0];
        defauldCommandID = extraList[1];
        defaulActiveDays = extraList[2];
        defaultTime = extraList[3];
        defaultIsWeekly = extraList[4];
        defaultIsActive = extraList[5];
        defaultCommandText = extraList[6];

        if (defaultCommandText.endsWith(" on")) {
            defaultCommandText = defaultCommandText.substring(0, defaultCommandText.length() - " on".length());
            command_mode.setSelection(0);
        }
        if (defaultCommandText.endsWith(" off")) {
            defaultCommandText = defaultCommandText.substring(0, defaultCommandText.length() - " off".length());
            command_mode.setSelection(1);
        }

        Log.e("defaultIsWeekly", defaultIsWeekly);
        Log.e("defaultIsActive", defaultIsActive);
        hourTextView.setText(defaultTime.split(":")[0]);
        minutesTextView.setText(defaultTime.split(":")[1]);
        relative_update.setTag(defauldCommandID);
        weekly.setChecked(Boolean.parseBoolean(defaultIsWeekly));
        active.setChecked(Boolean.parseBoolean(defaultIsActive));
        changeDayColor(sunday_tab, defaulActiveDays, Calendar.SUNDAY);
        changeDayColor(monday_tab, defaulActiveDays, Calendar.MONDAY);
        changeDayColor(tuesday_tab, defaulActiveDays, Calendar.TUESDAY);
        changeDayColor(wednesday_tab, defaulActiveDays, Calendar.WEDNESDAY);
        changeDayColor(thursday_tab, defaulActiveDays, Calendar.THURSDAY);
        changeDayColor(friday_tab, defaulActiveDays, Calendar.FRIDAY);
        changeDayColor(saturday_tab, defaulActiveDays, Calendar.SATURDAY);


        ids_list.add(defauldDeviceID);
        setSpinnerItem(devideIDSpinner, ids_list);

        devideIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                TextView textView = (TextView) view;
                sendDataToAll("getCommandID" + defauldDeviceID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("position", "nothing selected");

            }
        });

    }

    private void changeDayColor(View v, String daysString, int day) {
        if (daysString.contains(Integer.toString(day))) {
            v.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            v.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }

    public void goBack(View v) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isReceiving = true;
        receiver();
//        sendDataToAll("getIDS");

    }

    @Override
    protected void onStop() {
        super.onStop();
        isReceiving = false;
        if (clientSocket != null ) {
            clientSocket.disconnect();
            clientSocket.close();
        }

    }

    private void sendDataToAll(String text) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData(text, inetAddress, AutoConnection.port);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SheduleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setSpinnerItem(final Spinner spinner, final ArrayList<String> strings) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.my_spinner_text, strings);
//                adapter.setDropDownViewResource(R.layout.my_spinner_text);
                spinner.setAdapter(adapter);
            }
        });

    }

    private void setSpinnerItem(final Spinner spinner, final ArrayList<String> strings,final int selectedID) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.my_spinner_text, strings);
//                adapter.setDropDownViewResource(R.layout.my_spinner_text);
                spinner.setAdapter(adapter);
                if (selectedID != -1) {
                    spinner.setSelection(selectedID);
                }
            }
        });

    }

    private boolean isReceiving = true;
    private DatagramSocket clientSocket;
    private Thread thread;

    private void receiver() {
        thread = new Thread() {

            @Override
            public void run() {

//                ids_list.add("select Device ID");

                while (isReceiving) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null || clientSocket.isClosed())
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        Log.e("sentence", sentence);

//                        if (sentence.startsWith("getIDS")) {
//                            sentence = sentence.substring("getIDS".length(), sentence.length());
//                            ids_list.add(sentence);
//                            setSpinnerItem(devideIDSpinner, ids_list);
//
//                        } else
                        if (sentence.startsWith("getComandID")) {
                            sentence = sentence.substring("getComandID".length(), sentence.length());
                            proccessCommandsAndAddToList(sentence);
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    sendButton.setEnabled(true);

                                }
                            });
                        } else if (sentence.startsWith("updatedOk")) {
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(UpdateSceduleActivity.this, "Shedule updated.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(UpdateSceduleActivity.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (sentence.equals("addedNotOk")) {
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(UpdateSceduleActivity.this, "This shedule already exists.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(UpdateSceduleActivity.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }


                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                thread = null;

            }
        };
        thread.start();
    }

    private void proccessCommandsAndAddToList(String commands) {
        commands_list.removeAll(commands_list);
        String[] list = commands.split("@@@");
        int selectedID = -1;
        for (int i = 0; i < list.length; i++) {
            String str = list[i];
            if (str.endsWith(" on")) {
                str = str.substring(0, str.length() - " on".length());
            } else if (str.endsWith(" off")) {
                str = str.substring(0, str.length() - " off".length());
            } else if (str.endsWith("on")) {
                str = str.substring(0, str.length() - "on".length());
            } else if (str.endsWith("off")) {
                str = str.substring(0, str.length() - "off".length());
            }
            commands_list.add(str);
            if (str.equals(defaultCommandText)) {
                selectedID = i;
            }
        }


        setSpinnerItem(tab_command_text_view, commands_list,selectedID);

    }

    private void sendData(final String sendData, final InetAddress IPAddress, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID+sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID+sendData).length(), IPAddress, port);
                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void refreshFunction(View v) {
//        ids_list.removeAll(ids_list);
//        sendDataToAll("getIDS");
        if (thread == null || !thread.isAlive()) {
            receiver();
        }
        sendDataToAll("getCommandID" + defauldDeviceID);

    }

    public void addMinutesFunction(View v) {
        String minString = minutesTextView.getText().toString();
        int minutes = Integer.parseInt(minString);
        if (minutes < 59) {
            minutes++;
        } else {
            minutes = 0;
        }
        String minuterString;
        if (minutes < 10) {
            minuterString = "0" + Integer.toString(minutes);
        } else {
            minuterString = Integer.toString(minutes);
        }
        minutesTextView.setText(minuterString);
    }

    public void minusMinutesFunction(View v) {
        String minString = minutesTextView.getText().toString();
        int minutes = Integer.parseInt(minString);
        if (minutes > 0) {
            minutes--;
        } else {
            minutes = 59;
        }
        String minuterString;
        if (minutes < 10) {
            minuterString = "0" + Integer.toString(minutes);
        } else {
            minuterString = Integer.toString(minutes);
        }
        minutesTextView.setText(minuterString);

    }

    public void minusHourFunction(View v) {
        String hourString = hourTextView.getText().toString();
        int hour = Integer.parseInt(hourString);
        if (hour > 0) {
            hour--;
        } else {
            hour = 23;
        }

        String outHourString = null;
        if (hour < 10) {
            outHourString = "0" + Integer.toString(hour);
        } else {
            outHourString = Integer.toString(hour);
        }
        hourTextView.setText(outHourString);

    }

    public void addHourFunction(View v) {
        String hourString = hourTextView.getText().toString();
        int hour = Integer.parseInt(hourString);
        if (hour < 23) {
            hour++;
        } else {
            hour = 0;
        }
        String outHourString = null;
        if (hour < 10) {
            outHourString = "0" + Integer.toString(hour);
        } else {
            outHourString = Integer.toString(hour);
        }
        hourTextView.setText(outHourString);
    }


    public void saveFunction(final View v) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        v.setEnabled(false);
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if (v != null) {
                            v.setEnabled(true);
                        }
                    }
                }.execute();
            }
        });

        String activeDays = "";
        if (isDaySelected(sunday_tab)) {
            activeDays += Calendar.SUNDAY;
        }
        if (isDaySelected(monday_tab)) {
            activeDays += Calendar.MONDAY;
        }
        if (isDaySelected(tuesday_tab)) {
            activeDays += Calendar.TUESDAY;
        }
        if (isDaySelected(wednesday_tab)) {
            activeDays += Calendar.WEDNESDAY;
        }
        if (isDaySelected(thursday_tab)) {
            activeDays += Calendar.THURSDAY;
        }
        if (isDaySelected(friday_tab)) {
            activeDays += Calendar.FRIDAY;
        }
        if (isDaySelected(saturday_tab)) {
            activeDays += Calendar.SATURDAY;
        }

        try {

            sendDataToAll("updateShedule:DeviceID:" + devideIDSpinner.getSelectedItem().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.COMMAND_ID + ((RelativeLayout) v.getParent()).getTag().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING +
                    AddSceduleActivity.COMMAND_TEXT_STRING + tab_command_text_view.getSelectedItem().toString() + " " + command_mode.getSelectedItem().toString() +
                    AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.DAYS_STRING + activeDays + AddSceduleActivity.COMMAND_SPLIT_STRING +
                    AddSceduleActivity.ACTIVE_TIME_STRING + hourTextView.getText().toString() + ":" + minutesTextView.getText().toString()
                    + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.IS_WEEKLY + weekly.isChecked() + AddSceduleActivity.COMMAND_SPLIT_STRING +
                    AddSceduleActivity.IS_ACTIVE + active.isChecked());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isDaySelected(View v) {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            if (color == getResources().getColor(R.color.green)) {
                return true;
            }

        }

        return false;
    }

    public void chooseDayFunction(View v) {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            if (color == getResources().getColor(R.color.green)) {
                v.setBackgroundColor(getResources().getColor(R.color.red));
            } else if (color == getResources().getColor(R.color.red)) {
                v.setBackgroundColor(getResources().getColor(R.color.green));

            }


        }
    }

}
