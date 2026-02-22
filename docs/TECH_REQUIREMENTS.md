# FriendLens — Technical Requirements

## API Endpoints (Backend — Ktor)

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Email + password registration |
| POST | `/auth/login` | Login, returns JWT |
| POST | `/auth/refresh` | Refresh token |
| POST | `/auth/oauth/google` | Google OAuth flow |
| POST | `/auth/oauth/apple` | Apple Sign-In |
| POST | `/auth/forgot-password` | Password reset email |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/me` | Current user profile |
| PATCH | `/users/me` | Update profile |
| GET | `/users/me/groups` | List user's groups |

### Groups
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/groups` | Create group |
| GET | `/groups/{id}` | Get group details |
| PATCH | `/groups/{id}` | Update group (admin) |
| DELETE | `/groups/{id}` | Delete group |
| POST | `/groups/{id}/invite` | Generate invite link/QR |
| POST | `/groups/{id}/join` | Join via code/link |
| DELETE | `/groups/{id}/members/{userId}` | Remove member |
| GET | `/groups/{id}/photos` | List photos (paginated) |
| GET | `/groups/{id}/photos/feed` | SSE/WebSocket live feed |

### Photos
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/photos/upload` | Multipart upload (chunked) |
| GET | `/photos/{id}` | Get photo metadata + URL |
| DELETE | `/photos/{id}` | Delete (own photos) |
| POST | `/photos/{id}/approve` | Approve for sharing (if workflow enabled) |
| GET | `/photos/{id}/download` | Signed URL for full-res |

### Events (Phase 2+)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/groups/{id}/events` | Create event (date range) |
| GET | `/groups/{id}/events` | List events |
| PATCH | `/groups/{id}/events/{eventId}` | Update event |

---

## Data Models

### User
```kotlin
- id: UUID
- email: String
- displayName: String?
- avatarUrl: String?
- createdAt: Instant
- updatedAt: Instant
```

### Group
```kotlin
- id: UUID
- name: String
- coverPhotoId: UUID?
- createdById: UUID
- createdAt: Instant
- settings: GroupSettings (reviewWindowMinutes, requireApproval, etc.)
```

### GroupMember
```kotlin
- groupId: UUID
- userId: UUID
- role: ADMIN | MEMBER
- joinedAt: Instant
```

### Photo
```kotlin
- id: UUID
- groupId: UUID
- uploadedById: UUID
- storageKey: String  // S3 key
- thumbnailKey: String?
- originalFilename: String
- mimeType: String
- width: Int, height: Int
- fileSizeBytes: Long
- takenAt: Instant?   // EXIF
- location: GeoPoint? // EXIF
- status: PENDING | APPROVED | REJECTED  // if approval workflow
- createdAt: Instant
```

---

## Infrastructure Checklist

| Component | Service | Notes |
|-----------|---------|-------|
| Database | PostgreSQL 15+ | RDS or managed |
| Object Storage | S3 / MinIO | Bucket per env |
| Cache | Redis | Sessions, queues |
| CDN | CloudFront | For photo delivery |
| Auth | JWT + OAuth | No third-party auth service required for MVP |
| Push | FCM + APNs | Mobile notifications |
| Monitoring | Prometheus + Grafana | Or Datadog |

---

## Security Requirements

- [ ] HTTPS only
- [ ] JWT with short expiry (15 min) + refresh tokens
- [ ] Rate limiting on upload/auth endpoints
- [ ] Input validation (file type, size limits)
- [ ] S3 signed URLs (time-limited) for downloads
- [ ] No sensitive data in logs

---

## Mobile App Requirements

### iOS
- Camera roll read permission (photo library)
- Background upload (Background App Refresh)
- Offline queue with local SQLite/Realm
- Push notifications

### Android
- READ_MEDIA_IMAGES (Android 13+)
- WorkManager for background upload
- Foreground service for large batches
- Push via FCM
