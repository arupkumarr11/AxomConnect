# AxomConnect (অসমকানেক্ট)

A secure, mobile-first social media application built for the Assamese and Indian community worldwide, celebrating heritage, festivals, literature, and culture with zero-trust cloud persistence.

---

## 🌟 Key Features

### 1. Cultural Identity & Authentication
- **Google Sign-In**: Integrated via Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) with SHA-1/SHA-256 fingerprint verification.
- **Assamese & English Bilingual UI**: Native Assamese typography and terminology paired with English navigation labels.
- **Hero Landscape Banner**: Rich scenic banner featuring the Brahmaputra River, Bihu dancers, tea estates, Jaapi, Pepa, and Dhol.
- **Custom Adaptive Launcher Icon**: High-contrast icon featuring the golden One-Horned Rhinoceros motif and Gamusa weave on `#1E3A8A` Assam River Blue.

### 2. Social Feed & Interaction
- **Create Posts**: Share text updates, cultural stories, and photography.
- **Cultural Category Tags**: `#Bihu`, `#AssamCulture`, `#AssamFood`, `#Nature`, `#Kaziranga`, `#Majuli`, `#Music`, `#Community`.
- **Media Attachments**: Supports zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`), image URLs, and curated scenic presets.
- **Real-Time Likes & Comments**: Instant like count updates, heart animations, and modal comment bottom sheet.
- **Share & Moderation**: Native Android share sheet and community report submissions.

### 3. Explore & Discovery
- Trending Assamese cultural topics and search filtering.
- Filter feed by category tags or search keywords across posts and creators.

### 4. Profiles & Privacy
- Customizable profile with display name, `@username`, location (e.g., Guwahati, Jorhat, Dibrugarh), cultural bio, and interests.
- Personal posts feed with deletion capability.
- Sign out with state clearance.

### 5. Admin & Content Moderation
- **Security-Enforced Admin Role**: Role verification checked against `/admins/{uid}` in Cloud Firestore and verified administrator email. Clients cannot self-assign admin roles.
- **Moderation Panel**: Review flagged user reports, dismiss reports, or take down non-compliant posts.
- **Provisioning Guide**: Instructions for project owners to safely provision additional moderators.

---

## 🔒 Zero-Trust Security Architecture (`firestore.rules`)

AxomConnect enforces the **8 Pillars of Hardened Security Rules**:
- **Authentication Mandate**: All reads and writes enforce `request.auth != null`.
- **Resource Ownership**: Users can only create profiles matching their own UID (`request.auth.uid == userId`) and edit/delete their own posts.
- **Strict Schema Validation**: Standalone helpers (`isValidUserProfile`, `isValidPost`, `isValidComment`, `isValidLike`, `isValidReport`) validate field types, string boundaries, and timestamps.
- **Server Timestamps**: Writes enforce `is timestamp && <= request.time`.
- **Atomic Counts**: Like and comment counters only permit +/- 1 modifications with immutable authorship.
- **Testing**: Validated with both Tier 1 JavaScript unit tests (`firestore.test.js`) and Tier 2 Robolectric native tests (`AxomRepositoryRuleTest.kt`).
