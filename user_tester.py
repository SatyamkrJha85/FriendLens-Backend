import jwt
import urllib.request
from urllib.error import HTTPError
import json
import uuid
from datetime import datetime, timedelta

# 1. Provide your secret from the .env directly to generate a fake JWT that Ktor will trust
SECRET = "your_jwt_secret_xxxx"
user_id = "your_test_user_uuid_xxxx" # A generated static uuid for test user.

# 2. Build exactly what Supabase issues
payload = {
    "aud": "authenticated",
    "exp": int((datetime.now() + timedelta(hours=1)).timestamp()),
    "sub": user_id,
    "email": "test@developer.friendlens.com",
    "phone": "",
    "app_metadata": {"provider": "email", "providers": ["email"]},
    "user_metadata": {},
    "role": "authenticated",
    "aal": "aal1",
    "amr": [{"method": "password", "timestamp": int(datetime.now().timestamp())}],
    "session_id": str(uuid.uuid4())
}

encoded_jwt = jwt.encode(payload, SECRET, algorithm="HS256")

headers = {
    "Authorization": f"Bearer {encoded_jwt}",
    "Content-Type": "application/json"
}

def make_request(method, url, data=None):
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as res:
            response_body = res.read().decode()
            print(response_body)
            return json.loads(response_body) if response_body else None
    except HTTPError as e:
        print(f"HTTP Error {e.code}: {e.read().decode()}")
        return None

print("== 1. Testing GET /api/users/me ==")
make_request("GET", "http://localhost:8080/api/users/me")

print("\n== 2. Testing POST /api/groups ==")
group_data = json.dumps({"name": "Weekend Retreat", "description": "Going to the mountains!"}).encode()
group_res = make_request("POST", "http://localhost:8080/api/groups", data=group_data)

if group_res and "group" in group_res:
    group = group_res["group"]
    join_code = group["joinCode"]
    group_id = group["id"]
    print(f"\nCreated Group ID: {group_id}")
    print(f"Join Code: {join_code}")

    print(f"\n== 3. Testing POST /api/groups/join ==")
    join_data = json.dumps({"joinCode": join_code}).encode()
    make_request("POST", "http://localhost:8080/api/groups/join", data=join_data)

    print("\n== 4. Testing GET /api/groups/ ==")
    make_request("GET", "http://localhost:8080/api/groups")

    print(f"\n== 5. Testing GET /api/groups/{group_id}/photos ==")
    make_request("GET", f"http://localhost:8080/api/groups/{group_id}/photos")
else:
    print("Failed to create group")
