package com.vuzix.ultralite.sample;

import android.Manifest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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

import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.TextAlignment;
import com.vuzix.ultralite.TextWrapMode;
import com.vuzix.ultralite.UltraliteColor;
import com.vuzix.ultralite.UltraliteSDK;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSION_REQUEST_INTERNET = 1;
    private EditText notificationEditText;
    private SpeechRecognizer speechRecognizer;
    private static final String OpenAiToken = "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" ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
        }

        // Initialize views
//        ImageView installedImageView = findViewById(R.id.installed);
        ImageView linkedImageView = findViewById(R.id.linked);
//        TextView nameTextView = findViewById(R.id.name);
//        ImageView connectedImageView = findViewById(R.id.connected);
        Button notificationButton = findViewById(R.id.send_notification);
        notificationEditText = findViewById(R.id.notification_text);

        UltraliteSDK ultralite = UltraliteSDK.get(this);

//        ultralite.getAvailable().observe(this, available ->
//                installedImageView.setImageResource(available ? R.drawable.ic_check_24 : R.drawable.ic_close_24));

        ultralite.getLinked().observe(this, linked -> {
            linkedImageView.setImageResource(linked ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
//            nameTextView.setText(ultralite.getName());
        });

//        ultralite.getConnected().observe(this, connected -> {
//            connectedImageView.setImageResource(connected ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
//            notificationButton.setEnabled(connected);
//        });

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
                // Process speech recognition results
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String query = matches.get(0); // Take the first result
                    // Pass the query to GPT
                    queryGPT(query);
                } else {
                    Toast.makeText(MainActivity.this, "No speech input recognized", Toast.LENGTH_SHORT).show();
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

    private void startSpeechRecognition() {
        // Create speech recognition intent
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        // Start speech recognition
        speechRecognizer.startListening(speechIntent);
    }


    private void queryGPT(String query) {
        UltraliteSDK ultralite = UltraliteSDK.get(this);
        query = query + "(Answer in less than 20 words)";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new PostTask(query));
        try {
            String result = future.get(); // This will block until the task is completed
            JSONObject responseJson = new JSONObject(result);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                notificationEditText.setText(content);
                String notificationText = notificationEditText.getText().toString();
                ultralite.sendNotification("Jarvis",  notificationText,
                        loadLVGLImage(this, R.drawable.rocket));
            } else {
                notificationEditText.setText("No response content found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
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
