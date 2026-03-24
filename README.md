#  Image Upload & Gallery — Vaadin + Spring Boot

A beautiful, full-featured **Image Upload & Gallery** web application built with **Java 21**, **Spring Boot 3.3**, and **Vaadin 24**. Users can register, log in, upload images (drag-and-drop or click-to-browse), view them in a stunning dark-themed masonry gallery, add descriptions, expand to full-size, and delete — all scoped per-user with Spring Security.

---

##  Features

| Feature | Description |
|---|---|
| **User Registration & Login** | Secure account creation with password hashing (BCrypt). Glassmorphism-styled auth pages. |
| **Drag & Drop Upload** | Upload up to 10 images at once (PNG, JPG, GIF, WebP). Max 10 MB per file. |
| **Persistent Gallery** | Images are stored in an H2 file-based database and survive server restarts. |
| **Responsive Grid Layout** | 3-column masonry-style gallery with hover animations and action overlays. |
| **Full-Size Preview** | Click any image to open a large preview dialog with metadata display. |
| **Image Descriptions** | Add or edit descriptions on any image via the preview dialog. |
| **Delete with Confirmation** | Delete images with a confirmation dialog to prevent accidental removal. |
| **Per-User Isolation** | Each user sees only their own images. Enforced at the service layer. |
| **Dark Theme UI** | Premium dark gradient background with glassmorphism cards and micro-animations. |

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Downloading the Project](#downloading-the-project)
3. [How to Run the Project](#how-to-run-the-project)
4. [Using the Application](#using-the-application)
   - [Register a New Account](#1-register-a-new-account)
   - [Log In](#2-log-in)
   - [Upload Images](#3-upload-images)
   - [Browse the Gallery](#4-browse-the-gallery)
   - [View Full-Size Image](#5-view-full-size-image)
   - [Add / Edit Description](#6-add--edit-description)
   - [Delete an Image](#7-delete-an-image)
   - [Logout](#8-logout)
5. [Project Architecture](#project-architecture)
6. [Detailed Project Structure](#detailed-project-structure)
7. [Configuration Reference](#configuration-reference)
8. [Key Technologies](#key-technologies)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before you can run this project, you need **Java 21** installed.

### Install Java 21 (JDK)

| Platform | Instructions |
|---|---|
| **Windows** | Download the `.msi` installer from [Adoptium](https://adoptium.net/temurin/releases/?version=21). During install, enable **"Set JAVA_HOME"** and **"Add to PATH"**. |
| **macOS** | Download the `.pkg` from [Adoptium](https://adoptium.net/temurin/releases/?version=21), or use Homebrew: `brew install --cask temurin@21` |
| **Linux** | Use your package manager, e.g. `sudo apt install temurin-21-jdk` or download from Adoptium. |

**Verify Installation:**
```bash
java -version
# Expected output: openjdk version "21.x.x" ...
```

> **Note:** You do **not** need to install Maven. This project includes a Maven Wrapper (`mvnw`) that handles everything automatically.

---

## Downloading the Project

### Option 1: Download as ZIP (Easiest)
1. Go to the project's GitHub page.
2. Click the green **Code** button → **Download ZIP**.
3. Extract to a folder (e.g., `~/Desktop/Image_Upload/vaadin-example-project`).

### Option 2: Clone with Git
```bash
git clone <REPOSITORY_URL>
cd vaadin-example-project
```

### Option 3: Fork & Clone (for contributing)
1. Click **Fork** on GitHub to create your own copy.
2. Clone your fork:
   ```bash
   git clone https://github.com/<YOUR_USERNAME>/vaadin-example-project.git
   cd vaadin-example-project
   ```

---

## How to Run the Project

1. **Open a terminal** and navigate to the project folder:
   ```bash
   cd path/to/vaadin-example-project
   ```

2. **Start the application:**

   - **macOS / Linux:**
     ```bash
     ./mvnw spring-boot:run
     ```
   - **Windows:**
     ```cmd
     .\mvnw.cmd spring-boot:run
     ```

3. **Wait for startup** — you'll see log output ending with:
   ```
   Started NotesApplication in X.XX seconds
   ```

4. **Open your browser** and navigate to:

   **👉 [http://localhost:8080](http://localhost:8080)**

5. To **stop** the server, press `Ctrl + C` in the terminal.

---

## Using the Application

### 1. Register a New Account

When you first visit `http://localhost:8080`, you'll be redirected to the **Login** page.

- Click **"Create Account"** at the bottom of the login card.
- Fill in the registration form:
  - **Username** — choose a unique username (min. 3 characters)
  - **Password** — create a strong password (min. 6 characters)
  - **Confirm Password** — re-enter the same password
- Click **"Create Account"**.
- On success, you'll see a green notification and be redirected to the login page.

> **Tip:** Usernames must be unique. If you choose one that's already taken, you'll see an error notification.

### 2. Log In

- Enter your **username** and **password** on the login page.
- Click **Sign in**.
- You will be redirected to the **Gallery** (main page).

### 3. Upload Images

The upload section is at the top of the gallery page, inside a card with a dashed border.

**Method A — Click to Browse:**
1. Click the **"Choose Files"** button (purple gradient button).
2. Select one or more image files from your computer.
3. Accepted formats: `PNG`, `JPG/JPEG`, `GIF`, `WebP`.
4. Maximum file size: **10 MB** per file.
5. You can upload up to **10 files** at once.

**Method B — Drag and Drop:**
1. Drag image files from your file manager.
2. Drop them onto the upload area (the dashed-border zone).
3. Files will upload automatically.

After each successful upload, you'll see a  **green notification** and the gallery will refresh instantly.

### 4. Browse the Gallery

- Below the upload section, you'll find **"Your Gallery"** with an image count badge.
- Images are displayed in a responsive **3-column grid**.
- Each card shows:
  - **Thumbnail** — the image in a square crop, fitted to fill the card.
  - **File name** — original file name (truncated if too long).
  - **File size** — displayed in human-readable format (KB/MB).
  - **Upload date** — formatted as `MMM dd, yyyy`.
  - **Description** — shown in italics if one has been added.

**Hover Effects:**
- Hovering over a card lifts it with a subtle purple glow shadow.
- Hovering over the image area reveals an **overlay** with two action buttons:
  - 🔲 **Expand** — opens the full-size preview dialog.
  - 🗑️ **Delete** — opens delete confirmation.

### 5. View Full-Size Image

Click on any gallery card (or the expand button) to open the **Image Preview Dialog**:

- **Header** — shows the file name, file size, and exact upload timestamp.
- **Image** — displayed at full resolution, scaled to fit the viewport (max 70% viewport height).
- **Description field** — text input at the bottom to add or edit a description.
- **Save button** — saves the description.
- **Delete button** — opens delete confirmation.
- **Close** — click the ✕ button or press `Escape` to close.

### 6. Add / Edit Description

1. Open the full-size preview of any image.
2. In the **text field** at the bottom, type a description (e.g., "Sunset at the beach").
3. Click **"Save"**.
4. A  green notification confirms the description was saved.
5. The description appears on the gallery card in *italics*.

### 7. Delete an Image

**From the gallery card:**
1. Hover over the image thumbnail.
2. Click the **red trash icon** (🗑️) in the overlay.

**From the preview dialog:**
1. Click the **"Delete"** button at the bottom right.

In both cases, a **confirmation dialog** appears:
- Click **"Delete"** to confirm.
- Click **"Cancel"** to abort.

After deletion, the gallery refreshes and the image count updates.

### 8. Logout

Click the **"Logout"** text in the top-right corner of the header. You'll be redirected to the login page.

---

## Project Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                        BROWSER (Vaadin UI)                        │
│  ┌──────────────┐  ┌──────────────────┐  ┌────────────────────┐  │
│  │  LoginView   │  │  RegisterView    │  │  ImageUploadView   │  │
│  │  (login)     │  │  (register)      │  │  (root: "")        │  │
│  └──────┬───────┘  └────────┬─────────┘  └─────────┬──────────┘  │
└─────────┼───────────────────┼──────────────────────┼─────────────┘
          │                   │                      │
          ▼                   ▼                      ▼
┌───────────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER (Spring)                         │
│  ┌────────────────┐  ┌────────────────┐  ┌─────────────────────┐ │
│  │  UserService   │  │  ImageService  │  │  NoteService        │ │
│  │  (register)    │  │  (CRUD images) │  │  (CRUD notes)       │ │
│  └────────┬───────┘  └────────┬───────┘  └─────────┬───────────┘ │
└───────────┼───────────────────┼────────────────────┼─────────────┘
            │                   │                    │
            ▼                   ▼                    ▼
┌───────────────────────────────────────────────────────────────────┐
│                     DATA LAYER (Spring Data JPA)                  │
│  ┌────────────────┐  ┌─────────────────┐  ┌─────────────────┐   │
│  │ UserRepository │  │ ImageRepository │  │ NoteRepository  │   │
│  └────────┬───────┘  └────────┬────────┘  └────────┬────────┘   │
└───────────┼───────────────────┼─────────────────────┼────────────┘
            │                   │                     │
            ▼                   ▼                     ▼
┌───────────────────────────────────────────────────────────────────┐
│                      H2 DATABASE (File-based)                     │
│          ./data/notesdb.mv.db                                     │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────────────────┐  │
│  │ APPLICATION_ │  │ IMAGE_METADATA │  │       NOTE           │  │
│  │ USER         │  │ (BLOBs)        │  │                      │  │
│  └──────────────┘  └────────────────┘  └──────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
```

### Security Flow

```
Request → Spring Security Filter Chain
  ├── /login, /register  → AnonymousAllowed (public)
  ├── /logout            → Invalidates session
  └── / (gallery)        → @PermitAll (requires authentication)
                           ↓
                         VaadinWebSecurity → LoginView redirect
```

---

## Detailed Project Structure

```text
vaadin-example-project/
│
├── pom.xml                          # Maven build config & dependency management
├── mvnw / mvnw.cmd                  # Maven Wrapper (no manual Maven install needed)
│
├── src/main/java/com/example/notes/
│   │
│   ├── NotesApplication.java        # Spring Boot entry point (@SpringBootApplication)
│   │
│   ├── data/entity/
│   │   ├── User.java                # User entity (id, username, passwordHash)
│   │   ├── Note.java                # Note entity (id, content, user)
│   │   └── ImageMetadata.java       # ★ Image entity (id, fileName, contentType,
│   │                                #   fileSize, description, imageData BLOB,
│   │                                #   user, uploadedAt)
│   │
│   ├── data/repository/
│   │   ├── UserRepository.java      # JPA repository for User
│   │   ├── NoteRepository.java      # JPA repository for Note
│   │   └── ImageRepository.java     # ★ JPA repository for ImageMetadata
│   │                                #   - findByUserOrderByUploadedAtDesc()
│   │                                #   - countByUser()
│   │
│   ├── service/
│   │   ├── UserService.java         # User registration with BCrypt hashing
│   │   ├── NoteService.java         # Note CRUD operations
│   │   └── ImageService.java        # ★ Image CRUD operations:
│   │                                #   - saveImage(), getUserImages()
│   │                                #   - getImage(), deleteImage()
│   │                                #   - updateDescription()
│   │                                #   - getImageCount()
│   │                                #   All operations scoped to current user
│   │
│   ├── security/
│   │   ├── SecurityConfig.java      # Spring Security config (VaadinWebSecurity)
│   │   ├── LoginView.java           # ★ Glassmorphism login page
│   │   └── UserDetailsServiceImpl.java  # Loads users from DB for auth
│   │
│   └── views/
│       ├── ImageUploadView.java     # ★ Main gallery page (route: "")
│       │                            #   - Header with logo & logout
│       │                            #   - Drag & drop upload zone
│       │                            #   - Stats bar with image count
│       │                            #   - 3-column gallery grid
│       │                            #   - Full-size preview dialog
│       │                            #   - Delete confirmation dialog
│       │
│       └── RegisterView.java        # ★ Account creation page (route: "register")
│
├── src/main/resources/
│   └── application.properties       # Database, upload limits, Vaadin config
│
├── src/test/java/
│   └── NotesApplicationTests.java   # Spring Boot context load test
│
├── data/
│   └── notesdb.mv.db               # H2 database file (auto-created on first run)
│
└── target/                          # Compiled output (auto-generated)
```

> Files marked with **★** are the new Image Upload & Gallery feature additions.

---

## Configuration Reference

All configuration is in `src/main/resources/application.properties`:

| Property | Default Value | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/notesdb` | H2 database file location |
| `spring.datasource.username` | `sa` | Database username |
| `spring.datasource.password` | *(empty)* | Database password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-creates/updates tables on startup |
| `spring.h2.console.enabled` | `true` | Enables H2 web console at `/h2-console` |
| `spring.servlet.multipart.max-file-size` | `10MB` | Max upload size per file |
| `spring.servlet.multipart.max-request-size` | `50MB` | Max total request size |

---

## Key Technologies

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.3.4 | Backend framework, auto-configuration, embedded Tomcat |
| **Vaadin** | 24.4.4 | Full-stack web UI framework (Java → HTML/JS automatically) |
| **Spring Security** | 6.x | Authentication & authorization with BCrypt password hashing |
| **Spring Data JPA** | 3.x | Database access via repository pattern |
| **H2 Database** | 2.x | Lightweight, file-based SQL database (zero config) |
| **Hibernate** | 6.x | ORM for mapping Java entities to database tables |

---

## Troubleshooting

### `The JAVA_HOME environment variable is not defined correctly`
**Cause:** Java 21 is not installed, or the system doesn't know where it is.
**Fix:**
- Verify Java is installed: `java -version`
- Set `JAVA_HOME` to your JDK installation directory (e.g., `C:\Program Files\Java\jdk-21`).
- **Restart your terminal** after setting environment variables.

### `Port 8080 was already in use`
**Cause:** Another app or a previous instance is using port 8080.
**Fix:**
- Stop any running instances (`Ctrl+C`).
- Or add `server.port=8081` to `application.properties` to use a different port.

### Changes don't appear in the browser
**Cause:** Browser caching.
**Fix:** Hard-refresh with `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac). If that fails, restart the server.

### Upload fails with "file too large"
**Cause:** The file exceeds the 10 MB limit.
**Fix:** Compress the image or increase `spring.servlet.multipart.max-file-size` in `application.properties`.

### Images disappear after restarting
**Cause:** The `./data/notesdb.mv.db` file was deleted.
**Fix:** The database file at `./data/notesdb.mv.db` stores all images. Don't delete it unless you want a fresh start.

---

## License

This project is provided for educational purposes.

---

*Built with  using Vaadin + Spring Boot*
