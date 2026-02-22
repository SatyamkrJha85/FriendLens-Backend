import os
import json
import urllib.request
from urllib.error import HTTPError
import time

env_vars = {}
with open('.env', 'r') as f:
    for line in f:
        line = line.strip()
        if line and not line.startswith('#'):
            key, val = line.split('=', 1)
            env_vars[key] = val

SUPABASE_URL = env_vars.get('SUPABASE_URL')
SUPABASE_ANON_KEY = env_vars.get('SUPABASE_KEY')
if not SUPABASE_ANON_KEY:
    SUPABASE_ANON_KEY = "xxxx"

EMAIL = env_vars.get("TEST_USER_EMAIL", "xxxx@example.com")
PASSWORD = env_vars.get("TEST_USER_PASSWORD", "xxxx")
BASE_URL = "http://localhost:8080"

print(f"Logging in {EMAIL}...")
auth_url = f"{SUPABASE_URL}/auth/v1/token?grant_type=password"
try:
    req = urllib.request.Request(auth_url, data=json.dumps({"email": EMAIL, "password": PASSWORD}).encode('utf-8'),
                                headers={"apikey": SUPABASE_ANON_KEY, "Content-Type": "application/json"}, method="POST")
    with urllib.request.urlopen(req) as response:
        token = json.loads(response.read().decode())['access_token']
        print("✅ Login successful. Token acquired.\n")
except Exception as e:
    print("❌ Failed to login to Supabase for test!")
    exit(1)

def make_request(method, endpoint, payload=None, is_multipart=False, boundary=None):
    url = f"{BASE_URL}{endpoint}"
    print(f"--- Testing {method} {endpoint} ---")
    headers = {"Authorization": f"Bearer {token}"}
    
    data = None
    if payload and not is_multipart:
        data = json.dumps(payload).encode('utf-8')
        headers["Content-Type"] = "application/json"
    elif payload and is_multipart:
        data = payload
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as res:
            res_body = res.read().decode()
            print(f"✅ Success HTTP {res.getcode()}")
            return json.loads(res_body) if res_body else None
    except HTTPError as e:
        print(f"❌ Failed HTTP {e.code}: {e.read().decode()}")
        return None
    except Exception as e:
        print(f"❌ Failed Exception: {e}")
        return None

# Test Feedback
make_request("POST", "/api/feedback", payload={"content": "This app is great!", "rating": "5"})

# Group & Photo testing to test like feature
group_res = make_request("POST", "/api/groups", payload={"name": "Like Test Group", "description": "Testing Likes"})
if group_res and "group" in group_res:
    group_id = group_res["group"]["id"]
    
    boundary = "----TestBoundaryLike"
    body = (
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"image\"; filename=\"test.jpg\"\r\n"
        f"Content-Type: image/jpeg\r\n\r\n"
        f"fake_image_bytes\r\n"
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"capturedAt\"\r\n\r\n"
        f"2026-02-22T10:00:00\r\n"
        f"--{boundary}--\r\n"
    ).encode('utf-8')
    photo_res = make_request("POST", f"/api/groups/{group_id}/photos/upload", payload=body, is_multipart=True, boundary=boundary)

    if photo_res and "photo" in photo_res:
        photo_id = photo_res["photo"]["id"]
        
        # Test Like Photo
        make_request("POST", f"/api/groups/{group_id}/photos/{photo_id}/like")

        # Test Unlike Photo
        make_request("DELETE", f"/api/groups/{group_id}/photos/{photo_id}/like")
    else:
        print("Failed to upload photo for like test")
else:
    print("Failed to create group for like test")

print("\n🎉 ALL NEW FEATURE TESTS COMPLETED!")
