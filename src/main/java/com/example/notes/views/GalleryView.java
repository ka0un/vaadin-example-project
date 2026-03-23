package com.example.notes.views;

import com.example.notes.data.entity.GalleryItem;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.GalleryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final GalleryService galleryService;
    private final User currentUser;
    private final FlexLayout galleryContainer = new FlexLayout();

    public GalleryView(GalleryService galleryService, UserRepository userRepository, AuthenticationContext authContext) {
        this.galleryService = galleryService;

        // Resolve Auth Security Context: Get the username of the logged-in user
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        // Fetch User Entity: Map the security username to a database User record
        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in database"));

        // Layout Configuration: Set up the main VerticalLayout properties
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        // Header: Add a styled title to the page
        H2 title = new H2("Image Gallery");
        title.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Bottom.LARGE);
        add(title);

        // Initialize Sub-layouts: Configure components and initial data load
        configureUpload();
        configureGallery();
        updateGallery();
    }

    /**
     * Configures the Upload component, event listeners, and styling for the upload area.
     */
    private void configureUpload() {
        // MultiFileMemoryBuffer stores uploaded data temporarily in JVM memory
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        
        // Define constraints for the upload component
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(10 * 1024 * 1024); // Cap file size at 10MB

        // Helper text displayed above the upload area
        Span label = new Span("Upload new images (Max 10MB)");
        label.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        // Success Listener: Executed when a file is successfully received by the server
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            galleryService.saveGalleryItem(buffer.getInputStream(fileName), fileName, event.getMIMEType(), currentUser);
            
            Notification.show("Image '" + fileName + "' uploaded successfully!", 3000, Notification.Position.TOP_CENTER);
            
            updateGallery();
            
            upload.getElement().executeJs("this.files = []");
        });

        // Error Handlers: Provide feedback when a file is rejected or upload fails
        upload.addFileRejectedListener(event -> {
            Notification.show("Upload failed: " + event.getErrorMessage(), 5000, Notification.Position.TOP_CENTER);
        });

        upload.addFailedListener(event -> {
            Notification.show("Upload failed on server: " + event.getReason().getMessage(), 5000, Notification.Position.TOP_CENTER);
        });

        // Wrap the upload component in a centred layout
        VerticalLayout uploadLayout = new VerticalLayout(label, upload);
        uploadLayout.setAlignItems(Alignment.CENTER);
        uploadLayout.setPadding(false);
        add(uploadLayout);
    }

    /**
     * Initializes the grid layout properties for the gallery container.
     */
    private void configureGallery() {
        galleryContainer.setWidthFull();
        galleryContainer.getStyle().set("flex-wrap", "wrap");
        galleryContainer.getStyle().set("gap", "24px");
        galleryContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        galleryContainer.addClassNames(LumoUtility.Padding.Top.LARGE);
        add(galleryContainer);
    }

    /**
     * Rebuilds the gallery display by fetching the latest items from the database.
     */
    private void updateGallery() {
        galleryContainer.removeAll();
        List<GalleryItem> items = galleryService.getGalleryItems(currentUser);

        // Show a message if no images are currently available
        if (items.isEmpty()) {
            Div emptyState = new Div(new Span("No images found. Start by uploading one!"));
            emptyState.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.LARGE);
            galleryContainer.add(emptyState);
            return;
        }

        // Iterate through gallery items and create a visual card for each
        for (GalleryItem item : items) {
            galleryContainer.add(createGalleryCard(item));
        }
    }

    /**
     * Creates a styled Card component for a single gallery image, including hover effects and actions.
     */
    private Div createGalleryCard(GalleryItem item) {
        Div card = new Div();
        card.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.SMALL,
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.MEDIUM,
                "gallery-card"
        );
        card.setWidth("240px");
        
        // Dynamic Interactivity: Inject CSS transitions and Browser-side JavaScript for hover effects
        card.getStyle().set("transition", "transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out");
        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-5px)'; this.style.boxShadow = '0 10px 20px rgba(0,0,0,0.1)'; };" +
                                   "this.onmouseout = () => { this.style.transform = 'translateY(0)'; this.style.boxShadow = 'var(--lumo-box-shadow-m)'; };");

        StreamResource resource = new StreamResource(item.getFileName(), () -> {
            try {
                return new FileInputStream(item.getFilePath());
            } catch (FileNotFoundException e) {
                return null;
            }
        });

        // The Image component uses the StreamResource as its source
        Image image = new Image(resource, item.getFileName());
        image.setWidth("100%");
        image.setHeight("180px");
        image.getStyle().set("object-fit", "cover");
        image.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        // Display the original filename below the image
        Span fileName = new Span(item.getFileName());
        fileName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Vertical.SMALL, LumoUtility.TextAlignment.CENTER);
        fileName.getStyle().set("word-break", "break-all");
        fileName.getStyle().set("max-width", "200px");

        // Create a delete button with a trash icon
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        deleteBtn.setTooltipText("Delete Image");

        // Action: Confirmation dialog before deletion
        deleteBtn.addClickListener(e -> {
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("Confirm Deletion");
            
            VerticalLayout dialogLayout = new VerticalLayout(
                new Paragraph("Are you sure you want to permanently delete this image?")
            );
            dialogLayout.setPadding(false);
            dialogLayout.setSpacing(false);
            confirmDialog.add(dialogLayout);

            Button deleteConfirm = new Button("Delete", VaadinIcon.TRASH.create(), click -> {
                galleryService.deleteGalleryItem(item);
                updateGallery();
                Notification.show("Image removed from gallery.", 3000, Notification.Position.TOP_CENTER);
                confirmDialog.close();
            });
            deleteConfirm.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            Button cancelBtn = new Button("Cancel", click -> confirmDialog.close());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            confirmDialog.getFooter().add(cancelBtn, deleteConfirm);
            confirmDialog.open();
        });

        card.add(image, fileName, deleteBtn);
        return card;
    }
}
