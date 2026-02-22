# FriendLens — Product Roadmap & Vision

> **"Capture once, share everywhere — zero effort group photo sync for trips & events."**

---

##  Executive Summary

**FriendLens** is an automatic group photo-syncing app that solves the pain of collecting vacation/event photos from multiple friends' phones. No manual sending, no lost memories, no compression — just take photos and they sync to everyone in the group automatically.

---

##  Vision Statement

> *"We build the world's first automatic group photo ecosystem for trips & events — effortless sync, full-quality storage, and crystal-clear privacy. Every memory, from every phone, in one place."*

---

##  Competitor Landscape

| Competitor | Strengths | Weaknesses / Gaps |
|------------|-----------|-------------------|
| **Sharezie** | Auto sync, review period (5 min–5 days), full-res download | iOS-only, last update 2021, no Android |
| **Kwikpic** | AI face recognition, professional/event focus, cross-platform | Manual upload, not truly automatic, complex for casual trips |
| **TripMemo** | Real-time sync, offline mode, day-by-day story, map view | Newer player, limited awareness |
| **Airbum** | Auto-collect, offline sync, QR invite, travel-focused | Limited info on privacy controls |
| **Google Photos** | Ubiquitous, good AI | No true group auto-sync, partner sharing only, quality issues |
| **WhatsApp / iMessage** | Everyone has it | Heavy compression, no organization, manual only |
| **Kululu** | No app needed (web/QR) | Basic features, weak privacy |

### Competitor Gaps We Can Own

1. **Cross-platform from day one** — Sharezie is iOS-only; we target Android + iOS + Web
2. **Location-aware sync** — Only sync photos when group members are co-located (privacy)
3. **Time-bounded events** — Auto-archive after trip ends; no permanent clutter
4. **Original quality by default** — No compression unless user opts in
5. **Open API** — Let developers integrate (e.g., travel apps, printing services)
6. **Trip timeline + AI highlights** — Auto-generated "best of" reels, not just raw dump

---

##  Deep Pain Points (Validated)

### From User Reviews & Forums

| Pain Point | Severity | Our Solution |
|------------|----------|--------------|
| "Photos uploaded 25+ times before they appear" | High | Reliable background sync with retry + conflict resolution |
| "Locked out of account, no password reset" | High | Auth with email/Social + proper recovery flows |
| "Group chat = 300+ messages to find one pic" | High | Media-only shared space, no chat clutter |
| "Friends forget to send photos" | Critical | Automatic sync — no reminders needed |
| "Compression kills quality" | High | Original resolution by default |
| "Manual sending is tedious" | Critical | Zero manual steps |
| "Hard to find my friend's best shots" | Medium | AI tagging by person, event, location |
| "Privacy — who sees what?" | High | Explicit permissions, time-limited sharing, selective sync |
| "Works offline?" | Medium | Offline-first; sync when connected |
| "Too many photos, no structure" | Medium | Smart albums: by day, person, location, highlights |

### Use Cases

-  **Group vacations** — Beach trips, road trips, international travel
-  **Events** — Weddings, bachelor/bachelorette, reunions, festivals
-  **Adventures** — Hiking, camping, skiing — often offline
-  **Family gatherings** — Holidays, reunions

---

##  Feature Checklist

### Phase 1 — MVP (Months 1–3)

| # | Feature | Description | Priority |
|---|---------|-------------|----------|
| 1 | **Group creation** | Create group, name, cover image | P0 |
| 2 | **Invite flow** | QR code + shareable link | P0 |
| 3 | **Member management** | Add/remove members, roles (admin/member) | P0 |
| 4 | **Auto upload** | Background upload on photo capture (camera roll / in-app) | P0 |
| 5 | **Shared album view** | Timeline of all group photos | P0 |
| 6 | **Full-res download** | Save to device at original quality | P0 |
| 7 | **Basic auth** | Email/password + optional Google/Apple sign-in | P0 |
| 8 | **Cloud storage** | S3/equivalent for originals + thumbnails | P0 |

### Phase 2 — Enhanced UX (Months 4–6)

| # | Feature | Description | Priority |
|---|---------|-------------|----------|
| 9 | **Offline sync** | Queue photos when offline, sync when online | P0 |
| 10 | **Review before share** | Optional X-minute window to deselect photos | P1 |
| 11 | **Sort by time/date** | Day, week, event view | P1 |
| 12 | **Selective sync** | Only photos taken during "event" window | P1 |
| 13 | **Push notifications** | "New photos in [Trip Name]" | P1 |
| 14 | **Bulk download** | One-tap download all as ZIP | P1 |
| 15 | **Face detection (basic)** | Auto-tag people for filtering | P2 |

### Phase 3 — Smart & Privacy (Months 7–9)

| # | Feature | Description | Priority |
|---|---------|-------------|----------|
| 16 | **Location-aware sync** | Only sync when near other group members | P1 |
| 17 | **Time-bounded sharing** | Auto-archive album after 30/60/90 days | P1 |
| 18 | **Privacy controls** | Per-photo visibility, approval workflows, audience roles | P1 |
| 19 | **AI highlights & Storytelling** | Auto "best of" selection, collage, dynamic short reel | P2 |
| 20 | **Map view & Dynamic Sub-Albums** | Pin photos, auto-categorize by hyper-local zones | P2 |
| 21 | **Export to Cloud** | Optional integration to Google Photos / iCloud | P2 |
| 22 | **AI Photo Culling** | Auto-detect and flag blurry/duplicate photos for removal | P2 |

### Phase 4 — Growth & Monetization (Months 10–12)

