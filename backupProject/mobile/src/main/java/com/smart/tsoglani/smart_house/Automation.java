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

public class Automation extends AppCompatActivity {
    private DatagramSocket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver();
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
                        if (sentence.equalsIgnoreCase("chooseSheduleFunction")) {
                            Intent intent = new Intent(Automation.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else  if (sentence.equalsIgnoreCase("chooseTimerFunction")) {
                            Intent intent = new Intent(Automation.this, TimerActivity.class);
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


    public void sheduleFunction(View v){


        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseSheduleFunction", inetAddress, AutoConnection.port);
        }

    }

    public void timerFunction(View v){

        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseTimerFunction", inetAddress, AutoConnection.port);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (clientSocket != null ) {
            clientSocket.disconnect();
            clientSocket.close();
        }
        isReceiving = false;
    }

}
