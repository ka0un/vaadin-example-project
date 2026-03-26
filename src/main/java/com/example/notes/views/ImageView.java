package com.example.notes.views;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.security.CurrentUserService;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

@Route(value = "images", layout = MainLayout.class)
@PageTitle("Image Gallery")
@PermitAll
public class ImageView extends VerticalLayout {

    private enum GalleryFilter {
        ALL, RECENT, FAVORITES
    }

    private final ImageService imageService;
    private final CurrentUserService userService;
    private final User currentUser;

    // --- State Variables ---
    private Image draggedImage; 
    private GalleryFilter activeFilter = GalleryFilter.ALL;

    // --- UI Components ---
    private final VerticalLayout galleryLayout = new VerticalLayout();
    private final VerticalLayout emptyStateCard = new VerticalLayout();
    private final Div imageGrid = new Div(); 
    private final Span worksCounter = new Span();
    private Component uploadPanel;

    private final H2 sectionTitle = new H2("All photos");
    private final Button allPhotosBtn = new Button("All photos", VaadinIcon.GRID_BIG_O.create());
    private final Button favoritesBtn = new Button("Favorites", VaadinIcon.HEART_O.create());
    private final Button uploadMediaBtn = new Button("Upload media", VaadinIcon.UPLOAD.create());

    public ImageView(ImageService imageService, CurrentUserService userService) {
        this.imageService = imageService;
        this.userService = userService;
        this.currentUser = userService.getCurrentUser();

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");
        
        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                imageService.saveImage(fileName, buffer.getInputStream(fileName), currentUser);
                Notification.show("Uploaded: " + fileName, 1800, Notification.Position.TOP_CENTER);
                refreshGallery();
            } catch (IOException | IllegalArgumentException e) {
                Notification.show("Upload failed: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);

        VerticalLayout contentArea = new VerticalLayout();
        contentArea.getStyle().set("flex", "1").set("background", "#ffffff").set("overflow-y", "auto");
        contentArea.setPadding(true);

        uploadPanel = createUploadPanel(upload);
        uploadPanel.setVisible(false);

        contentArea.add(createModernHeader(), uploadPanel, createGallerySection());
        mainLayout.add(createSidebar(), contentArea);
        add(mainLayout);

        initSidebarActions();
        applySidebarActiveStyles();
        refreshGallery();
    }

    // --- Reordering Logic ---
    private void reorderImages(Image source, Image target) {
        List<Image> currentImages = imageService.getUserImages(currentUser);
        
        int sourceIndex = -1;
        int targetIndex = -1;

        for (int i = 0; i < currentImages.size(); i++) {
            if (currentImages.get(i).getId().equals(source.getId())) sourceIndex = i;
            if (currentImages.get(i).getId().equals(target.getId())) targetIndex = i;
        }

        if (sourceIndex != -1 && targetIndex != -1) {
            Image movingImage = currentImages.remove(sourceIndex);
            currentImages.add(targetIndex, movingImage);
            
            refreshGridWithCustomList(currentImages);
            Notification.show("Moved to new position");
        }
    }

    private void refreshGridWithCustomList(List<Image> customList) {
        imageGrid.removeAll();
        worksCounter.setText("Showing " + customList.size() + " works");
        for (Image img : customList) {
            imageGrid.add(createImageCard(img));
        }
    }

    // --- UI Builders ---
    private Component createUploadPanel(Upload upload) {
        VerticalLayout dropZone = new VerticalLayout();
        dropZone.setAlignItems(FlexComponent.Alignment.CENTER);
        dropZone.getStyle()
                .set("border", "2px dashed #e5e7eb")
                .set("border-radius", "16px")
                .set("background", "#f9fafb")
                .set("padding", "2.5rem")
                .set("margin-bottom", "1rem");

        Icon uploadIcon = VaadinIcon.PICTURE.create();
        uploadIcon.getStyle().set("color", "#3b82f6").set("font-size", "2rem");

        H3 title = new H3("Drag and drop assets here");
        title.getStyle().set("margin-top", "0.5rem");
        
        Paragraph subtitle = new Paragraph("Your files will appear below once uploaded.");
        subtitle.getStyle().set("color", "#6b7280").set("font-size", "0.85rem");

        Button selectBtn = new Button("Select Files");
        selectBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(selectBtn);

        dropZone.add(uploadIcon, title, subtitle, upload);
        return dropZone;
    }

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("260px");
        sidebar.setHeightFull();
        sidebar.getStyle()
                .set("background-color", "#f9fafb")
                .set("border-right", "1px solid #e5e7eb")
                .set("padding", "2rem 1.5rem");

        Span libraryLabel = new Span("LIBRARY");
        libraryLabel.getStyle().set("font-size", "0.75rem").set("font-weight", "700").set("color", "#6b7280");

        Stream.of(uploadMediaBtn, favoritesBtn, allPhotosBtn).forEach(btn -> {
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btn.setWidthFull();
            btn.getStyle().set("justify-content", "flex-start").set("border-radius", "10px");
        });

        sidebar.add(libraryLabel, uploadMediaBtn, favoritesBtn, allPhotosBtn);
        return sidebar;
    }

