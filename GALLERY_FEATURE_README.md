# Vaadin Image Gallery Feature

This document summarizes the recent architectural and UI updates made to the Vaadin Notes Application.

## 🌟 New Features & Capabilities

### 1. Robust Image Persistence

- **Database Integration**: Images are securely uploaded and stored directly in the H2 database via a newly created `ImageEntity`, `ImageRepository`, and `ImageService`.
- **Large File Support**: Configured the servlet limits in `application.properties` to seamlessly process and accept high-quality file uploads up to `10MB`.

### 2. Premium UI/UX Design

- **Componentized Architecture**: The gallery logic is cleanly split into reusable Vaadin Flow components (`ImageCardComponent` and `ImageUploadComponent`) for enhanced maintainability.
- **Responsive CSS Grid**: The traditional linear layout was replaced with a native auto-filling CSS Grid wrapper, allowing the entire gallery to fluidly adapt to desktop, tablet, and mobile screens.
- **Embedded Upload Box**: The custom "Upload Images / Drop Files Here" component acts as the very first card neatly tucked inside the gallery grid itself, maximizing layout consistency.

### 3. Multi-Select Bulk Deletion

- **Interactive Selection Mode**: Clicking any image instantly toggles its "selected" state. The UI responds by highlighting the image card with a primary colored border and a checkmark overlay.
- **Contextual Actions**: Visual clutter was reduced by removing individual repetitive delete buttons. Instead, selecting one or multiple images smoothly slides in a "Delete Selected (X)" bulk-action button inside your gallery header.

### 4. Soothing Global Styling

- **Color Palette**: The application features a highly polished modern aesthetic. The main navigation drawer utilizes an elegant, deep Indigo (`#312e81`), contrasting beautifully against crisp white buttons and a gentle Slate (`#f8fafc`) global application background.
- **Micro-interactions**: Subtle hover state transitions, lift effects, borders, and shadows have been added to the buttons, side menu links, and image cards to yield a premium user feel.
- **Robust Error Handling**: Real-time Vaadin Notifications are fired smoothly on any `FileRejectedEvent` (e.g., if a file is too large or the wrong type), keeping users perfectly informed without throwing system crashes or failing silently.

## 🚀 How to Test

Trigger your Spring Boot application locally via terminal:

```shell
./mvnw spring-boot:run
```

Once initialized, log in with your credentials and click the brand new **"Gallery"** tab from your stylish left-hand navigation menu!
