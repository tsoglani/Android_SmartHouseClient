package com.smart.tsoglani.smart_house;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MenuActivity extends AppCompatActivity {
    private DatagramSocket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MainActivity.generateUniqueUserID();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isReceiving = true;
        receiver();
    }

    public void chooseSpeechFunction(View v) {

        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseSpeechFunction", inetAddress, AutoConnection.port);
        }

    }

    public void automationFunction(View v) {


        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseAutomationFunction", inetAddress, AutoConnection.port);
        }

    }

    public void chooseSwitchFunction(View v) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseSwitchFunction", inetAddress, AutoConnection.port);
        }


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goBack(View v) {
        onBackPressed();
    }

    private boolean isReceiving = true;

    private void receiver() {
        new Thread() {
            @Override
            public void run() {
                while (isReceiving) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null || clientSocket.isClosed())
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        if (sentence.equalsIgnoreCase("chooseSwitchFunction")) {
                            Intent intent = new Intent(MenuActivity.this, SwitchManualActivity.class);
                            startActivity(intent);

                        } else if (sentence.equalsIgnoreCase("chooseSpeechFunction")) {
                            Intent intent = new Intent(MenuActivity.this, SpeechActivity.class);
                            startActivity(intent);
                        } else if (sentence.equalsIgnoreCase("chooseAutomationFunction")) {
                            Intent intent = new Intent(MenuActivity.this, Automation.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }


                    } catch (SocketException e) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    private void sendData(final String sendData, final InetAddress IPAddress, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID + sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID + sendData).length(), IPAddress, port);
                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (clientSocket != null) {
            clientSocket.disconnect();
            clientSocket.close();
        }
        isReceiving = false;
    }
}
