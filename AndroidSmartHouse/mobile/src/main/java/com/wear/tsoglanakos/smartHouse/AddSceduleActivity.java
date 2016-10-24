package com.wear.tsoglanakos.smartHouse;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;

public class AddSceduleActivity extends AppCompatActivity {
    private Spinner devideIDSpinner, tab_command_text_view;
    private ArrayList<String> ids_list = new ArrayList<String>();
    private ArrayList<String> commands_list = new ArrayList<String>();
    private TextView hourTextView, minutesTextView;
    private TextView sunday_tab, monday_tab, tuesday_tab, wednesday_tab, thursday_tab, friday_tab, saturday_tab;
    private CheckBox weekly, active;
    private Button sendButton;
    public final static String DAYS_STRING = "ActiveDays:", TIME_STRING = "Time:",ACTIVE_TIME_STRING = "ActiveTime:", COMMAND_TEXT_STRING = "CommandText:",TIME_STAMP="TimeStamp:",
            IS_WEEKLY = "IsWeekly:", IS_ACTIVE = "IsActive:", COMMAND_SPLIT_STRING = "##", SHEDULE_SPLIT_STRING = "@@!@@", COMMAND_ID = "CommandID:", DEVICE_ID = "DeviceID:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scedule);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        devideIDSpinner = (Spinner) findViewById(R.id.deviceID);
        tab_command_text_view = (Spinner) findViewById(R.id.tab_command_text_view);
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
        devideIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                commands_list.removeAll(commands_list);
                sendDataToAll("getCommandID" + textView.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("position", "nothing selected");

            }
        });

    }

    public void goBack(View v) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isReceiving = true;
        receiver();
        sendDataToAll("getIDS");

    }

    @Override
    protected void onStop() {
        super.onStop();
        isReceiving = false;
        if (clientSocket != null ) {
            clientSocket.disconnect();
            clientSocket.close();
            clientSocket=null;
        }

    }

    private void sendDataToAll(String text) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData(text, inetAddress, AutoConnection.port);
        }
    }

    @Override
    public void onBackPressed() {
        isReceiving=false;
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

    private boolean isReceiving = true;
    private DatagramSocket clientSocket;
    private Thread thread;

    private void receiver() {
        thread = new Thread() {

            @Override
            public void run() {
                ids_list.removeAll(ids_list);
//                ids_list.add("select Device ID");

                while (isReceiving) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null)
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        Log.e("sentence", sentence);

                        if (sentence.startsWith("getIDS")) {
                            sentence = sentence.substring("getIDS".length(), sentence.length());
                            ids_list.add(sentence);
                            setSpinnerItem(devideIDSpinner, ids_list);

                        } else if (sentence.startsWith("getComandID")) {
                            sentence = sentence.substring("getComandID".length(), sentence.length());
                            proccessCommandsAndAddToList(sentence);
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    sendButton.setEnabled(true);
                                }
                            });
                        } else if (sentence.equals("addedOk")) {
                            isReceiving=false;
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddSceduleActivity.this, "New shedule saved.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(AddSceduleActivity.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (sentence.equals("addedNotOk")) {
                            isReceiving=false;
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddSceduleActivity.this, "This shedule already exists.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(AddSceduleActivity.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }


                    } catch (SocketException e) {
//                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                thread=null;

            }
        };
        thread.start();
    }

    private void proccessCommandsAndAddToList(String commands) {
        commands_list.removeAll(commands_list);
        String[] list = commands.split("@@@");
        for (String str : list) {
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
        }
        setSpinnerItem(tab_command_text_view, commands_list);

    }

    private void sendData(final String output, final InetAddress IPAddress, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                      String    sendData= output;
                    try {
                        sendData=  StringUtils.stripAccents(output);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
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

        if (thread == null || !thread.isAlive()) {
            receiver();
        }

        ids_list.removeAll(ids_list);
        sendDataToAll("getIDS");
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
            String extgraString=(isDayOn(saturday_tab))?" on":" off";
            activeDays += Calendar.SUNDAY+extgraString;
        }
        if (isDaySelected(monday_tab)) {
            String extgraString=(isDayOn(monday_tab))?" on":" off";
            activeDays += Calendar.MONDAY+extgraString;
        }
        if (isDaySelected(tuesday_tab)) {
            String extgraString=(isDayOn(tuesday_tab))?" on":" off";
            activeDays += Calendar.TUESDAY+extgraString;
        }
        if (isDaySelected(wednesday_tab)) {
            String extgraString=(isDayOn(wednesday_tab))?" on":" off";
            activeDays += Calendar.WEDNESDAY+extgraString;
        }
        if (isDaySelected(thursday_tab)) {
            String extgraString=(isDayOn(thursday_tab))?" on":" off";
            activeDays += Calendar.THURSDAY+extgraString;
        }
        if (isDaySelected(friday_tab)) {
            String extgraString=(isDayOn(friday_tab))?" on":" off";
            activeDays += Calendar.FRIDAY+extgraString;
        }
        if (isDaySelected(saturday_tab)) {
            String extgraString=(isDayOn(saturday_tab))?" on":" off";
            activeDays += Calendar.SATURDAY+extgraString;
        }

        try {

            sendDataToAll("saveShedule:DeviceID:" + devideIDSpinner.getSelectedItem().toString() + COMMAND_SPLIT_STRING + COMMAND_TEXT_STRING + tab_command_text_view.getSelectedItem().toString() +
                    COMMAND_SPLIT_STRING + DAYS_STRING + activeDays + COMMAND_SPLIT_STRING + ACTIVE_TIME_STRING + hourTextView.getText().toString() + ":" + minutesTextView.getText().toString()
                    + COMMAND_SPLIT_STRING + IS_WEEKLY + weekly.isChecked() + COMMAND_SPLIT_STRING + IS_ACTIVE + active.isChecked());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isDaySelected(View v) {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            if (color == getResources().getColor(R.color.green)||color == getResources().getColor(R.color.red)) {
                return true;
            }

        }

        return false;
    }

    private boolean isDayOn(View v) {
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
    private boolean isDayOff(View v) {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            if (color == getResources().getColor(R.color.red)) {
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
                v.setBackgroundColor(getResources().getColor(R.color.gray));

            } else if (color == getResources().getColor(R.color.gray)) {
                v.setBackgroundColor(getResources().getColor(R.color.green));

            }


        }
    }

}