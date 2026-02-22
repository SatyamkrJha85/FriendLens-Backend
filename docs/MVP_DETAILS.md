# FriendLens — MVP Details & Implementation Plan

##  Goal
Deliver the core value proposition: **"Effortless group photo syncing across devices, storing original quality, with a zero-friction invite flow."**

This document details the engineering specifications, UX flows, and exact features required for **Phase 1 (Months 1–3)**.

---

##  Core MVP Features
*Scope is strictly limited to ensure rapid launch.*

1. **User Auth & Profiles**
   - Simple Email/Password Registration.
   - Profile picture and display name.
2. **Group Management**
   - Create a group (Name, Cover Image, Start/End Dates).
   - Generate unique QR code & Shareable invite link.
3. **Photo Upload & Sync**
   - Background photo uploading (listener on device's camera roll during the active Group window).
   - Manual upload fallback.
   - Original quality (no downscaling before S3 upload) - *MVP selling point*.
4. **Shared Gallery**
   - Real-time/Pulled timeline of all group photos.
   - Display uploader's name on each photo.
5. **Photo Download**
   - Save full-res photo to device.

---

##  Core User Flows

### Flow 1: Group Creation & Invite
1. User taps "Create New Group".
2. Inputs Group Name, Dates (e.g., "Weekend Trip: Friday to Sunday").
3. App presents a large QR Code and a "Copy Link" button.
4. Friends scan QR with standard camera -> redirects to App Store/Play Store (or App if installed).
5. Friend opens App -> Deep-link joins them into the group instantly.

### Flow 2: Auto-Upload (The "Magic" Moment)
1. User joins group and grants Photo Library permission.
2. App sets a background task / listener for new photos added to device library.
3. If new photo timestamp matches the Group's active time window, it queues for upload.
4. Image is uploaded chunked/directly to S3.
5. Notification sent / feed updated for other group members.

### Flow 3: Viewing & Downloading
1. User opens group gallery.
2. Grid view of thumbnails (served via CDN/resized).
3. Tap photo -> Full screen view -> Tap "Download" -> Saved to device gallery at original resolution.

---

##  Database Schema (PostgreSQL MVP)

### `users`
- `id` (UUID, PK)
- `email` (String, Unique)
- `password_hash` (String)
- `display_name` (String)
- `profile_pic_url` (String, nullable)
- `created_at` (Timestamp)

### `groups`
- `id` (UUID, PK)
- `name` (String)
- `created_by` (UUID, FK -> users)
- `cover_image_url` (String, nullable)
- `invite_code` (String, Unique, Indexed)
- `start_date` (Timestamp)
- `end_date` (Timestamp)
- `created_at` (Timestamp)

### `group_members`
- `group_id` (UUID, FK -> groups)
- `user_id` (UUID, FK -> users)
- `role` (Enum: ADMIN, MEMBER)
- `joined_at` (Timestamp)
- *PK is (group_id, user_id)*

### `photos`
- `id` (UUID, PK)
- `group_id` (UUID, FK -> groups)
- `uploaded_by` (UUID, FK -> users)
- `s3_key_original` (String)
- `s3_key_thumbnail` (String)
- `captured_at` (Timestamp) - *Exif data*
- `uploaded_at` (Timestamp)
- `file_size_bytes` (Long)

---

##  API Endpoints (Ktor)

**Auth**
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login` (Returns JWT)

**Groups**
- `POST /api/v1/groups` (Create)
- `GET /api/v1/groups` (List user's groups)
- `GET /api/v1/groups/{id}` (Details)
- `POST /api/v1/groups/join` (Body: invite_code)

**Photos**
- `GET /api/v1/groups/{id}/photos` (Paginated list, returns thumbnail URLs)
- `POST /api/v1/groups/{id}/photos/url` (Get pre-signed S3 URL for upload)
- `POST /api/v1/groups/{id}/photos` (Confirm upload, writes to DB)

---

##  Architecture & Infrastructure Notes

1. **Storage:** Use AWS S3 (or MinIO for local dev). Generate **Pre-signed URLs** from the Ktor backend so the mobile app uploads directly to S3, bypassing Ktor for heavy binary payloads.
2. **Thumbnail Generation:**
   - *Cheap MVP:* Mobile app generates a compressed thumbnail and uploads it alongside the original.
   - *Better MVP:* AWS Lambda / Serverless function triggers on S3 upload, resizes, and saves the thumbnail. (Recommended)
3. **Real-time Updates:** For MVP, simple pull-to-refresh on the mobile client is sufficient. If time permits, adding Server-Sent Events (SSE) via Ktor or simple WebSocket for "New photos available" badge.
4. **Mobile Stack Recommendations:**
   - React Native (Expo) or Kotlin Multiplatform (KMP) + Compose to hit iOS/Android simultaneously.
   - Crucial package needed: Background Fetch / Background UI task handler to upload photos while app is minimized.

##  Free Tech Stack Options for MVP (2025/2026)
To build and launch this MVP with **$0 in monthly hosting costs**, the following free-tier services are highly recommended based on the architecture constraints (Kotlin JVM + Postgres + S3 Object Storage).

### 1. Object Storage (Photos) - **Crucial for $0 without a Credit Card**
*   **Winner: Supabase Storage.** 
    *   **Why:** Since Cloudflare R2 requires a credit card file (even just for the free tier), we are pivoting to Supabase Storage. It gives 1 GB of storage and 5 GB of monthly bandwidth completely free with NO credit card required. Supabase Storage is also 100% S3-compatible, so it will work identically in our code.
*   *Runner-up: Backblaze B2 (10 GB free).* Requires a bit more setup but is a great fallback.

### 2. Backend API Hosting (Ktor / JVM)
JVM applications can be heavy on memory, limiting our choices for free hosting.
*   **Winner: Oracle Cloud "Always Free" Tier.**
    *   **Why:** Provides up to **4 ARM Ampere A1 Compute instances with 24GB RAM total**. This is unbelievably generous and perfect for JVM/Ktor which thrives on ample memory.
*   *Runner-up: Render (Free Web Service tier).* Great developer experience, auto-deploys from GitHub via Docker, but comes with 512MB RAM (tight for JVM) and spins down after 15 mins of inactivity.
*   *Runner-up 2: Fly.io (Hobby tier).* Good for deploying Dockerized Ktor apps close to users, but may require a credit card and careful usage monitoring to stay under the $5 waiver limit.

### 3. Database Hosting (PostgreSQL)
*   **Winner: Supabase.**
    *   **Why:** Excellent developer experience, 100% Postgres under the hood. The free tier gives 500MB of database space space which is plenty for early MVP users, groups, and basic photo metadata. 
*   *Runner-up: Neon.tech.* Serverless Postgres. Gives 500MB storage and 1GB RAM on the free tier, separating storage and compute. Outstanding auto-scaling.
*   *Runner-up 3: Aiven.* Offers a simple free Postgres DB with 5GB storage, 1 CPU, and 1GB RAM.

### 4. Authentication (Email/Password & Social)
*   **Winner: Supabase Auth OR Ktor Native.**
    *   **Why:** Supabase gives 50,000 Monthly Active Users for free. Alternatively, just writing our own JWT-based auth natively inside Ktor costs nothing, ensures we aren't vendor locked, and MVP data stays together.

---

##  Out of Scope for MVP
- Video support (too expensive/complex for Phase 1).
- Comments and Likes (distracts from the core utility).
- Complex facial recognition or AI tagging.
- Location-aware geofencing (time-based bounding only).
- Billing/Freemium limits (MVP will be free/unlimited to gather early adopters).
