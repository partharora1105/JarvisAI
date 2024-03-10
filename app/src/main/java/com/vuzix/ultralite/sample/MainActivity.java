package com.vuzix.ultralite.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.UltraliteSDK;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSION_REQUEST_INTERNET = 1;
    private EditText speechText;
    private EditText notificationEditText;
    private SpeechRecognizer speechRecognizer;

    int RC_SIGN_IN = 20;

    Boolean isListening = Boolean.FALSE;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;

    String authCode = "default_code";
    String host_url = "https://ccghwd.pythonanywhere.com";
    String trigger = "Jarvis";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR_EVENTS))
                .requestServerAuthCode("137591440076-a250qqhgi5t2ggtme5mvi2ir7cs3e2ct.apps.googleusercontent.com", false) // Replace YOUR_SERVER_CLIENT_ID
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignIn();
//        gAuth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                googleSignIn();
//            }
//        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }


        ImageView linkedImageView = findViewById(R.id.linked);

        notificationEditText = findViewById(R.id.notification_text_2);
        speechText = findViewById(R.id.speech_text_2);

        UltraliteSDK ultralite = UltraliteSDK.get(this);

        ultralite.getLinked().observe(this, linked -> {
            linkedImageView.setImageResource(linked ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
//            nameTextView.setText(ultralite.getName());
        });

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onError(int error) {
                if (isListening){
                    startSpeechRecognition();
                    //Toast.makeText(MainActivity.this, "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Stopping Speech Recognition", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spech_text = matches.get(0); // Take the first result
                    analyzeSpeech(spech_text);
                    startSpeechRecognition();
                } else {
                    Toast.makeText(MainActivity.this, "No speech input recognized", Toast.LENGTH_SHORT).show();
                    startSpeechRecognition();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Button speechInputButton = findViewById(R.id.speech_input_button_start);
        speechInputButton.setOnClickListener(v -> {
            startSpeechRecognition();
            isListening = true;
        });


        Button stopSpeechInputButton = findViewById(R.id.speech_input_button_end);
        stopSpeechInputButton.setOnClickListener(v -> {
            isListening = false;
            stopSpeechRecognition();
        });

    }

    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            authCode = account.getServerAuthCode();
            Toast.makeText(MainActivity.this, "Signed into Google", Toast.LENGTH_SHORT).show();
            Log.d("AUTH_CODE", "Auth code is: " + authCode);
        } catch (ApiException e) {
            Log.e("SIGN_IN_ERROR", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this, "Sign in failed. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void startSpeechRecognition(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        }
        // Create speech recognition intent
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        // Start speech recognition
        speechRecognizer.startListening(speechIntent);
    }

    private void stopSpeechRecognition(){
        speechRecognizer.stopListening();
    }

    private void analyzeSpeech(String voice_input) {
        speechText.setText(voice_input);

        if (voice_input.toLowerCase().contains(trigger.toLowerCase())){
            googleSignIn();
            String availability = getResponse(authCode, voice_input);
            sendToGlasses(availability);
        }
    }

    private String getResponse(String authCode, String voiceInput) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try {
                String encodedVoiceInput = URLEncoder.encode(voiceInput, StandardCharsets.UTF_8.toString());
                Log.d("AUTH_CODE", "Auth code before endpoint is: " + authCode);
                String urlString = String.format("%s/everyday/wear/rest/api/speech/output/%s/%s", host_url, authCode, encodedVoiceInput);
                Log.d("URL", "URL is: " + urlString);
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    System.out.println(response.toString());
                    return response.toString();
                } else {
                    System.out.println("GET request not worked");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        try {
            String result = future.get();
            Log.d("HTTP_GET_RESULT", "Result from server: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong";
        } finally {
            executor.shutdown();
        }
    }


    private void sendToGlasses(String content) {
        UltraliteSDK ultralite = UltraliteSDK.get(this);
        notificationEditText.setText(content);
        String notificationText = notificationEditText.getText().toString();
        ultralite.sendNotification("Jarvis", notificationText,
                loadLVGLImage(this, R.drawable.rocket));
    }

    private static LVGLImage loadLVGLImage(Context context, int resource) {
        return LVGLImage.fromBitmap(loadBitmap(context, resource), LVGLImage.CF_INDEXED_1_BIT);
    }

    @SuppressWarnings("ConstantConditions")
    private static Bitmap loadBitmap(Context context, int resource) {
        BitmapDrawable drawable = (BitmapDrawable) ResourcesCompat.getDrawable(
                context.getResources(), resource, context.getTheme());
        return drawable.getBitmap();
    }

}



