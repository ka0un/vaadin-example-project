package com.example.notes.views;

import com.example.notes.data.entity.GalleryItem;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.GalleryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
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

/**
 * View for image upload and gallery display.
 */
@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final GalleryService galleryService;
    private final User currentUser;
    private final FlexLayout galleryContainer = new FlexLayout();

    public GalleryView(GalleryService galleryService, UserRepository userRepository, AuthenticationContext authContext) {
        this.galleryService = galleryService;

        // Fetch current user details from security context
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        // Fetch current user from database
        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in database"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        H2 title = new H2("Image Gallery");
        title.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Bottom.LARGE);
        add(title);

        configureUpload();
        configureGallery();
        updateGallery();
    }

    private void configureUpload() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB limit

        // UI Label for upload
        Span label = new Span("Upload new images (Max 10MB)");
        label.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        upload.addSucceededListener(event -> {
            galleryService.saveGalleryItem(buffer.getInputStream(), event.getFileName(), event.getMIMEType(), currentUser);
            Notification.show("Image '" + event.getFileName() + "' uploaded successfully!", 3000, Notification.Position.TOP_CENTER);
            updateGallery();
        });

        upload.addFileRejectedListener(event -> {
            Notification.show("Upload failed: " + event.getErrorMessage(), 5000, Notification.Position.TOP_CENTER);
        });

        upload.addFailedListener(event -> {
            Notification.show("Upload failed on server: " + event.getReason().getMessage(), 5000, Notification.Position.TOP_CENTER);
        });

        VerticalLayout uploadLayout = new VerticalLayout(label, upload);
        uploadLayout.setAlignItems(Alignment.CENTER);
        uploadLayout.setPadding(false);
        add(uploadLayout);
    }

    private void configureGallery() {
        galleryContainer.setWidthFull();
        galleryContainer.getStyle().set("flex-wrap", "wrap");
        galleryContainer.getStyle().set("gap", "24px");
        galleryContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        galleryContainer.addClassNames(LumoUtility.Padding.Top.LARGE);
        add(galleryContainer);
    }

    private void updateGallery() {
        galleryContainer.removeAll();
        List<GalleryItem> items = galleryService.getGalleryItems(currentUser);

        if (items.isEmpty()) {
            Div emptyState = new Div(new Span("No images found. Start by uploading one!"));
            emptyState.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.LARGE);
            galleryContainer.add(emptyState);
            return;
        }

        for (GalleryItem item : items) {
            galleryContainer.add(createGalleryCard(item));
        }
    }

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
        card.getStyle().set("transition", "transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out");
        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-5px)'; this.style.boxShadow = '0 10px 20px rgba(0,0,0,0.1)'; };" +
                                   "this.onmouseout = () => { this.style.transform = 'translateY(0)'; this.style.boxShadow = 'var(--lumo-box-shadow-m)'; };");

        // Create StreamResource for the image
        StreamResource resource = new StreamResource(item.getFileName(), () -> {
            try {
                return new FileInputStream(item.getFilePath());
            } catch (FileNotFoundException e) {
                return null;
            }
        });

        Image image = new Image(resource, item.getFileName());
        image.setWidth("100%");
        image.setHeight("180px");
        image.getStyle().set("object-fit", "cover");
        image.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        Span fileName = new Span(item.getFileName());
        fileName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Vertical.SMALL, LumoUtility.TextAlignment.CENTER);
        fileName.getStyle().set("word-break", "break-all");
        fileName.getStyle().set("max-width", "200px");

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            galleryService.deleteGalleryItem(item);
            updateGallery();
            Notification.show("Removed from gallery.");
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        deleteBtn.setTooltipText("Delete Image");

        card.add(image, fileName, deleteBtn);
        return card;
    }
}