    private Component createModernHeader() {
        H2 title = new H2("My Gallery");
        title.getStyle().set("margin", "0").set("font-size", "1.8rem").set("font-weight", "800");
        Paragraph subtitle = new Paragraph("Manage your visual assets.");
        subtitle.getStyle().set("margin", "0").set("color", "#6b7280");
        return new VerticalLayout(title, subtitle);
    }

    private Component createGallerySection() {
        galleryLayout.setWidthFull();
        imageGrid.setWidthFull();
        imageGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(180px, 1fr))")
                .set("gap", "1rem")
                .set("margin-top", "1rem");
        
        galleryLayout.add(sectionTitle, worksCounter, emptyStateCard, imageGrid);
        return galleryLayout;
    }

    private void refreshGallery() {
        imageGrid.removeAll();
        List<Image> images = imageService.getUserImages(currentUser);
        
        if (activeFilter == GalleryFilter.FAVORITES) {
            images = images.stream().filter(Image::isFavorite).toList();
        } 

        if (images.isEmpty()) {
            setupEmptyState();
            emptyStateCard.setVisible(true);
            imageGrid.setVisible(false);
            worksCounter.setVisible(false);
        } else {
            emptyStateCard.setVisible(false);
            imageGrid.setVisible(true);
            worksCounter.setVisible(true);
            worksCounter.setText("Showing " + images.size() + " works");
            
            for (Image img : images) {
                imageGrid.add(activeFilter == GalleryFilter.RECENT ? createUploadedFileRow(img) : createImageCard(img));
            }
        }
    }

    private void setupEmptyState() {
        emptyStateCard.removeAll();
        emptyStateCard.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyStateCard.setJustifyContentMode(JustifyContentMode.CENTER);
        emptyStateCard.getStyle()
                .set("padding", "4rem")
                .set("border", "2px dashed #e5e7eb")
                .set("border-radius", "16px")
                .set("margin-top", "2rem")
                .set("background", "#fcfcfc");

        Icon icon = VaadinIcon.PICTURE.create();
        icon.getStyle().set("font-size", "3.5rem").set("color", "#d1d5db");

        String titleText = activeFilter == GalleryFilter.FAVORITES ? "No favorites yet" : "Your gallery is empty";
        String descText = activeFilter == GalleryFilter.FAVORITES ? 
            "Heart your favorite images to see them here." : 
            "Drag and drop your first image or use the 'Upload media' tab to get started.";

        H3 title = new H3(titleText);
        Paragraph description = new Paragraph(descText);
        description.getStyle().set("color", "#6b7280").set("text-align", "center");

        emptyStateCard.add(icon, title, description);
    }

    private Component createImageCard(Image img) {
        Div container = new Div();
        container.getStyle()
                .set("position", "relative")
                .set("width", "100%")
                .set("aspect-ratio", "1 / 1")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        DragSource<Div> dragSource = DragSource.create(container);
        dragSource.setDraggable(true);
        dragSource.addDragStartListener(e -> {
            container.getStyle().set("opacity", "0.4");
            this.draggedImage = img; 
        });
        dragSource.addDragEndListener(e -> container.getStyle().set("opacity", "1"));

        DropTarget<Div> dropTarget = DropTarget.create(container);
        dropTarget.addDropListener(e -> {
            if (draggedImage != null && draggedImage != img) {
                reorderImages(draggedImage, img);
            }
        });

        com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(generateResource(img), "uploaded");
        image.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");

        HorizontalLayout actionsOverlay = new HorizontalLayout();
        actionsOverlay.getStyle()
                .set("position", "absolute")
                .set("top", "8px")
                .set("right", "8px")
                .set("z-index", "1")
                .set("opacity", "0")
                .set("transition", "opacity 0.2s ease-in-out");
        
        Icon heartIcon = img.isFavorite() ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create();
        Button favBtn = new Button(heartIcon, e -> {
            imageService.toggleFavorite(img);
            refreshGallery();
        });
        favBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        favBtn.getStyle()
                .set("color", img.isFavorite() ? "#e11d48" : "white")
                .set("background", "rgba(0,0,0,0.4)")
                .set("border-radius", "50%")
                .set("min-width", "32px")
                .set("height", "32px");

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> {
            imageService.deleteImage(img);
            refreshGallery();
            Notification.show("Deleted successfully");
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteBtn.getStyle()
                .set("background", "rgba(0,0,0,0.4)")
                .set("color", "white")
                .set("border-radius", "50%")
                .set("min-width", "32px")
                .set("height", "32px");

        actionsOverlay.add(favBtn, deleteBtn);
        container.add(image, actionsOverlay);
        
        container.getElement().executeJs(
            "this.addEventListener('mouseenter', () => this.querySelector('vaadin-horizontal-layout').style.opacity = '1');" +
            "this.addEventListener('mouseleave', () => this.querySelector('vaadin-horizontal-layout').style.opacity = '0');"
        );

        return container;
    }

    private Component createUploadedFileRow(Image img) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle()
                .set("background", "#ffffff")
                .set("border", "1px solid #f3f4f6")
                .set("border-radius", "12px")
                .set("padding", "12px")
                .set("margin-bottom", "8px");

        com.vaadin.flow.component.html.Image thumb = new com.vaadin.flow.component.html.Image(generateResource(img), "thumb");
        thumb.setWidth("45px"); thumb.setHeight("45px");
        thumb.getStyle().set("object-fit", "cover").set("border-radius", "6px");

        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false); details.setPadding(false);
        
        Span fileName = new Span(formatDisplayName(img.getFileName()));
        fileName.getStyle().set("font-weight", "600");
        
        Span statusText = new Span("UPLOAD COMPLETE");
        statusText.getStyle().set("color", "#2563eb").set("font-size", "0.7rem").set("font-weight", "800");

        details.add(fileName, statusText);
        Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
        successIcon.getStyle().set("color", "#2563eb").set("margin-left", "auto");

        row.add(thumb, details, successIcon);
        return row;
    }

    private void initSidebarActions() {
        allPhotosBtn.addClickListener(e -> updateFilter(GalleryFilter.ALL, "All photos", false));
        favoritesBtn.addClickListener(e -> updateFilter(GalleryFilter.FAVORITES, "Favorites", false));
        uploadMediaBtn.addClickListener(e -> updateFilter(GalleryFilter.RECENT, "Recent uploads", true));
    }

    private void updateFilter(GalleryFilter filter, String title, boolean showUpload) {
        activeFilter = filter;
        sectionTitle.setText(title);
        uploadPanel.setVisible(showUpload);
        applySidebarActiveStyles();
        refreshGallery();
    }

    private void applySidebarActiveStyles() {
        setSidebarButtonActiveStyle(allPhotosBtn, activeFilter == GalleryFilter.ALL);
        setSidebarButtonActiveStyle(favoritesBtn, activeFilter == GalleryFilter.FAVORITES);
        setSidebarButtonActiveStyle(uploadMediaBtn, activeFilter == GalleryFilter.RECENT);
    }

    private void setSidebarButtonActiveStyle(Button button, boolean isActive) {
        button.getStyle().set("background", isActive ? "#e7f0ff" : "transparent")
                         .set("color", isActive ? "#1d4ed8" : "#374151")
                         .set("font-weight", isActive ? "700" : "500");
    }

    private StreamResource generateResource(Image img) {
        return new StreamResource(img.getFileName(), () -> {
            try { return new FileInputStream(img.getFilePath()); } 
            catch (Exception e) { return InputStream.nullInputStream(); }
        });
    }

    private String formatDisplayName(String fileName) {
        int separator = fileName.indexOf('_');
        return (separator >= 0) ? fileName.substring(separator + 1) : fileName;
    }
}