# Image Gallery Feature

The **Image Gallery** feature allows users to upload, store, and manage images within the application. It provides a visual interface for browsing uploaded content with a modern, responsive design.

## Features
- **Secure Uploads**: Support for JPEG, PNG, and GIF images.
- **Size Limits**: Configured to handle files up to **10MB** (client and server-side).
- **Persistent Storage**: Images are saved to the local filesystem (`data/uploads/`) and metadata is stored in the database.
- **Interactive UI**: 
  - Real-time upload progress.
  - Hover effects on image cards.
  - Soft deletions (removes from both disk and database).
- **User Isolation**: Users only see and manage their own uploaded images.

## Technical Architecture

### 1. Data Model (`GalleryItem.java`)
Represents an image in the database.
- `fileName`: Original name of the uploaded file.
- `contentType`: MIME type (e.g., `image/jpeg`).
- `filePath`: Absolute path on the server where the file is stored.
- `uploadTime`: Timestamp of the upload.
- `owner`: Relationship to the `User` who uploaded it.

### 2. Service Layer (`GalleryService.java`)
Manages the business logic and filesystem operations.
- `saveGalleryItem()`: Generates a unique UUID for the filename to prevent collisions, saves the file to disk, and persists metadata.
- `getGalleryItems()`: Retrieves items for the current authenticated user.
- `deleteGalleryItem()`: Ensures the physical file is deleted before removing the database record.

### 3. Frontend (`GalleryView.java`)
Built with Vaadin components for a seamless Java-to-Web experience.
- Uses `MemoryBuffer` and `Upload` for efficient file handling.
- `StreamResource` is used to serve images directly from the filesystem to the browser.
- Responsive `FlexLayout` container for the gallery grid.

## Configuration & Performance

### File Size Limits
To allow the 10MB limit requested in the UI, the following settings are configured in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
server.tomcat.max-http-post-size=15MB
vaadin.maxPostSize=15728640
```

### Storage Location
Images are stored in:
`[project-root]/data/uploads/`

## Troubleshooting
If you encounter a **"413 Payload Too Large"** error:
1. Ensure the `application.properties` settings match your requirements.
2. Restart the application to apply multipart configuration changes.
3. Check the browser console for specific Vaadin error messages.
