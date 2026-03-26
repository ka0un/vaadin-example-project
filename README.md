---

## 🖼️ Image Gallery Feature

### Overview
The Image Gallery feature allows authenticated users to upload, view, and delete images within the application. Each user can only see and manage their own images privately.

### How to Navigate to the Gallery
1. Log in to the application
2. Click the **☰ menu icon** on the top left
3. Click **"Image Gallery"** in the side drawer

---

### How to Upload an Image
1. Click the **"Upload File..."** button or drag and drop an image into the upload area
2. Supported formats: **JPG, PNG, GIF, WEBP**
3. Maximum file size: **5MB**
4. Once uploaded, the image will instantly appear in the gallery below

---

### How to View an Image
1. Click on any image card in the gallery
2. A full screen dialog will open showing the image in full size
3. Click **"✕ Close"** or click outside the dialog to close it

---

### How to Delete an Image
1. Hover over any image card
2. Click the **red trash icon** 🗑️ at the bottom right of the card
3. The image will be permanently removed from both the gallery and the server

---

### Feature Highlights
- 🔒 **Private gallery** — each user only sees their own images
- 📱 **Responsive layout** — adapts to all screen sizes automatically
- 🖼️ **Full screen viewer** — click any image to view it in full size
- ⚠️ **Smart validation** — invalid file types and oversized files are automatically rejected
- 💬 **Notifications** — success and error messages keep the user informed
- ✨ **Smooth animations** — hover effects on image cards
- 📅 **Upload date** — each card shows when the image was uploaded
- 🗂️ **Newest first** — images are sorted by most recently uploaded

---

### Technical Implementation

| File | Location | Purpose |
|---|---|---|
| `ImageEntity.java` | `data/entity/` | Database table for image metadata |
| `ImageRepository.java` | `data/repository/` | Database operations for images |
| `ImageService.java` | `service/` | File saving, loading, and deletion logic |
| `ImageGalleryView.java` | `views/` | Main gallery UI page |
| `ImageCard.java` | `views/components/` | Reusable card component for each image |



### Notes
- Uploaded images are physically stored in the `uploads/` folder in the project root
- Image metadata (filename, path, upload date) is stored in the H2 database
- The `uploads/` folder is created automatically on first run