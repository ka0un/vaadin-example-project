package com.example.notes.views;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.security.CurrentUserService;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

    private final VerticalLayout galleryLayout = new VerticalLayout();
    private final VerticalLayout emptyStateCard = new VerticalLayout();
    private final VerticalLayout imageGrid = new VerticalLayout();
    private final Span emptyTitle = new Span();
    private final Paragraph emptySubtitle = new Paragraph();
    private final Span worksCounter = new Span();
    private Component uploadPanel;

    private final H2 sectionTitle = new H2("All photos");
    private final Button allPhotosBtn = new Button("All photos", VaadinIcon.GRID_BIG_O.create());
    private final Button favoritesBtn = new Button("Favorites", VaadinIcon.HEART_O.create());
    private final Button uploadMediaBtn = new Button("Upload media", VaadinIcon.UPLOAD.create());
    private GalleryFilter activeFilter = GalleryFilter.ALL;

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

        // ✅ FIXED: Method now exists below
        uploadPanel = createUploadPanel(upload);
        uploadPanel.setVisible(false);

        contentArea.add(createModernHeader(), uploadPanel, createGallerySection());
        mainLayout.add(createSidebar(), contentArea);
        add(mainLayout);

        initSidebarActions();
        applySidebarActiveStyles();
        refreshGallery();
    }

    // ✅ FIXED: Added missing method to resolve compilation error
    private Component createUploadPanel(Upload upload) {
        VerticalLayout dropZone = new VerticalLayout();
        dropZone.setAlignItems(Alignment.CENTER);
        dropZone.getStyle()
                .set("border", "2px dashed #e5e7eb")
                .set("border-radius", "16px")
                .set("background", "#f9fafb")
                .set("padding", "2.5rem");

        Icon uploadIcon = VaadinIcon.PICTURE.create();
        uploadIcon.getStyle().set("color", "#3b82f6").set("font-size", "2rem");

        H3 title = new H3("Drag and drop assets here");
        title.getStyle().set("margin-top", "0.5rem");
        
        Paragraph subtitle = new Paragraph("Your files will appear in the list below once uploaded.");
        subtitle.getStyle().set("color", "#6b7280").set("font-size", "0.85rem");

        // Use a nice blue button for selection
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

    private Component createUploadedFileRow(Image img) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.getStyle()
                .set("background", "#ffffff")
                .set("border", "1px solid #f3f4f6")
                .set("border-radius", "12px")
                .set("padding", "12px")
                .set("margin-bottom", "8px");

        com.vaadin.flow.component.html.Image thumb = new com.vaadin.flow.component.html.Image(generateResource(img), "thumb");
        thumb.setWidth("45px");
        thumb.setHeight("45px");
        thumb.getStyle().set("object-fit", "cover").set("border-radius", "6px");

        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);
        
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

    private Component createGallerySection() {
        galleryLayout.setWidthFull();
        imageGrid.setWidthFull();
        imageGrid.getStyle().set("display", "grid").set("gap", "1.5rem");
        galleryLayout.add(sectionTitle, worksCounter, emptyStateCard, imageGrid);
        return galleryLayout;
    }

    private void refreshGallery() {
        imageGrid.removeAll();
        List<Image> images = imageService.getUserImages(currentUser);
        
        if (activeFilter == GalleryFilter.FAVORITES) {
            images = images.stream().filter(Image::isFavorite).toList();
            imageGrid.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))");
        } else if (activeFilter == GalleryFilter.RECENT) {
            images = images.stream().limit(8).toList();
            imageGrid.getStyle().set("grid-template-columns", "1fr"); 
        } else {
            imageGrid.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))");
        }

        worksCounter.setText("Showing " + images.size() + " works");
        
        if (images.isEmpty()) {
            setupEmptyState();
            emptyStateCard.setVisible(true);
            imageGrid.setVisible(false);
        } else {
            emptyStateCard.setVisible(false);
            imageGrid.setVisible(true);
            for (Image img : images) {
                imageGrid.add(activeFilter == GalleryFilter.RECENT ? createUploadedFileRow(img) : createImageCard(img));
            }
        }
    }

    private Component createImageCard(Image img) {
        com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(generateResource(img), "uploaded");
        image.setWidthFull();
        image.setHeight("200px");
        image.getStyle().set("object-fit", "cover").set("border-radius", "12px");

        Icon heartIcon = img.isFavorite() ? VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create();
        Button favBtn = new Button(heartIcon, e -> {
            imageService.toggleFavorite(img);
            refreshGallery();
        });
        favBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        favBtn.getStyle().set("color", img.isFavorite() ? "#e11d48" : "#6b7280");

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> {
            imageService.deleteImage(img);
            refreshGallery();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout actions = new HorizontalLayout(favBtn, deleteBtn);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout card = new VerticalLayout(image, new Span(formatDisplayName(img.getFileName())), actions);
        card.getStyle().set("border", "1px solid #e5e7eb").set("border-radius", "16px");
        return card;
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

    private void setupEmptyState() {
        emptyStateCard.removeAll();
        emptyTitle.setText(activeFilter == GalleryFilter.FAVORITES ? "No favorites yet" : "No images found");
        emptyStateCard.add(new Icon(VaadinIcon.PICTURE), emptyTitle);
        emptyStateCard.setAlignItems(Alignment.CENTER);
    }

    private String formatDisplayName(String fileName) {
        int separator = fileName.indexOf('_');
        return (separator >= 0) ? fileName.substring(separator + 1) : fileName;
    }
}