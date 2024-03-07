# TODO gcal creation 

# TODO receive mic recording

from datetime import datetime

import os

from openai import OpenAI

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

SCOPES = ["https://www.googleapis.com/auth/calendar.events"]

def main():
  """Shows basic usage of the Google Calendar API.
  Prints the start and name of the next 10 events on the user's calendar.
  """
  creds = None
  # The file token.json stores the user's access and refresh tokens, and is
  # created automatically when the authorization flow completes for the first
  # time.
  if os.path.exists("token.json"):
    creds = Credentials.from_authorized_user_file("token.json", SCOPES)
  # If there are no (valid) credentials available, let the user log in.
  if not creds or not creds.valid:
    if creds and creds.expired and creds.refresh_token:
      creds.refresh(Request())
    else:
      flow = InstalledAppFlow.from_client_secrets_file(
          "new_res/new_credential.json", SCOPES
      )
      creds = flow.run_local_server(port=57890)
    # Save the credentials for the next run
    with open("token.json", "w") as token:
      token.write(creds.to_json())

  try:
    service = build("calendar", "v3", credentials=creds)

    # Call the Calendar API
    # now = datetime.datetime.utcnow().isoformat() + "Z"  # 'Z' indicates UTC time
    # print("Getting the upcoming 10 events")
    # events_result = (
    #     service.events()
    #     .list(
    #         calendarId="primary",
    #         timeMin=now,
    #         maxResults=10,
    #         singleEvents=True,
    #         orderBy="startTime",
    #     )
    #     .execute()
    # )
    # events = events_result.get("items", [])

    # if not events:
    #   print("No upcoming events found.")
    #   return

    # # Prints the start and name of the next 10 events
    # for event in events:
    #   start = event["start"].get("dateTime", event["start"].get("date"))
    #   print(start, event["summary"])
      
    event = {
    'summary': 'Test Even Create',
    'description': 'Test Event Creation',
    'start': {
        'dateTime': '2024-03-07T00:36:00',
        'timeZone': 'America/New_York',
    },
    'end': {
        'dateTime': '2024-03-07T00:38:00',
        'timeZone': 'America/New_York',
    },
    'attendees': [
        {'email': 'anand.anvith@gmail.com'},
    ],
    }

    event = service.events().insert(calendarId='primary', body=event).execute()
    print ('Event created: %s' % (event.get('htmlLink')))

  except HttpError as error:
    print(f"An error occurred: {error}")


if __name__ == "__main__":
  main()

# client = OpenAI(
#     api_key="Secret_key" # remember to use key
# )

# def get_curr_details():
#   now = datetime.now()
#   return now.strftime("%H:%M:%S %Y-%m-%d %A")

# mic_record = "Lets meet on july 12th at 8pm"

# query = "The date & time now is"+ get_curr_details() + \
#     "and the transcripted voice recording is: "+ mic_record + \
#     "Based on the current date & time as well as the transcripted voice recording, calculate and return the date and time that the event has to be scheduled on" \
#     "The output should be in the format YYYY-MM-DD-HH-MM-SS. Do not return anything else."

# completion = client.chat.completions.create(
#   model="gpt-3.5-turbo",
#   messages=[
#     {"role": "user", "content": query}
#   ]
# )

# print(completion.choices[0].message.content)
