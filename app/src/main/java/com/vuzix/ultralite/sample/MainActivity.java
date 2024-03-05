package com.vuzix.ultralite.sample;

import android.Manifest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.common.api.Scope;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.TextAlignment;
import com.vuzix.ultralite.TextWrapMode;
import com.vuzix.ultralite.UltraliteColor;
import com.vuzix.ultralite.UltraliteSDK;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * A Google Calendar API service object used to access the API.
     * Note: Do not confuse this class with API library's model classes, which
     * represent specific data structures.
     */
    com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential credential;

    final HttpTransport transport = new NetHttpTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR};

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSION_REQUEST_INTERNET = 1;
    private EditText speechText;
    private EditText notificationEditText;
    private SpeechRecognizer speechRecognizer;
    private static final String OpenAiToken = "" ;

    private static final int RC_SIGN_IN = 123;
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


//        Working Auth Sys
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CalendarScopes.CALENDAR_READONLY))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        Log.d("GoogleCloud", "Credential Generated.");
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("JarvisAI")
                .build();
        Log.d("GoogleCloud", "Service Generated.");


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }


        ImageView linkedImageView = findViewById(R.id.linked);
        Button notificationButton = findViewById(R.id.send_notification);
        notificationEditText = findViewById(R.id.notification_text);
        speechText = findViewById(R.id.speech_text);

        UltraliteSDK ultralite = UltraliteSDK.get(this);

        ultralite.getLinked().observe(this, linked -> {
            linkedImageView.setImageResource(linked ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
//            nameTextView.setText(ultralite.getName());
        });






        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Not used
            }

            @Override
            public void onBeginningOfSpeech() {
                // Not used
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Not used
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Not used
            }

            @Override
            public void onEndOfSpeech() {
                // Not used
            }

            @Override
            public void onError(int error) {
                // Handle speech recognition error
                Toast.makeText(MainActivity.this, "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
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
            public void onPartialResults(Bundle partialResults) {
                // Not used
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Not used
            }
        });

        Button speechInputButton = findViewById(R.id.speech_input_button);
        speechInputButton.setOnClickListener(v -> {
            // Start speech recognition
            startSpeechRecognition();
        });


        notificationButton.setOnClickListener(v -> {
            String notificationText = notificationEditText.getText().toString();
            ultralite.sendNotification("Jarvis", notificationText,
                    loadLVGLImage(this, R.drawable.rocket));
        });
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

    private void google_cal(){
        DateTime now = new DateTime(System.currentTimeMillis());
        Log.d("GoogleCloud", "Date Generated.");
        mService.events();
        Log.d("GoogleCloud", "Events Generated." + mService.events());
        Events events = null;
        try {
            Calendar.Events.List cal_events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true);
            Log.d("GoogleCloud", "cal events generated"+ cal_events);

            cal_events.execute();

            Log.d("GoogleCloud", "No error.");
        } catch (IOException e) {
            Log.d("GoogleCloud", "Events Error.");
        }
        assert events != null;
        List<Event> items = events.getItems();
        Log.d("GoogleCloud", "Items Generated.");
    }


    private void startSpeechRecognition(){
        // Create speech recognition intent
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        // Start speech recognition
        speechRecognizer.startListening(speechIntent);
    }


    private void analyzeSpeech(String voice_input) {
        speechText.setText(voice_input);
        if (voice_input.toLowerCase().contains("schedule")){
            pull_schedule(voice_input);
        }
        if (voice_input.toLowerCase().contains("jarvis")){
            askQuestion(voice_input);
        }
    }
    private void askQuestion(String voice_input) {
        String query = voice_input + "(Answer in less than 20 words)";
        String output = queryGpt(query);
        sendToGlasses(output);
    }
    private void pull_schedule(String voice_input) {
        String query =  "The current day, date & time is: " + getCurrentDateTimeFormatted() +
                "return date in only YYYY-MM-DD format (don't return anything" +
                "else) : " + voice_input;
        String date= queryGpt(query);
        String availability = getAvailability(date);
        sendToGlasses(availability);
    }

    public static String getCurrentDateTimeFormatted() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Define the format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm:ss");

        // Format and return the current date and time
        return now.format(formatter);
    }

    private String queryGpt(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new PostTask(query));
        try {
            String result = future.get(); // This will block until the task is completed
            JSONObject responseJson = new JSONObject(result);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");

            } else {
                return "NA";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        return "NA";
    }

    private String getAvailability(String date) {
        return date + "\n2pm-3pm \n5pm-6pm";
    }

    private void sendToGlasses(String content) {
        UltraliteSDK ultralite = UltraliteSDK.get(this);
        notificationEditText.setText(content);
        String notificationText = notificationEditText.getText().toString();
        ultralite.sendNotification("Jarvis",  notificationText,
                loadLVGLImage(this, R.drawable.rocket));
    }

    private static class PostTask implements Callable<String> {
        private final String query;
        public PostTask(String query) {
            this.query = query;
        }

        @Override
        public String call() throws Exception {
            String response = "";
            try {
                // URL and parameters
                String url = "https://api.openai.com/v1/chat/completions";
                String bearerToken = OpenAiToken;

                // Create JSON payload
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "gpt-3.5-turbo-1106");

                JSONArray messagesArray = new JSONArray();
                JSONObject messageObject = new JSONObject();
                messageObject.put("role", "user");
                messageObject.put("content", query);
                messagesArray.put(messageObject);

                requestBody.put("messages", messagesArray);

                // Create connection
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // Set request method
                con.setRequestMethod("POST");

                // Set request headers
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + bearerToken);

                // Enable input and output streams
                con.setDoOutput(true);
                con.setDoInput(true);

                // Send POST request
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    byte[] postData = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    wr.write(postData);
                }

                // Get response
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder responseBuffer = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        responseBuffer.append(inputLine);
                    }
                    response = responseBuffer.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }
    }
}



