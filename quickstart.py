from datetime import datetime

import os

import json

from openai import OpenAI

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

SCOPES = ["https://www.googleapis.com/auth/calendar.events"]

# TODO receive mic recording

client = OpenAI(
    api_key="secret_key" # remember to use key
)

def get_curr_details():
  now = datetime.now()
  return now.strftime("%H:%M:%S %Y-%m-%d %A")

mic_record = "Lets meet on next tuesday between 12 to 1 pm"

query = "The date & time now is"+ get_curr_details() + \
    "and the transcripted voice recording is: "+ mic_record + \
    "Based on the current date & time as well as the transcripted voice recording, fill in the JSON format specified" \
    "Return the JSON format specified, do not return anything else."

functions = [
  {
    "name": "add_event",
    "description": "Adds a new event to the calendar",
    "parameters": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string",
          "description": "name of the event"
        },
        "event_description": {
          "type": "string",
          "description": "The description of the event"
        },
        "start_year": {
          "type": "integer",
          "description": "The year in which the event starts"
        },
        "end_year": {
          "type": "integer",
          "description": "The year in which the event ends"
        },
        "start_month": {
          "type": "integer",
          "description": "The month in which the event start"
        },
        "end_month": {
          "type": "integer",
          "description": "The month in which the event ends"
        },
        "start_day": {
          "type": "integer",
          "description": "The day in which the event starts"
        },
        "end_day": {
          "type": "integer",
          "description": "The day in which the event ends"
        },
        "start_time": {
          "type": "integer",
          "description": "The start time in 24hr format"
        },
        "end_time": {
          "type": "integer",
          "description": "The end time in 24hr format. If no time is specified, the end time should be one hour after the start"
        }
      },
      "required": ["name", "event_description", "start_year", "start_month", "start_day", "start_time", "end_year", "end_month", "end_day", "end_time"]
    }
  },
]

completion = client.chat.completions.create(
  model="gpt-3.5-turbo-0613",
  messages=[
    {"role": "user", "content": query}
    ],
  functions=functions,
  stream=False,
)

data = json.loads(completion.choices[0].message.function_call.arguments)

start_year = data['start_year']
start_month = data['start_month']
start_day = data['start_day']
start_time = data['start_time']

end_year = data['start_year']
end_month = data['start_month']
end_day = data['start_day']
end_time = data['start_time']

start_timestamp = datetime(start_year, start_month, start_day, start_time).isoformat()

end_timestamp = datetime(end_year, end_month, end_day, end_time).isoformat()

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
  event = {
    'summary': data['name'],
    'description': data['event_description'],
    'start': {
        'dateTime': start_timestamp,
        'timeZone': 'America/New_York',
    },
    'end': {
        'dateTime': end_timestamp,
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