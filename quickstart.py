# TODO gcal creation 

# TODO receive mic recording

from datetime import datetime

import os

from openai import OpenAI

client = OpenAI(
    api_key="Secret_key" # remember to use key
)

def get_curr_details():
  now = datetime.now()
  return now.strftime("%H:%M:%S %Y-%m-%d %A")

mic_record = "Lets meet on july 12th at 8pm"

query = "The date & time now is"+ get_curr_details() + \
    "and the transcripted voice recording is: "+ mic_record + \
    "Based on the current date & time as well as the transcripted voice recording, calculate and return the date and time that the event has to be scheduled on" \
    "The output should be in the format YYYY-MM-DD-HH-MM-SS. Do not return anything else."

completion = client.chat.completions.create(
  model="gpt-3.5-turbo",
  messages=[
    {"role": "user", "content": query}
  ]
)

print(completion.choices[0].message.content)
