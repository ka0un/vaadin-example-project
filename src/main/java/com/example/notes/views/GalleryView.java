package com.example.notes.views;

import com.example.notes.components.ImageCardComponent;
import com.example.notes.components.ImageUploadComponent;
import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The main View class for the Image Gallery.
 * Coordinates user session, UI components (upload and grid), data persistence,
 * and bulk deletion actions.
 */
@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
public class GalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final User currentUser;
    
    private final VerticalLayout imageGrid;
    private final Set<ImageEntity> selectedImages = new HashSet<>();
    private final Button deleteSelectedButton;
    private final ImageUploadComponent uploadComponent;

    public GalleryView(ImageService imageService, UserRepository userRepository, AuthenticationContext authContext) {
        this.imageService = imageService;
        this.currentUser = fetchCurrentUser(userRepository, authContext);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        
        // Gentle, eye-friendly background color for the gallery region
        getStyle().set("background-color", "#f8fafc"); // Slate-50

        uploadComponent = createUploadComponent();
        
        // Setup bulk delete button
        deleteSelectedButton = new Button("Delete Selected", VaadinIcon.TRASH.create(), e -> deleteSelectedImages());
        deleteSelectedButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteSelectedButton.setVisible(false); // Hidden until items are selected

        // Beautifully styled "My Image Gallery" bar
        H2 title = new H2("My Image Gallery");
        title.getStyle().set("color", "#312e81"); // Deep indigo text
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-weight", "800");
        title.getStyle().set("font-size", "1.75rem");

        HorizontalLayout headerBar = new HorizontalLayout(title, deleteSelectedButton);
        headerBar.setWidthFull();
        headerBar.setMaxWidth("1200px");
        headerBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerBar.setAlignItems(Alignment.CENTER);
        // Premium card styling for the bar
        headerBar.getStyle().set("background-color", "white");
        headerBar.getStyle().set("padding", "20px 32px");
        headerBar.getStyle().set("border-radius", "16px");
        headerBar.getStyle().set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)");
        headerBar.getStyle().set("margin-top", "32px");
        headerBar.getStyle().set("margin-bottom", "8px");

        imageGrid = createResponsiveGrid();
        imageGrid.setMaxWidth("1200px"); // Constrain the gallery to a readable maximum width

        add(headerBar, imageGrid);

        // Initial population of the gallery
        refreshGallery();
    }

    private User fetchCurrentUser(UserRepository userRepository, AuthenticationContext authContext) {
        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));
    }

    private ImageUploadComponent createUploadComponent() {
        ImageUploadComponent uploadComponent = new ImageUploadComponent();
        uploadComponent.setUploadSuccessListener((fileName, mimeType, data) -> {
            try {
                ImageEntity imageEntity = new ImageEntity(fileName, mimeType, data, currentUser);
                imageService.saveImage(imageEntity);
                refreshGallery();
            } catch (Exception e) {
                Notification errorNotification = Notification.show("Warning: Could not save image to database.", 5000, Notification.Position.MIDDLE);
                errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return uploadComponent;
    }

    private VerticalLayout createResponsiveGrid() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setWidthFull();
        gridLayout.setPadding(true);
        
        gridLayout.getStyle().set("display", "grid");
        gridLayout.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))");
        gridLayout.getStyle().set("gap", "24px");
        gridLayout.getStyle().set("align-items", "start");
        
        return gridLayout;
    }

    private void refreshGallery() {
        imageGrid.removeAll();
        selectedImages.clear();
        updateDeleteButtonVisibility();
        
        // Add the upload component as the first item in the grid
        imageGrid.add(uploadComponent);
        
        List<ImageEntity> images = imageService.getImagesByUser(currentUser);

        for (ImageEntity image : images) {
            ImageCardComponent card = new ImageCardComponent(image, this::handleSelectionToggle);
            imageGrid.add(card);
        }
    }

    /**
     * Triggered automatically whenever a user clicks an ImageCardComponent.
     */
    private void handleSelectionToggle(ImageCardComponent card) {
        if (card.isSelected()) {
            selectedImages.add(card.getImageEntity());
        } else {
            selectedImages.remove(card.getImageEntity());
        }
        updateDeleteButtonVisibility();
    }

    private void updateDeleteButtonVisibility() {
        deleteSelectedButton.setVisible(!selectedImages.isEmpty());
        deleteSelectedButton.setText("Delete Selected (" + selectedImages.size() + ")");
    }

    private void deleteSelectedImages() {
        try {
            for (ImageEntity image : selectedImages) {
                imageService.deleteImage(image);
            }
            Notification.show(selectedImages.size() + " images deleted successfully.", 3000, Notification.Position.MIDDLE);
            refreshGallery();
        } catch (Exception e) {
            Notification errorNotification = Notification.show("Error: Could not delete selected images.", 5000, Notification.Position.MIDDLE);
            errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
