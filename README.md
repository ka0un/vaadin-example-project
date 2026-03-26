## Feature: Image Upload

This PR introduces an Image Upload feature that allows users to upload, preview, and manage images within the application using a clean and component-based design.

---

## ✨ Features Implemented

### 📤 Image Upload
- Drag & drop or click-to-upload functionality using Vaadin Upload
- Supports common image formats (JPEG, PNG, GIF, WEBP)
- File size validation (max 5MB per file)

### 👀 Preview Before Save
- Images are first stored temporarily in memory
- Users can preview images before saving to the database

### 💾 Batch Save
- Images are only persisted when the user clicks "Save Images"
- Supports saving multiple images in one action
- Uses a service-layer method (`saveAll`) for efficient batch operations

### 🖼️ Image Gallery
- Displays uploaded images in a responsive grid layout
- Clean card-based UI with hover effects
- Images are loaded per user

### ✅ Selection & Batch Delete
- Users can select multiple images
- "Select All / Deselect All" functionality
- Batch deletion using image IDs
- Optimized to avoid unnecessary database calls

---

## 🧱 Architecture & Design

- **Component-based UI**
    - `ImageUploadComponent` → handles upload + preview logic
    - `ImageGalleryToolbar` → selection & batch actions
    - `ImageCardComponent` → individual image rendering
    - `ImageView` → orchestrates state and layout

- **Separation of concerns**
    - UI layer (Vaadin views/components)
    - Service layer (business logic)
    - Data layer (JPA repositories)

- **Optimized state management**
    - Uses `Set<Long>` for selection instead of entity objects
    - Avoids redundant database queries (e.g., select-all uses in-memory state)

---

## ⚡ Technical Highlights

- Efficient batch operations (`saveAll`, `deleteImagesByIds`)
- No unnecessary API/database calls during selection or UI updates
- Clean and readable code with meaningful naming
- Responsive layout using CSS and Flexbox
- Graceful error handling for invalid uploads

---

## 📝 Notes & Limitations

- Images are stored as BLOBs in the H2 database (for simplicity)
- Duplicate filenames are not uniquely handled during pending removal

---

## 🚀 How to Use

1. Navigate to **My Images**
2. Drag & drop or select image files
3. Preview images
4. Click **Save Images** to store them
5. Select images and use the toolbar to delete if needed

---

