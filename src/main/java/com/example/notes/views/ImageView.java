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

    private Image draggedImage; 
    private GalleryFilter activeFilter = GalleryFilter.ALL;

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

        // 1. INJECT RESPONSIVE CSS
        applyResponsiveStyles();

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

        // 2. MAIN LAYOUT WITH CLASS NAME
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.addClassName("image-main-layout");
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);

        VerticalLayout contentArea = new VerticalLayout();
        contentArea.addClassName("image-content-area");
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

    private void applyResponsiveStyles() {
        getElement().executeJs(
            """
            if (!document.getElementById('gallery-resp-style')) {
                const style = document.createElement('style');
                style.id = 'gallery-resp-style';
                style.textContent = `
                    /* --- DESKTOP DEFAULT --- */
                    .image-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
                        gap: 1.5rem;
                    }

                    /* --- TABLET & MOBILE (Under 900px) --- */
                    @media (max-width: 900px) {
                        .image-main-layout { 
                            flex-direction: column !important; 
                        }
                        .image-sidebar { 
                            width: 100% !important; 
                            height: auto !important; 
                            border-right: none !important; 
                            border-bottom: 1px solid #e5e7eb;
                            padding: 1rem !important;
                        }
                        .sidebar-btn-container {
                            flex-direction: row !important;
                            overflow-x: auto;
                            gap: 10px !important;
                            padding-bottom: 5px;
                        }
                        .sidebar-btn-container vaadin-button {
                            width: auto !important;
                            flex-shrink: 0;
                        }
                        .sidebar-label { display: none; }
                        
                        .image-grid {
                            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)) !important;
                            gap: 12px !important;
                        }
                    }

                    /* --- SMALL PHONES (Under 480px) --- */
                    @media (max-width: 480px) {
                        .image-grid {
                            grid-template-columns: repeat(2, 1fr) !important;
                            gap: 8px !important;
                        }
                        .image-content-area { padding: 12px !important; }
                    }
                `;
                document.head.appendChild(style);
            }
            """
        );
    }

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.addClassName("image-sidebar");
        sidebar.setWidth("260px");
        sidebar.setHeightFull();
        sidebar.getStyle()
                .set("background-color", "#f9fafb")
                .set("border-right", "1px solid #e5e7eb")
                .set("padding", "2rem 1.5rem");

        Span libraryLabel = new Span("LIBRARY");
        libraryLabel.addClassName("sidebar-label");
        libraryLabel.getStyle().set("font-size", "0.75rem").set("font-weight", "700").set("color", "#6b7280");

        VerticalLayout btnContainer = new VerticalLayout();
        btnContainer.addClassName("sidebar-btn-container");
        btnContainer.setPadding(false);
        btnContainer.setSpacing(true);
        btnContainer.setWidthFull();

        Stream.of(uploadMediaBtn, favoritesBtn, allPhotosBtn).forEach(btn -> {
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btn.setWidthFull();
            btn.getStyle().set("justify-content", "flex-start").set("border-radius", "10px");
            btnContainer.add(btn);
        });

        sidebar.add(libraryLabel, btnContainer);
        return sidebar;
    }

    private Component createGallerySection() {
        galleryLayout.setWidthFull();
        imageGrid.setWidthFull();
        imageGrid.addClassName("image-grid");
        imageGrid.getStyle().set("margin-top", "1rem");
        
        galleryLayout.add(sectionTitle, worksCounter, emptyStateCard, imageGrid);
        return galleryLayout;
    }

    // --- REMAINDER OF YOUR ORIGINAL LOGIC UNCHANGED ---

    private void reorderImages(Image source, Image target) {
        List<Image> currentImages = imageService.getUserImages(currentUser);
        int sourceIndex = -1, targetIndex = -1;
        for (int i = 0; i < currentImages.size(); i++) {
            if (currentImages.get(i).getId().equals(source.getId())) sourceIndex = i;
            if (currentImages.get(i).getId().equals(target.getId())) targetIndex = i;
        }
        if (sourceIndex != -1 && targetIndex != -1) {
            Image movingImage = currentImages.remove(sourceIndex);
            currentImages.add(targetIndex, movingImage);
            refreshGridWithCustomList(currentImages);
        }
    }

    private void refreshGridWithCustomList(List<Image> customList) {
        imageGrid.removeAll();
        worksCounter.setText("Showing " + customList.size() + " works");
        for (Image img : customList) { imageGrid.add(createImageCard(img)); }
    }

    private Component createUploadPanel(Upload upload) {
        VerticalLayout dropZone = new VerticalLayout();
        dropZone.setAlignItems(FlexComponent.Alignment.CENTER);
        dropZone.getStyle().set("border", "2px dashed #e5e7eb").set("border-radius", "16px").set("background", "#f9fafb").set("padding", "2.5rem").set("margin-bottom", "1rem");
        H3 title = new H3("Drag and drop assets here");
        Button selectBtn = new Button("Select Files");
        selectBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(selectBtn);
        dropZone.add(VaadinIcon.PICTURE.create(), title, upload);
        return dropZone;
    }

    private Component createModernHeader() {
        H2 title = new H2("My Gallery");
        title.getStyle().set("margin", "0").set("font-size", "1.8rem").set("font-weight", "800");
        Paragraph subtitle = new Paragraph("Manage your visual assets.");
        subtitle.getStyle().set("margin", "0").set("color", "#6b7280");
        return new VerticalLayout(title, subtitle);
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
        emptyStateCard.getStyle().set("padding", "4rem").set("border", "2px dashed #e5e7eb").set("border-radius", "16px");
        emptyStateCard.add(VaadinIcon.PICTURE.create(), new H3("Gallery is empty"));
    }

        private Component createImageCard(Image img) {
        Div container = new Div();
        container.getStyle().set("position", "relative").set("aspect-ratio", "1 / 1").set("overflow", "hidden").set("border-radius", "12px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
        
        DragSource<Div> dragSource = DragSource.create(container);
        dragSource.addDragStartListener(e -> this.draggedImage = img);
        DropTarget<Div> dropTarget = DropTarget.create(container);
        dropTarget.addDropListener(e -> { if (draggedImage != null && draggedImage != img) reorderImages(draggedImage, img); });

        com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(generateResource(img), "img");
        image.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");

        // TO PREVENT STUTTERING
        image.getElement().setAttribute("loading", "lazy"); 

        HorizontalLayout actionsOverlay = new HorizontalLayout();
        actionsOverlay.getStyle().set("position", "absolute").set("top", "8px").set("right", "8px").set("opacity", "0").set("transition", "opacity 0.2s");
        
        Button favBtn = new Button(img.isFavorite() ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create(), e -> { imageService.toggleFavorite(img); refreshGallery(); });
        favBtn.getStyle().set("color", img.isFavorite() ? "#e11d48" : "white").set("background", "rgba(0,0,0,0.4)").set("border-radius", "50%");
        
        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> { imageService.deleteImage(img); refreshGallery(); });
        deleteBtn.getStyle().set("color", "white").set("background", "rgba(0,0,0,0.4)").set("border-radius", "50%");

        actionsOverlay.add(favBtn, deleteBtn);
        container.add(image, actionsOverlay);
        container.getElement().executeJs("this.addEventListener('mouseenter', () => this.querySelector('vaadin-horizontal-layout').style.opacity = '1'); this.addEventListener('mouseleave', () => this.querySelector('vaadin-horizontal-layout').style.opacity = '0');");
        return container;
    }

    private Component createUploadedFileRow(Image img) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull(); row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("background", "white").set("border", "1px solid #f3f4f6").set("border-radius", "12px").set("padding", "12px");
        com.vaadin.flow.component.html.Image thumb = new com.vaadin.flow.component.html.Image(generateResource(img), "thumb");
        thumb.setWidth("45px"); thumb.setHeight("45px"); thumb.getStyle().set("object-fit", "cover");
        row.add(thumb, new Span(formatDisplayName(img.getFileName())), VaadinIcon.CHECK_CIRCLE.create());
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
        Stream.of(allPhotosBtn, favoritesBtn, uploadMediaBtn).forEach(btn -> {
            boolean active = (btn == allPhotosBtn && activeFilter == GalleryFilter.ALL) || (btn == favoritesBtn && activeFilter == GalleryFilter.FAVORITES) || (btn == uploadMediaBtn && activeFilter == GalleryFilter.RECENT);
            btn.getStyle().set("background", active ? "#e7f0ff" : "transparent").set("color", active ? "#1d4ed8" : "#374151").set("font-weight", active ? "700" : "500");
        });
    }

    private StreamResource generateResource(Image img) {
        return new StreamResource(img.getFileName(), () -> {
            try { return new FileInputStream(img.getFilePath()); } catch (Exception e) { return InputStream.nullInputStream(); }
        });
    }

    private String formatDisplayName(String fileName) {
        int separator = fileName.indexOf('_');
        return (separator >= 0) ? fileName.substring(separator + 1) : fileName;
    }
}