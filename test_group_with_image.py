import os
import json
import urllib.request
from urllib.error import HTTPError

env_vars = {}
with open('.env', 'r') as f:
    for line in f:
        line = line.strip()
        if line and not line.startswith('#'):
            k, v = line.split('=', 1)
            env_vars[k] = v

SUPABASE_URL = env_vars.get('SUPABASE_URL')
SUPABASE_ANON_KEY = env_vars.get('SUPABASE_KEY')
EMAIL = env_vars.get("TEST_USER_EMAIL", "xxxx@example.com")
PASSWORD = env_vars.get("TEST_USER_PASSWORD", "xxxx")
BASE_URL = "http://localhost:8080" # Test local first

print(f"Logging in {EMAIL}...")
auth_url = f"{SUPABASE_URL}/auth/v1/token?grant_type=password"
try:
    req = urllib.request.Request(auth_url, data=json.dumps({"email": EMAIL, "password": PASSWORD}).encode('utf-8'),
                                headers={"apikey": SUPABASE_ANON_KEY, "Content-Type": "application/json"}, method="POST")
    with urllib.request.urlopen(req) as response:
        token = json.loads(response.read().decode())['access_token']
        print("✅ Login successful.\n")
except Exception as e:
    print(f"❌ Login failed: {e}")
    exit(1)

def test_create_group_with_image():
    print("--- Testing POST /api/groups (Multipart with Image) ---")
    boundary = "----TestBoundaryGroup"
    
    # Simulate multipart form data
    body = (
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"name\"\r\n\r\n"
        f"Group with Image\r\n"
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"description\"\r\n\r\n"
        f"Testing cover photo upload\r\n"
        f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"image\"; filename=\"cover.jpg\"\r\n"
        f"Content-Type: image/jpeg\r\n\r\n"
        f"fake_image_bytes_xyz\r\n"
        f"--{boundary}--\r\n"
    ).encode('utf-8')

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": f"multipart/form-data; boundary={boundary}"
    }

    req = urllib.request.Request(f"{BASE_URL}/api/groups", data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req) as res:
            res_body = json.loads(res.read().decode())
            print(f"✅ Success: {json.dumps(res_body, indent=2)}")
            if res_body.get("group", {}).get("coverImageUrl"):
                print("🔥 Cover Image URL present!")
            else:
                print("⚠️ Cover Image URL missing from response.")
    except Exception as e:
        print(f"❌ Failed: {e}")
        if hasattr(e, 'read'):
            print(e.read().decode())

if __name__ == "__main__":
    test_create_group_with_image()
