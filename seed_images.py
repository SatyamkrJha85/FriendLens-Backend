import os
import json
import urllib.request
from urllib.error import HTTPError
import time
import random
import uuid

# Read .env
env_vars = {}
try:
    with open('.env', 'r') as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#'):
                k, v = line.split('=', 1)
                env_vars[k] = v
except:
    pass

SUPABASE_URL = env_vars.get('SUPABASE_URL')
SUPABASE_ANON_KEY = env_vars.get('SUPABASE_KEY', "xxxx")

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
    print(f"❌ Failed to login to Supabase for test! {e}")
    if hasattr(e, 'read'):
        print(e.read().decode())
    import traceback
    traceback.print_exc()
    exit(1)

def get_random_image_bytes(width=600, height=800):
    url = f"https://picsum.photos/{width}/{height}?random={random.randint(1, 10000)}"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        with urllib.request.urlopen(req) as res:
            return res.read()
    except Exception as e:
        print(f"Failed to fetch image from {url}: {e}")
        return None

def make_multipart_request(endpoint, fields, files):
    url = f"{BASE_URL}{endpoint}"
    boundary = f"----WebKitFormBoundary{uuid.uuid4().hex}"
    
    body = b""
    for key, val in fields.items():
        if val is not None:
            body += f"--{boundary}\r\n".encode('utf-8')
            body += f"Content-Disposition: form-data; name=\"{key}\"\r\n\r\n".encode('utf-8')
            body += f"{val}\r\n".encode('utf-8')
            
    for key, (filename, file_bytes) in files.items():
        body += f"--{boundary}\r\n".encode('utf-8')
        body += f"Content-Disposition: form-data; name=\"{key}\"; filename=\"{filename}\"\r\n".encode('utf-8')
        body += b"Content-Type: image/jpeg\r\n\r\n"
        body += file_bytes
        body += b"\r\n"
        
    body += f"--{boundary}--\r\n".encode('utf-8')
    
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": f"multipart/form-data; boundary={boundary}"
    }
    
    req = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req) as res:
            res_body = res.read().decode()
            return True, json.loads(res_body) if res_body else None
    except HTTPError as e:
        return False, e.read().decode()

groups_to_create = [
    {"name": "Mountain Trip 2026", "description": "Awesome hiking trip with the squad!"},
    {"name": "City Explorers", "description": "Urban adventures."}
]

for g_info in groups_to_create:
    print(f"\n📁 Creating Group: {g_info['name']}")
    cover_img = get_random_image_bytes(800, 600)
    
    files = {}
    if cover_img:
        files['image'] = ('cover.jpg', cover_img)
        
    success, res = make_multipart_request("/api/groups", {"name": g_info["name"], "description": g_info["description"]}, files)
    if not success:
        print(f"❌ Failed to create group: {res}")
        continue
        
    group_id = res['group']['id']
    print(f"✅ Group created. ID: {group_id}")
    
    print("📸 Uploading 10 fake photos to this group...")
    for i in range(10):
        photo_bytes = get_random_image_bytes(800, 1000)
        if not photo_bytes:
            print(f"  [{i+1}/10] Failed to download image from picsum.")
            continue
            
        fields = {"capturedAt": "2026-02-22T12:00:00"}
        success, photo_res = make_multipart_request(f"/api/groups/{group_id}/photos/upload", fields, {"image": (f"photo_{i}.jpg", photo_bytes)})
        
        if success:
            print(f"  [{i+1}/10] Uploaded successfully.")
        else:
            print(f"  [{i+1}/10] Failed: {photo_res}")
        time.sleep(0.5)

print("\n🎉 Seeding completed! Real images are now visible in the app.")
