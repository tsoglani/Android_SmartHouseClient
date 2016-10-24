package com.smart.tsoglani.smart_house;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Locale;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SpeechActivity extends Activity {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public static final String LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);



        // hide the action bar
//        getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isOnline())
                    promptSpeechInput();
                else
                    toast("No internet connection.");
            }
        });

       String language=getValue(this,LANGUAGE,"Eng");



        RadioGroup rg= (RadioGroup) findViewById(R.id.radioSex);
        RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
        RadioButton radioEng = (RadioButton) findViewById(R.id.radioEng);

        if(language.equals("Auto")){
            radioAuto.setChecked(true);
            radioEng.setChecked(false);
        }else{
            radioAuto.setChecked(false);
            radioEng.setChecked(true);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
                if(radioAuto.getId()==checkedId){
                    save(SpeechActivity.this,LANGUAGE,"Auto");
                }else{
                    save(SpeechActivity.this,LANGUAGE,"Eng");

                }
            }
        });
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
        if (radioAuto.isChecked()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        } else {
            intent.putExtra("en-US",
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        }
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            toast(getString(R.string.speech_not_supported));
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    final ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                for (InetAddress inetAddress : AutoConnection.usingInetAddress)
                                    sendData(greekToGreeklish(result.get(0)), inetAddress, AutoConnection.port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                }
                break;
            }

        }
    }

    static String greekToGreeklish(String input) {
        String output = new String();
        input = input.toLowerCase();
        for (int i = 0; i < input.length(); i++) {
            output += getGreeklishChar(input.charAt(i));
        }
        return output;
    }

    private static String getGreeklishChar(char greekChar) {
        String greeklishString = null;
        switch (greekChar) {
            case 'α':
            case 'ά':
                greeklishString = "a";
                break;
            case 'β':
                greeklishString = "v";
                break;
            case 'γ':
                greeklishString = "g";
                break;
            case 'δ':
                greeklishString = "d";
                break;
            case 'ε':
            case 'έ':
                greeklishString = "e";
                break;
            case 'ζ':
                greeklishString = "z";
                break;
            case 'η':
            case 'ή':
            case 'ι':
            case 'ί':
                greeklishString = "i";
                break;
            case 'θ':
                greeklishString = "th";
                break;
            case 'κ':
                greeklishString = "k";
                break;
            case 'λ':
                greeklishString = "l";
                break;
            case 'μ':
                greeklishString = "m";
                break;
            case 'ν':
                greeklishString = "n";
                break;
            case 'ξ':
                greeklishString = "ks";
                break;
            case 'ό':
            case 'ο':
            case 'ω':
            case 'ώ':
                greeklishString = "o";
                break;
            case 'π':
                greeklishString = "p";
                break;
            case 'ρ':
                greeklishString = "r";
                break;
            case 'σ':
            case 'ς':
                greeklishString = "s";
                break;
            case 'τ':
                greeklishString = "t";
                break;
            case 'υ':
            case 'ύ':
                greeklishString = "y";
                break;
            case 'φ':
                greeklishString = "f";
                break;
            case 'χ':
                greeklishString = "x";
                break;
            case 'ψ':
                greeklishString = "ps";
                break;
            default:
                greeklishString = Character.toString(greekChar);
        }

        return greeklishString;
    }

    DatagramSocket clientSocket;

    private void sendData(String sendData, InetAddress IPAddress, int port) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID+sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID+sendData).length(), IPAddress, port);
//        toast(sendData);

        clientSocket = new DatagramSocket();

        clientSocket.send(sendPacket);
    }

    private void toast(final String msg) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(SpeechActivity.this, msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void goBack(View v) {
        onBackPressed();
    }




    public static void save(Activity act, String valueCodeName, String value) {
        SharedPreferences.Editor editor = act.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(valueCodeName, value);
        editor.commit();
    }


    public static String getValue(Activity act, String valueCodeName,String defaultValue) {
        SharedPreferences prefs = act.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(valueCodeName, defaultValue);

        return restoredText;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (clientSocket != null ) {
            clientSocket.disconnect();
            clientSocket.close();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

}
