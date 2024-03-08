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

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        gAuth = findViewById(R.id.signInButton);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("137591440076-7sa937qr4tvi3q4tnm3tmobmpil33dkn.apps.googleusercontent.com")
                .requestEmail().build();

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

    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());

            }
            catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println(e.getMessage());
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();

                            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                            startActivity(intent);

                            System.out.println(user.getEmail());
                        }
                        else {
                            Toast.makeText(MainActivity.this, "sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
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
        String availability = "Error getting availability";
        try {
            availability = getAvailability(date);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
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



