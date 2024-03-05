# TODO gcal creation 

# TODO receive mic recording

from datetime import datetime

from openai import OpenAI
import os

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


def get_curr_details():
  now = datetime.now()
  return now.strftime("%H:%M:%S %Y-%m-%d %A")

mic_record = "Lets meet next tuesday at noon instead"

query = "The date & time now is", get_curr_details(), "and the transcripted voice recording is: ", mic_record,
"Based on the current date & time as well as the transcripted voice recording, calculate and return the date and time that the event has to be scheduled on",
"The output should be in the format YYYY-MM-DD-HH-MM-SS. Do not return anything else."

response = client.chat.completions.create(
    model="gpt-3.5-turbo",
    messages=[
        {
            "role": "system",
            "content": "you are a time retrieval assistant"
        },
        {"role": "user", 
         "content": query
        }
    ]
)
print(response)
  