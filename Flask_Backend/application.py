from flask import Flask
from datetime import datetime

import os

import json

from openai import OpenAI

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow, Flow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

from flask import Flask, jsonify
from google.oauth2 import service_account
import googleapiclient.discovery




app = Flask(__name__, static_folder="static")
localDomain = "http://localhost:5000"
publicDomain = "https://ccghwd.pythonanywhere.com/"
DOMAIN = localDomain

localPath = ""
publicPath = "/home/ccgHwd/mysite/"
PATH = localPath

SCOPES = [
    "https://www.googleapis.com/auth/calendar.events",
    "https://www.googleapis.com/auth/userinfo.email",
    "https://www.googleapis.com/auth/userinfo.profile",
    "openid"
]
open_ai_key ="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" # remember to use key
credentials_path = PATH + "static/credential.json"
token_path = PATH + "static/token.json"



@app.route('/')
def hello_world():
    return 'Hello from Thad!'


#"https://ccghwd.pythonanywhere.com/everyday/wear/rest/api/speech/output/<temp>/<auth_code>/<voice_input>"
@app.route("/everyday/wear/rest/api/speech/output/<temp>/<auth_code>/<voice_input>")
def basic_command(temp, auth_code, voice_input):
    if "schedule" in voice_input.lower() :
      auth_code = f"{temp}/{auth_code}"
      print(auth_code)
      output = calander(voice_input, auth_code)
    else:
       output = "no sched"
    return output
    #return output # f"Anv passed {voice_input} with code {auth_code}"


def calander(voice_input, auth_code):
  client = OpenAI(
    api_key=open_ai_key
  )

  mic_record = voice_input

  now = datetime.now()
  curr_details = now.strftime("%H:%M:%S %Y-%m-%d %A")
  query = "The date & time now is"+ curr_details + \
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

  print("a")
  creds = get_creds_from_auth_code(auth_code)
  print("b")
  # if not creds or not creds.valid:
  #   if creds and creds.expired and creds.refresh_token:
  #     creds.refresh(Request())
  #   else:
  #     flow = InstalledAppFlow.from_client_secrets_file(
  #         credentials_path, SCOPES
  #     )
  #     creds = flow.run_local_server(port=57890)
    # Save the credentials for the next run
    # with open("token.json", "w") as token:
    #   token.write(creds.to_json())

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
          {'email': 'xxxxx@gmail.com'},
      ],
      }
    event = service.events().insert(calendarId='primary', body=event).execute()
    event = f"Event : {data['name']},\n When : {start_timestamp},\n"

    print (event)
    return event
  except HttpError as error:
    print(f"An error occurred: {error}")
    return f"An error occurred: {error}"


def get_creds_from_auth_code(auth_code):
  flow = Flow.from_client_secrets_file(
        credentials_path,
        scopes=SCOPES,
        redirect_uri='urn:ietf:wg:oauth:2.0:oob'  # This redirect URI is used for apps that do not have a web server
  )

  # Exchange the authorization code for a credentials object
  # You need to ensure that this auth_code is the one you got from the front end
  flow.fetch_token(code=auth_code)

  credentials = flow.credentials
  return credentials


if DOMAIN != publicDomain:
    if __name__ == '__main__':
        app.debug = True
        app.run()

