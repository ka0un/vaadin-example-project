    package com.example.notes.views;

    import com.example.notes.data.entity.User;
    import com.example.notes.data.entity.UserImage;
    import com.example.notes.data.repository.UserRepository;
    import com.example.notes.service.ImageService;
    import com.vaadin.flow.component.button.Button;
    import com.vaadin.flow.component.button.ButtonVariant;
    import com.vaadin.flow.component.html.H3;
    import com.vaadin.flow.component.html.Image;
    import com.vaadin.flow.component.html.Paragraph;
    import com.vaadin.flow.component.notification.Notification;
    import com.vaadin.flow.component.notification.NotificationVariant;
    import com.vaadin.flow.component.orderedlayout.FlexLayout;
    import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
    import com.vaadin.flow.component.orderedlayout.VerticalLayout;
    import com.vaadin.flow.component.orderedlayout.Scroller;
    import com.vaadin.flow.component.upload.Upload;
    import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
    import com.vaadin.flow.router.PageTitle;
    import com.vaadin.flow.router.Route;
    import com.vaadin.flow.server.StreamResource;
    import com.vaadin.flow.spring.security.AuthenticationContext;
    import jakarta.annotation.security.PermitAll;
    import org.springframework.security.core.userdetails.UserDetails;

    import java.io.ByteArrayInputStream;
    import java.io.IOException;
    import java.util.Locale;

    @Route(value = "gallery", layout = MainLayout.class)
    @PageTitle("Gallery | Vaadin Notes App")
    @PermitAll
    public class GalleryView extends VerticalLayout {

        private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
        private static final String[] ACCEPTED_IMAGE_TYPES = {
                ".png", ".jpg", ".jpeg", ".gif", ".webp",
                "image/png", "image/jpeg", "image/gif", "image/webp"
        };

        private final ImageService imageService;
        private final User currentUser;
        private final FlexLayout galleryLayout = new FlexLayout();
        private final Scroller galleryScroller = new Scroller(galleryLayout);

        public GalleryView(ImageService imageService, UserRepository userRepository, AuthenticationContext authContext) {
            this.imageService = imageService;

            String username = authContext.getAuthenticatedUser(UserDetails.class)
                    .map(UserDetails::getUsername)
                    .orElseThrow(() -> new IllegalStateException("User not authenticated"));

            this.currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in DB"));

            setSizeFull();
            setWidthFull();
            setPadding(true);
            setSpacing(true);
            setAlignItems(Alignment.STRETCH);

            H3 title = new H3("My Image Gallery");
            Upload upload = createUpload();
            configureGalleryLayout();
            configureGalleryScroller();
            refreshGallery();

            add(title, upload, galleryScroller);
            setFlexGrow(1, galleryScroller);
        }

        private Upload createUpload() {
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes(ACCEPTED_IMAGE_TYPES);
            upload.setMaxFileSize(MAX_IMAGE_BYTES);
            upload.setMaxFiles(10);
            upload.setDropAllowed(true);

            // Main upload flow: read bytes, validate/save via service, and refresh gallery.
            upload.addSucceededListener(event -> handleUploadSuccess(upload, buffer, event.getFileName(), event.getMIMEType()));

            upload.addFileRejectedListener(event -> {
                showError(mapFileRejectedMessage(event.getErrorMessage()));
                upload.clearFileList();
            });

            upload.addFailedListener(event -> {
                showError(mapUploadFailedMessage(event.getReason(), event.getFileName()));
                upload.clearFileList();
            });

            // Safety net: clear client-side file rows after every attempt.
            upload.addFinishedListener(event -> upload.clearFileList());
            return upload;
        }

        private void handleUploadSuccess(Upload upload, MemoryBuffer buffer, String fileName, String mimeType) {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                imageService.saveImage(fileName, mimeType, bytes, currentUser);
                showSuccess();
                refreshGallery();
            } catch (IOException e) {
                showError("Failed to read uploaded image");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError("Upload failed. Please try again.");
            } finally {
                upload.clearFileList();
            }
        }

        private String mapUploadFailedMessage(Throwable reason, String fileName) {
            String raw = "";
            if (reason != null) {
                String message = reason.getMessage();
                raw = (message == null || message.isBlank()) ? reason.toString() : message;
            }

            String message = raw.toLowerCase(Locale.ROOT);
            if (message.contains("forbidden") || message.contains("403") || message.contains("access denied")) {
                return "Upload forbidden. Please log in again and retry.";
            }
            if (message.contains("413") || message.contains("payload") || message.contains("too large")) {
                return "Server rejected this file size. Please upload an image up to 5 MB.";
            }

            String safeFileName = (fileName == null || fileName.isBlank()) ? "file" : fileName;
            return "Failed to upload " + safeFileName + ". Please try again.";
        }

        private String mapFileRejectedMessage(String rawMessage) {
            String message = rawMessage == null ? "" : rawMessage.toLowerCase(Locale.ROOT);

            if (message.contains("file is too big") || message.contains("maximum") || message.contains("exceed")) {
                return "Image is too large. Maximum size is 5 MB";
            }
            if (message.contains("incorrect file type") || message.contains("file type") || message.contains("mime")) {
                return "Only PNG, JPG, GIF, and WEBP images are allowed";
            }
            return "Upload failed. Please try a PNG, JPG, GIF, or WEBP image.";
        }

        private void configureGalleryLayout() {
            galleryLayout.setWidthFull();
            galleryLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            galleryLayout.getStyle().set("align-content", "flex-start");
            galleryLayout.getStyle().set("gap", "1rem");
        }

        private void configureGalleryScroller() {
            galleryScroller.setSizeFull();
            galleryScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
            galleryScroller.getStyle().set("min-height", "0");
        }

        private void refreshGallery() {
            galleryLayout.removeAll();

            var images = imageService.getImages(currentUser);
            if (images.isEmpty()) {
                galleryLayout.add(new Paragraph("No images uploaded yet."));
                return;
            }

            images.forEach(image -> galleryLayout.add(createImageCard(image)));
        }

        private VerticalLayout createImageCard(UserImage image) {
            StreamResource resource = new StreamResource(
                    image.getId() + "-" + image.getFileName(),
                    () -> new ByteArrayInputStream(image.getData())
            );
            resource.setContentType(image.getContentType());

            Image preview = new Image(resource, image.getFileName());
            preview.setWidth("220px");
            preview.setHeight("180px");
            preview.getStyle().set("object-fit", "cover");
            preview.getStyle().set("border-radius", "8px");

            Paragraph fileName = new Paragraph(image.getFileName());
            fileName.getStyle().set("margin", "0");
            fileName.getStyle().set("max-width", "220px");

            Button deleteButton = new Button("Delete", click -> {
                imageService.deleteImage(image, currentUser);
                refreshGallery();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout footer = new HorizontalLayout(deleteButton);
            footer.setPadding(false);
            footer.setSpacing(false);

            VerticalLayout card = new VerticalLayout(preview, fileName, footer);
            card.setPadding(false);
            card.setSpacing(true);
            card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
            card.getStyle().set("border-radius", "10px");
            card.getStyle().set("padding", "0.75rem");
            card.setWidth("250px");

            return card;
        }

        private void showSuccess() {
            Notification notification = Notification.show("Image uploaded", 2500, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }

        private void showError(String message) {
            Notification notification = Notification.show(message, 3500, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