| # | Feature | Description | Priority |
|---|---------|-------------|----------|
| 22 | **Freemium tiers** | Free: 5GB/group; Paid: more storage, AI features | P1 |
| 23 | **Event pass** | One-time premium for weddings/large events | P1 |
| 24 | **Photo printing & Keepsakes** | Order physical prints/merch seamlessly from album | P2 |
| 25 | **Public share links** | Time-limited view-only links | P2 |
| 26 | **Event Gamification** | Photo scavenger hunts & interactive prompts for attendees | P2 |
| 27 | **Content Licensing Marketplace** | Allow hosts to legally use attendee content (B2B) | P3 |
| 28 | **API for developers** | Open API for integrations | P3 |

---

##  Tech Stack

### Current (FriendLens Backend)

| Layer | Technology |
|-------|------------|
| **Runtime** | JVM 21 |
| **Framework** | Ktor 3.4 |
| **Language** | Kotlin 2.3 |
| **Server** | Netty |
| **Config** | YAML |
| **Auth** | Ktor Auth (basic) |
| **Caching** | SimpleCache (Memory/Redis) |
| **API Docs** | OpenAPI + Swagger UI |
| **Async API** | Kotlin AsyncAPI |

### Recommended Additions

| Component | Technology | Notes |
|-----------|------------|-------|
| **Database** | PostgreSQL + Exposed / JPA | Users, groups, metadata |
| **Object storage** | AWS S3 / MinIO | Photo originals + thumbnails |
| **Queue** | Redis / RabbitMQ / SQS | Async upload processing |
| **Real-time** | WebSockets / Server-Sent Events | Live photo feed updates |
| **Mobile (iOS)** | Swift / SwiftUI | Native or Kotlin Multiplatform |
| **Mobile (Android)** | Kotlin + Jetpack Compose | Native or KMP |
| **Image processing** | ImageMagick / Sharp / LibVips | Thumbnails, resizing |
| **Auth** | JWT + OAuth2 (Google, Apple) | Industry standard |
| **CDN** | CloudFront / Cloudflare | Fast image delivery |

### Architecture (High-Level)

```
┌─────────────┐     ┌─────────────┐     ┌──────────────────┐
│  Mobile App │────│  Ktor API   │────│  PostgreSQL      │
│  (iOS/Andr) │     │  (REST/WS)  │     │  (users, groups) │
└─────────────┘     └──────┬──────┘     └──────────────────┘
                           │
                           ├────────────  S3 / MinIO (photos)
                           ├────────────  Redis (cache, queue)
                           └────────────  Push (FCM/APNs)
```

---

##  Roadmap Timeline

```
Q1 2025                    Q2 2025                    Q3 2025                    Q4 2025
├──────────────────────────┼──────────────────────────┼──────────────────────────┤
│ MVP                      │ Enhanced UX              │ Smart & Privacy          │ Growth
│ • Auth                   │ • Offline sync           │ • Location-aware         │ • Freemium
│ • Groups + invites       │ • Review window          │ • Time-bounded           │ • Event pass
│ • Auto upload            │ • Sort/filter            │ • AI highlights          │ • Printing
│ • Shared album           │ • Push notifications     │ • Map view               │ • Public links
│ • Cloud storage          │ • Bulk download          │ • Export                 │ • API
└──────────────────────────┴──────────────────────────┴──────────────────────────┴──────────────────┘
```

---

##  Unique Differentiators (Competitors Don't Do Well)

| Differentiator | Why It Matters |
|----------------|----------------|
| **Location-aware sync** | Only sync when you're actually with the group — no random brunch pics from 6 months ago |
| **Event time window** | Define "Trip: Mar 1–7" — only photos from that period go to the album |
| **Cross-platform from day one** | iOS + Android + Web; no "wait for Android" |
| **Original quality default** | No hidden compression; users get what they took |
| **Open API** | Travel apps, printing, framing — ecosystem plays |
| **Facial approval** | "Approve photos of me before they're shared" — privacy-first |
| **Auto archive** | Album expires after trip; reduces clutter and privacy surface |
| **AI-driven Culling & Highlights** | Automatically filters out blurry/bad shots and creates dynamic storytelling reels instantly |
| **Dynamic Sub-Albums** | Auto-categorizes photos by hyper-local zones (e.g., "Main Stage", "Dinner") as they happen |
| **Event Gamification** | Built-in photo scavenger hunts (e.g. "Take a pic with the bride") to boost event engagement |
| **Granular Audience Roles** | "VIP" vs "General" permissions to ensure corporate or large event privacy perfectly |
| **Content Licensing (B2B)** | Transparent Opt-in process for event organizers to acquire and utilize generated content |

---

##  Success Metrics

| Metric | Target (Year 1) |
|--------|-----------------|
| DAU | 10K |
| Groups created | 50K |
| Photos synced | 1M |
| Avg photos per group | 200 |
| Retention (D30) | 25% |
| NPS | 40+ |

---

##  Monetization Strategy

1. **Freemium** — Free: 1–2 groups, 5GB total; Paid: unlimited groups, 50GB+
2. **Event pass** — One-time $5–15 for weddings, large events
3. **Partnerships** — Photo printing, travel brands, cloud storage upsell

---

##  Next Steps (Immediate)

- [ ] Finalize data models (User, Group, Photo, Event)
- [ ] Set up PostgreSQL + Exposed
- [ ] Implement auth (JWT + OAuth)
- [ ] Design upload API (multipart, chunked)
- [ ] Integrate S3/MinIO for storage
- [ ] Create mobile app scaffold (or start with Web PWA)
- [ ] Define OpenAPI spec for all endpoints

---

*Last updated: February 2025*
