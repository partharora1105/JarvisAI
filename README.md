# Jarvis AI - Smart Assistant for AR Glasses

Jarvis is an AI assistant designed to integrate with [Vuzix Z100](https://www.vuzix.com/products/z100-smart-glasses) smart glasses, enriching your daily experiences and enhancing productivity. Functioning as your intuitive companion, Jarvis perceives what you see, interprets what you hear, and executes tasks, delivering relevant information promptly to your glasses to effortlessly blend your real and virtual worlds.

## Overview

Jarvis AI operates through an Android application linked to vuzix glasses using their [SDK](https://github.com/Vuzix/ultralite-sdk-android) and a Flask backend. The Android app listens to conversations, and when the keyword "Jarvis" is detected, it sends a request to the Flask backend. The request is processed, completing tasks like scheduling an event, and an appropriate response is delivered to be displayed on the smart glasses. Currently the implementation doesn't use any camera but is envisioned to leverage one in future.

## Commands for Jarvis

To activate Jarvis, currently, it gets activated by saying "Jarvis". Based on these keywords while speaking, it will understand the context and take appropriate action. The watch app, which is a work in progress, could also be used to trigger Jarvis as well:

1. "Schedule" : Schedules an event on your Google Calendar based on the conversation.
2. "Events" : Shows your events on the calendar for a specific day being talked about.
3. "Notes" : Gets access to your notes and responds according to what's needed (Currently stored within [directory](https://github.com/partharora1105/JarvisAI/blob/main/Flask_Application/static/notes.txt)).
4. "Remember" : Activates the [Remembrance Agent](https://cdn.aaai.org/Symposia/Spring/1996/SS-96-02/SS96-02-022.pdf) (Note, this is not available in the current version but is a work in progress).
5. Anything else : Triggers a response from choosen LLM (Currently using GPT 3.5).

## Directory Structure

- **Android_App**: Contains the Android application for Vuzix Z100 smart glasses.
- **Flask_Application**: Contains the Flask backend for processing user requests.
- **Watch_App**: Template for the upcoming watch interface.

## Getting Started

### Running the Android App

To run the Android app:
1. Use [Android Studio](https://developer.android.com/studio) to open the project located in the "Android_App" directory.
2. Build the app for your Android device.
3. Make sure your Android device is connected to your glasses.
4. Open the app and connect your Google account when prompted.
5. Click on "Start Listening" and enjoy all-time assistance.

### Running the Flask Backend

Note: For now, this is not a required step as the Android version is already querying our backend which processes your request. However, if you want to make any changes to how the AI responds, this is needed.

To run the Flask backend:
1. Navigate to the "Flask_Application" directory.
2. Install all dependencies listed in the "requirements.txt" file using pip:
    ```
    pip install -r requirements.txt
    ```
3. Host the Flask application somewhere accessible; we are currently hosted on [https://ccghwd.pythonanywhere.com/](https://ccghwd.pythonanywhere.com/).
4. Add the endpoint of the hosted backend to the Android app.

## Contribution

Contributions to Jarvis AI are welcome! Feel free to submit pull requests or reach out with any ideas or suggestions for improvement.

## License

This project is licensed under the [MIT License](LICENSE).

---

For any inquiries or support, please contact parth.03.arora@gmail.com.
