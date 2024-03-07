from datetime import datetime

# Get current UTC date and time
current_datetime = datetime.utcnow()
print(current_datetime.strftime('%Y-%m-%dT%H:%M:%S'))