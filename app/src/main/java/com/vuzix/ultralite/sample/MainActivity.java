package com.vuzix.ultralite.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private static final String OpenAiToken = "" ;

    int RC_SIGN_IN = 20;

    Button gAuth;

    FirebaseAuth auth;

    GoogleSignInClient mGoogleSignInClient;

    String authCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        gAuth = findViewById(R.id.signInButton);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR_EVENTS))
                .requestServerAuthCode("137591440076-a250qqhgi5t2ggtme5mvi2ir7cs3e2ct.apps.googleusercontent.com", false) // Replace YOUR_SERVER_CLIENT_ID
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        gAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }



        ImageView linkedImageView = findViewById(R.id.linked);
        Button notificationButton = findViewById(R.id.send_notification_2);
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

            // Get the authorization code to verify it's not null
            authCode = account.getServerAuthCode();

            // For debugging purposes, log the auth code
            Log.d("AUTH_CODE", "Auth code is: " + authCode);

            // add your code here to call the endpoint

            // Since sign-in was successful, proceed to the next activity
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.e("SIGN_IN_ERROR", "signInResult:failed code=" + e.getStatusCode());

            // Inform the user that sign-in failed
            Toast.makeText(MainActivity.this, "Sign in failed. Please try again.", Toast.LENGTH_LONG).show();

            // Optionally, reset any sign-in UI elements or provide options to retry sign-in
        }
    }

    private String sendToServerWithAuthCodeAndVoiceInput(String authCode, String voiceInput) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try {
                // Encode voice input to ensure it's safe for URL inclusion
                String encodedVoiceInput = URLEncoder.encode(voiceInput, StandardCharsets.UTF_8.toString());

                // Construct the URL
                String urlString = String.format("https://ccghwd.pythonanywhere.com/everyday/wear/rest/api/speech/output/%s/%s", authCode, encodedVoiceInput);
                URL url = new URL(urlString);

                // Open connection
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                // Set request method
                con.setRequestMethod("GET");

                // Set request headers, if needed (for example, if you require a User-Agent)
                // con.setRequestProperty("User-Agent", "Mozilla/5.0");

                // Get the response code to check for successful request
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Print result for debugging
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
            // Get the result of the future here, which will block until the callable has returned.
            String result = future.get();
            // Handle the result as needed
            Log.d("HTTP_GET_RESULT", "Result from server: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong";
        } finally {
            executor.shutdown();
        }
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
        String availability = sendToServerWithAuthCodeAndVoiceInput(authCode, voice_input);
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

    private String getAvailability(String date) throws GeneralSecurityException, IOException {
        return "Monday 2-3pm";
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



