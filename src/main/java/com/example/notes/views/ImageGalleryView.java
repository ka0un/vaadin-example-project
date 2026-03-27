package com.example.notes.views;

import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageGalleryItem;
import com.example.notes.views.components.ImageUploadComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "gallery", layout = MainLayout.class)
@PageTitle("Gallery | Vaadin Notes App")
@PermitAll
public class ImageGalleryView extends VerticalLayout {

    private final ImageService imageService;
    private final User currentUser;

    // Toolbar
    private final Span totalCountSpan = new Span();
    private final TextField searchField = new TextField();

    // Tab switcher
    private final Tab allTab = new Tab(VaadinIcon.PICTURE.create(), new Span("All Images"));
    private final Tab favTab = new Tab(VaadinIcon.HEART.create(), new Span("Favourites"));

    // Separate grids for each tab
    private final FlexLayout allImagesGrid = new FlexLayout();
    private final FlexLayout favoritesGrid = new FlexLayout();
    private final VerticalLayout allImagesPanel = new VerticalLayout();
    private final VerticalLayout favoritesPanel = new VerticalLayout();

    // Navigation state — per-tab image list
    private List<Image> allImages = new ArrayList<>();
    private List<Image> favoriteImages = new ArrayList<>();
    private List<Image> currentImages = new ArrayList<>(); // active in lightbox
    private int currentIndex = 0;
    private String currentSearchFilter = "";
    private boolean onFavoritesTab = false;

    // Lightbox components
    private Dialog lightboxDialog;
    private com.vaadin.flow.component.html.Image lightboxImage;
    private H4 imageNameLabel;
    private final HorizontalLayout metaLayout = new HorizontalLayout();
    private Button prevButton;
    private Button nextButton;
    private Button favoriteButton;
    private Anchor downloadAnchor;

    public ImageGalleryView(ImageService imageService, UserRepository userRepository,
                            AuthenticationContext authContext) {
        this.imageService = imageService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        H2 title = new H2("My Image Gallery");
        title.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.MEDIUM);

        ImageUploadComponent upload = new ImageUploadComponent(imageService, currentUser, this::onImageUploaded);
        upload.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        HorizontalLayout toolbar = buildToolbar();
        Tabs tabs = buildTabs();

        configureGrid(allImagesGrid);
        configureGrid(favoritesGrid);

        allImagesPanel.add(allImagesGrid);
        allImagesPanel.setPadding(false);
        allImagesPanel.setSpacing(false);
        allImagesPanel.setWidthFull();

        favoritesPanel.add(favoritesGrid);
        favoritesPanel.setPadding(false);
        favoritesPanel.setSpacing(false);
        favoritesPanel.setWidthFull();
        favoritesPanel.setVisible(false);

        buildLightboxDialog();

        add(title, upload, toolbar, tabs, allImagesPanel, favoritesPanel);

