package com.example.notes.views;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserImage;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.ImageService;
import com.example.notes.views.components.ImageCardComponent;
import com.example.notes.views.components.ImageGalleryToolbar;
import com.example.notes.views.components.ImageUploadComponent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Route(value = "images", layout = MainLayout.class)
@PageTitle("Images | Vaadin Notes App")
@PermitAll
@CssImport("./styles/imageView.css")
public class ImageView extends VerticalLayout {

    private final FlexLayout gallery = new FlexLayout();
    private final ImageService imageService;
    private final User currentUser;

    private final Set<Long> selectedIds = new HashSet<>();
    private final Map<Long, Div> cardMap = new HashMap<>();

    private ImageGalleryToolbar toolbar;

    public ImageView(ImageService imageService, UserRepository userRepository, AuthenticationContext authContext) {
        this.imageService = imageService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB"));

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        H2 title = new H2("Image Upload");
        title.addClassName("image-title");

        gallery.addClassName("image-gallery");

        toolbar = new ImageGalleryToolbar(
                selectedIds,
                cardMap,
                imageService,
                deletedIds -> {
                    // remove cards from gallery after batch delete
                    deletedIds.forEach(id -> {
                        gallery.remove(cardMap.get(id));
                        cardMap.remove(id);
                    });
                    selectedIds.clear();
                    toolbar.refresh();
                }
        );

        ImageUploadComponent uploadComponent = new ImageUploadComponent(
                imageService,
                currentUser,
                savedImages -> savedImages.forEach(this::addImageToGallery)
        );

        add(title, uploadComponent, toolbar, gallery);
        updateGallery();
    }

    private void addImageToGallery(UserImage userImage) {
        ImageCardComponent card = new ImageCardComponent(
                userImage,
                selectedIds,
                toggled -> toolbar.refresh()
        );
        cardMap.put(userImage.getId(), card);
        gallery.add(card);
    }

    private void updateGallery() {
        gallery.removeAll();
        cardMap.clear();
        selectedIds.clear();
        imageService.getByUser(currentUser).forEach(this::addImageToGallery);
        toolbar.refresh();

    }
}