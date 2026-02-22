import jwt
import urllib.request
from urllib.error import HTTPError
import json
import uuid
from datetime import datetime, timedelta

SECRET = "KbgG5Mg5xaVbqxIX7A9yZxWRJQvnAt/q8Mn/0MiX9JarcQ/XtY4WPkRVralqbFWcTuDsSwl87cmOkqwSi2vY9Q=="
user_id = "5a0ff5ac-d487-448b-862a-c0634257c218"

def test_token(role, aud, exp_delta_hours):
    payload = {
        "aud": aud,
        "exp": int((datetime.now() + timedelta(hours=exp_delta_hours)).timestamp()),
        "sub": user_id,
        "role": role,
    }
    encoded_jwt = jwt.encode(payload, SECRET, algorithm="HS256")
    headers = {
        "Authorization": f"Bearer {encoded_jwt}",
        "Content-Type": "application/json"
    }
    req = urllib.request.Request("http://localhost:8080/api/users/me", headers=headers, method="GET")
    try:
        with urllib.request.urlopen(req) as res:
            print(f"Role: {role}, Aud: {aud}, Exp: {exp_delta_hours} -> Success: {res.read().decode()}")
    except HTTPError as e:
        print(f"Role: {role}, Aud: {aud}, Exp: {exp_delta_hours} -> Failed: HTTP {e.code}")

print("Testing valid token:")
test_token("authenticated", "authenticated", 1)

print("Testing expired token:")
test_token("authenticated", "authenticated", -1)

print("Testing anon role token:")
test_token("anon", "authenticated", 1)

print("\n== Testing PUT /api/users/me ==")
payload = {
    "aud": "authenticated",
    "exp": int((datetime.now() + timedelta(hours=1)).timestamp()),
    "sub": user_id,
    "role": "authenticated",
}
encoded_jwt = jwt.encode(payload, SECRET, algorithm="HS256")

req = urllib.request.Request(
    "http://localhost:8080/api/users/me",
    data=json.dumps({"username": "Test User", "avatarUrl": "https://example.com/avatar.png"}).encode('utf-8'),
    headers={"Authorization": f"Bearer {encoded_jwt}", "Content-Type": "application/json"},
    method="PUT"
)
try:
    with urllib.request.urlopen(req) as res:
        print(f"Success: {res.read().decode()}")
except HTTPError as e:
    print(f"Failed: HTTP {e.code}")

print("\n== Testing GET /api/users/me again ==")
req2 = urllib.request.Request(
    "http://localhost:8080/api/users/me",
    headers={"Authorization": f"Bearer {encoded_jwt}", "Content-Type": "application/json"}
)
try:
    with urllib.request.urlopen(req2) as res:
        print(f"Success: {res.read().decode()}")
except HTTPError as e:
    print(f"Failed: HTTP {e.code}")
