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
    SUPABASE_ANON_KEY = "xxxx" # Use value from .env

EMAIL = env_vars.get("TEST_USER_EMAIL", "xxxx@example.com")
PASSWORD = env_vars.get("TEST_USER_PASSWORD", "xxxx")
BASE_URL = "https://friendlens-backend.onrender.com"

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

print("=== STARTING PRODUCTION TESTS ===")

# 1. Base URL
print("--- Testing GET / ---")
try:
    with urllib.request.urlopen(BASE_URL) as res:
        print(f"✅ Success HTTP {res.getcode()}: {res.read().decode()}")
except Exception as e:
    print(f"❌ Failed Exception: {e}")

# 2. GET /api/users/me
make_request("GET", "/api/users/me")

# 3. PUT /api/users/me
make_request("PUT", "/api/users/me", payload={"username": "Production Test User", "avatarUrl": "http://img.com/a.png"})

# 4. GET /api/users/me Verify
user = make_request("GET", "/api/users/me")

# 5. POST /api/feedback
make_request("POST", "/api/feedback", payload={"content": "This app is great in production!", "rating": "5"})

# 6. POST /api/groups (Multipart with Image)
print("--- Testing POST /api/groups (Multipart with Image) ---")
boundary = "----TestBoundaryGroupProd"
body = (
    f"--{boundary}\r\n"
    f"Content-Disposition: form-data; name=\"name\"\r\n\r\n"
    f"Prod Group with Image\r\n"
    f"--{boundary}\r\n"
    f"Content-Disposition: form-data; name=\"description\"\r\n\r\n"
    f"Testing cover photo upload in prod\r\n"
    f"--{boundary}\r\n"
    f"Content-Disposition: form-data; name=\"image\"; filename=\"cover.jpg\"\r\n"
    f"Content-Type: image/jpeg\r\n\r\n"
    f"fake_image_bytes_prod\r\n"
    f"--{boundary}--\r\n"
).encode('utf-8')

group_res = make_request("POST", "/api/groups", payload=body, is_multipart=True, boundary=boundary)
group_id = None
join_code = None
if group_res and group_res.get("status") == "success":
    group_id = group_res["group"]["id"]
    join_code = group_res["group"]["joinCode"]
    if group_res["group"].get("coverImageUrl"):
        print("✅ Cover Image URL verified in production!")
    else:
        print("⚠️ Cover Image URL missing in production response.")

if group_id:
    # 7. GET /api/groups
    make_request("GET", "/api/groups")

    # 8. GET /api/groups/{id}
    make_request("GET", f"/api/groups/{group_id}")

    # 9. POST /api/groups/join
    make_request("POST", "/api/groups/join", payload={"joinCode": join_code})

    # 10. POST /api/groups/{id}/photos/upload
    boundary = "----TestBoundaryProd12345"
    body = (
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"image\"; filename=\"prod_test.jpg\"\r\n"
        f"Content-Type: image/jpeg\r\n\r\n"
        f"fake_image_bytes\r\n"
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"capturedAt\"\r\n\r\n"
        f"2026-02-22T10:00:00\r\n"
        f"--{boundary}--\r\n"
    ).encode('utf-8')
    photo_res = make_request("POST", f"/api/groups/{group_id}/photos/upload", payload=body, is_multipart=True, boundary=boundary)
    
    # 11. GET /api/groups/{id}/photos
    make_request("GET", f"/api/groups/{group_id}/photos")
    
    if photo_res and "photo" in photo_res:
        photo_id = photo_res["photo"]["id"]
        
        # 12. POST /api/groups/{id}/photos/{photoId}/like
        make_request("POST", f"/api/groups/{group_id}/photos/{photo_id}/like")

        # 13. DELETE /api/groups/{id}/photos/{photoId}/like
        make_request("DELETE", f"/api/groups/{group_id}/photos/{photo_id}/like")

print("\n🎉 ALL PRODUCTION ENDPOINT TESTS COMPLETED!")


