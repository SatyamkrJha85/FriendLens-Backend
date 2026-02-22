# FriendLens Backend API Documentation

Welcome to the **FriendLens** backend API documentation. This API is built with **Ktor (Kotlin)**, **Exposed** (ORM for PostgreSQL), and is designed to integrate seamlessly with **Supabase** for Authentication (via JWKS) and Storage (via S3 buckets).

**Base URL:** `https://friendlens-backend.onrender.com`

---

## 🔒 Authentication

All API endpoints (except the root ping `/`) require a Supabase-issued JSON Web Token (JWT).
You must pass this token in the `Authorization` header of every HTTP request:

```http
Authorization: Bearer <YOUR_SUPABASE_JWT_TOKEN>
```

The backend dynamically verifies the signature against your Supabase project's public keys. It operates off the `sub` claim (which holds the Supabase Auth UUID).

---

## 🧑‍💻 User Management

### 1. Get Current User Profile
Automatically creates a profile in the database if it doesn't exist, and returns the current user details.

* **Endpoint:** `GET /api/users/me`
* **Response:**
```json
{
  "status": "success",
  "userId": "uuid-here",
  "email": "user@email.com",
  "username": "Current Name",
  "avatarUrl": "https://img..."
}
```

### 2. Update User Profile
Updates the user's display name or avatar URL.

* **Endpoint:** `PUT /api/users/me`
* **Body (JSON):**
```json
{
  "username": "New Name",
  "avatarUrl": "https://newurl.com/a.png"
}
```
* **Response:**
```json
{
  "status": "success",
  "message": "Profile updated successfully"
}
```

---

## 👥 Groups

Groups are collaborative albums where friends can upload and view photos.

### 1. Create a Group
Creates a new group, making the creator the `owner`, and generates a unique random 6-character `joinCode`. Optionally, you can upload a cover image for the group.

* **Endpoint:** `POST /api/groups`
* **Headers:** `Content-Type: multipart/form-data`
* **Form Data Parts:**
  * `name`: (Text type, required, the name of the group)
  * `description`: (Text type, optional, the group description)
  * `image`: (File type, optional, the cover image binary bytes)
* **Response:**
```json
{
  "status": "success",
  "group": {
    "id": "group-uuid-here",
    "name": "Trip to Hawaii",
    "description": "2026 vacay!",
    "joinCode": "AB12CD",
    "coverImageUrl": "https://bucket.../cover.jpg"
  }
}
```

### 2. Get All User Groups
Fetches a list of all groups the current user is a member of.

* **Endpoint:** `GET /api/groups`
* **Response:**
```json
{
  "status": "success",
  "groups": [
    {
      "id": "group-uuid",
      "name": "Trip to Hawaii",
      "description": "2026 vacay!",
      "joinCode": "AB12CD"
    }
  ]
}
```

### 3. Get Group Details
Fetches detailed info about a specific group by its `UUID`. **Requires membership.**

* **Endpoint:** `GET /api/groups/{id}`

### 4. Join a Group
Allows a user to join a group using the 6-character `joinCode`.

* **Endpoint:** `POST /api/groups/join`
* **Body (JSON):**
```json
{
  "joinCode": "AB12CD"
}
```

---

## 📸 Photos

### 1. Upload a Photo
Uploads raw image bytes to Supabase Storage (S3) and logs the entry in the Postgres database. **Requires membership to the group.**

* **Endpoint:** `POST /api/groups/{id}/photos/upload`
* **Headers:** `Content-Type: multipart/form-data`
* **Form Data Parts:**
  * `image`: (File type, binary bytes of the photo)
  * `capturedAt`: (Text type, optional ISO timestamp e.g. `2026-02-22T10:00:00`)
* **Response:**
```json
{
  "status": "success",
  "photo": {
    "id": "photo-uuid",
    "originalUrl": "https://bucket.../photo.jpg",
    "s3Key": "groups/group-uuid/photos/photo.jpg"
  }
}
```

### 2. Get All Group Photos
Returns all photos uploaded to the group, including the uploader's basic info. **Requires membership.**

* **Endpoint:** `GET /api/groups/{id}/photos`
* **Response:**
```json
{
  "status": "success",
  "photos": [
    {
      "id": "photo-uuid",
      "originalUrl": "https://...",
      "uploadedBy": "user-uuid",
      "uploadedByUsername": "John Doe",
      "uploadedByAvatar": "https://..."
    }
  ]
}
```

### 3. Like a Photo
Logs a "Like" on a specific photo by the current user.

* **Endpoint:** `POST /api/groups/{id}/photos/{photoId}/like`

### 4. Unlike a Photo
Removes a previous "Like" from a photo.

* **Endpoint:** `DELETE /api/groups/{id}/photos/{photoId}/like`

---

## 📝 General

### 1. Submit App Feedback
Allows users to submit textual feedback and a star rating directly to the database.

* **Endpoint:** `POST /api/feedback`
* **Body (JSON):**
```json
{
  "content": "This app is wonderful!",
  "rating": "5"
}
```