        refreshAll();
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────────

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.Padding.Horizontal.MEDIUM);

        totalCountSpan.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.SECONDARY);

        searchField.setPlaceholder("Search images...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setWidth("260px");
        searchField.addValueChangeListener(e -> {
            currentSearchFilter = e.getValue();
            refreshAll();
        });

        toolbar.add(totalCountSpan, searchField);
        return toolbar;
    }

    // ─── Tabs ─────────────────────────────────────────────────────────────────

    private Tabs buildTabs() {
        Tabs tabs = new Tabs(allTab, favTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(e -> {
            onFavoritesTab = e.getSelectedTab() == favTab;
            allImagesPanel.setVisible(!onFavoritesTab);
            favoritesPanel.setVisible(onFavoritesTab);
            // Search only applies to "All" tab; clear when switching
            if (onFavoritesTab) {
                searchField.setEnabled(false);
            } else {
                searchField.setEnabled(true);
            }
        });
        return tabs;
    }

    // ─── Gallery grid config ─────────────────────────────────────────────────

    private void configureGrid(FlexLayout grid) {
        grid.setWidthFull();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.setJustifyContentMode(JustifyContentMode.CENTER);
        grid.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.Padding.MEDIUM);
    }

    // ─── Data refresh ─────────────────────────────────────────────────────────

    private void onImageUploaded() {
        searchField.setValue("");
        currentSearchFilter = "";
        refreshAll();
    }

    private void refreshAll() {
        allImages = imageService.getImagesByUserFiltered(currentUser, currentSearchFilter);
        favoriteImages = imageService.getFavoritesByUser(currentUser);

        long totalCount = imageService.countImagesByUser(currentUser);
        totalCountSpan.setText("Total: " + totalCount + " image" + (totalCount == 1 ? "" : "s")
                + " · " + favoriteImages.size() + " ❤");

        populateGrid(allImagesGrid, allImages, false);
        populateGrid(favoritesGrid, favoriteImages, true);
    }

    private void populateGrid(FlexLayout grid, List<Image> images, boolean isFavTab) {
        grid.removeAll();
        if (images.isEmpty()) {
            Paragraph empty = new Paragraph(isFavTab
                    ? "No favourites yet. Open an image and click ❤ to add one!"
                    : "No images found.");
            empty.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.AUTO);
            grid.add(empty);
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            final int index = i;
            final boolean fromFav = isFavTab;
            Image image = images.get(i);
            ImageGalleryItem item = new ImageGalleryItem(
                    image,
                    imageService,
                    this::refreshAll,
                    () -> openLightbox(index, fromFav)
            );
            grid.add(item);
        }
    }

    // ─── Lightbox ─────────────────────────────────────────────────────────────

    private void buildLightboxDialog() {
        lightboxDialog = new Dialog();
        lightboxDialog.setWidth("92vw");
        lightboxDialog.setHeight("90vh");
        lightboxDialog.getElement().getStyle()
                .set("padding", "0")
                .set("border-radius", "12px")
                .set("overflow", "hidden");

        // Top bar
        imageNameLabel = new H4("");
        imageNameLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.BODY);
        Button closeButton = new Button(VaadinIcon.CLOSE.create(), e -> lightboxDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
        HorizontalLayout topBar = new HorizontalLayout(imageNameLabel, closeButton);
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.CENTER);
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Background.CONTRAST_5);

        // Image
        lightboxImage = new com.vaadin.flow.component.html.Image();
        lightboxImage.getStyle()
                .set("max-width", "100%").set("max-height", "100%")
                .set("object-fit", "contain").set("display", "block").set("margin", "auto");
        VerticalLayout imageWrapper = new VerticalLayout(lightboxImage);
        imageWrapper.setSizeFull();
        imageWrapper.setAlignItems(Alignment.CENTER);
        imageWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        imageWrapper.setPadding(false);
        imageWrapper.setSpacing(false);

        // Navigation
        prevButton = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> navigate(-1));
        prevButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        prevButton.setWidth("56px");
        prevButton.setHeight("56px");

        nextButton = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> navigate(1));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        nextButton.setWidth("56px");
        nextButton.setHeight("56px");

        HorizontalLayout imageRow = new HorizontalLayout(prevButton, imageWrapper, nextButton);
        imageRow.setSizeFull();
        imageRow.setAlignItems(Alignment.CENTER);
        imageRow.setFlexGrow(1, imageWrapper);
        imageRow.setPadding(false);
        imageRow.setSpacing(false);
        imageRow.getStyle().set("padding", "12px");

        // Bottom bar: meta + actions
        metaLayout.setSpacing(false);
        metaLayout.getStyle().set("gap", "16px");
        metaLayout.setAlignItems(Alignment.CENTER);

        favoriteButton = new Button(VaadinIcon.HEART.create(), e -> toggleFavorite());
        favoriteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        downloadAnchor = new Anchor();
        downloadAnchor.getElement().setAttribute("download", true);
        Button downloadButton = new Button("Download", VaadinIcon.DOWNLOAD.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadAnchor.add(downloadButton);

        HorizontalLayout actionLayout = new HorizontalLayout(favoriteButton, downloadAnchor);
        actionLayout.setSpacing(true);
        actionLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout bottomBar = new HorizontalLayout(metaLayout, actionLayout);
        bottomBar.setWidthFull();
        bottomBar.setAlignItems(Alignment.CENTER);
        bottomBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        bottomBar.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Background.CONTRAST_5);

        // Assemble
        VerticalLayout dialogContent = new VerticalLayout(topBar, imageRow, bottomBar);
        dialogContent.setSizeFull();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.setFlexGrow(1, imageRow);

        lightboxDialog.add(dialogContent);
    }

    private void openLightbox(int index, boolean fromFavTab) {
        currentImages = fromFavTab ? new ArrayList<>(favoriteImages) : new ArrayList<>(allImages);
        if (currentImages.isEmpty()) return;
        currentIndex = index;
        showImageAtCurrentIndex();
        lightboxDialog.open();
    }

    private void navigate(int direction) {
        int newIndex = currentIndex + direction;
        if (newIndex >= 0 && newIndex < currentImages.size()) {
            currentIndex = newIndex;
            showImageAtCurrentIndex();
        }
    }

    private void showImageAtCurrentIndex() {
        Image image = currentImages.get(currentIndex);

        StreamResource resource = new StreamResource(
                image.getId() + "_" + image.getName(),
                () -> new ByteArrayInputStream(image.getData()));

        lightboxImage.setSrc(resource);
        lightboxImage.setAlt(image.getName());
        imageNameLabel.setText(image.getName() + " (" + (currentIndex + 1) + " / " + currentImages.size() + ")");

        // Metadata
        metaLayout.removeAll();
        String dateStr = image.getUploadDate() != null
                ? image.getUploadDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                : "Unknown date";
        metaLayout.add(metaChip(VaadinIcon.CALENDAR, dateStr));

        long kb = image.getFileSize() / 1024;
        String sizeStr = kb > 1024 ? (kb / 1024) + " MB" : kb + " KB";
        metaLayout.add(metaChip(VaadinIcon.FILE, sizeStr));

        String resStr = (image.getWidth() > 0 && image.getHeight() > 0)
                ? image.getWidth() + "×" + image.getHeight() : "Unknown size";
        metaLayout.add(metaChip(VaadinIcon.PICTURE, resStr));

        // Actions
        updateFavoriteButtonState(image.isFavorite());
        downloadAnchor.setHref(resource);
        downloadAnchor.getElement().setAttribute("download", image.getName());

        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < currentImages.size() - 1);
    }

    private HorizontalLayout metaChip(VaadinIcon iconType, String text) {
        Icon icon = iconType.create();
        icon.setSize("14px");
        Span label = new Span(text);
        label.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        HorizontalLayout chip = new HorizontalLayout(icon, label);
        chip.setSpacing(false);
        chip.setAlignItems(Alignment.CENTER);
        chip.getStyle().set("gap", "4px");
        return chip;
    }

    private void toggleFavorite() {
        Image image = currentImages.get(currentIndex);
        boolean isFav = imageService.toggleFavorite(image);
        updateFavoriteButtonState(isFav);
        refreshAll();
    }

    private void updateFavoriteButtonState(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.getStyle().set("color", "var(--lumo-error-color)");
        } else {
            favoriteButton.getStyle().remove("color");
        }
    }
}
