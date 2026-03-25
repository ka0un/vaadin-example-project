package com.example.notes.views;
import com.example.notes.data.entity.CalendarEvent;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.UserRepository;
import com.example.notes.service.CalendarEventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.security.PermitAll;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendar | Vaadin Notes App")
@PermitAll
public class CalendarView extends VerticalLayout {

    private final CalendarEventService eventService;
    private final User currentUser;

    private LocalDate currentMonth;
    private final Div calendarGrid = new Div();
    private final Div eventsSidebar = new Div();
    private final H2 monthLabel = new H2();
    private LocalDate selectedDate;

    private static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public CalendarView(CalendarEventService eventService,
                        UserRepository userRepository,
                        AuthenticationContext authContext) {
        this.eventService = eventService;

        String username = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        this.currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        this.currentMonth = LocalDate.now().withDayOfMonth(1);
        this.selectedDate = LocalDate.now();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildTopBar());
        add(buildMainArea());
        renderCalendar();
        renderDayPanel(selectedDate);
    }

    // ── Top bar: month navigation ──────────────────────────────────────────

    private HorizontalLayout buildTopBar() {
        Button prevBtn = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> {
            currentMonth = currentMonth.minusMonths(1);
            renderCalendar();
        });
        Button nextBtn = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> {
            currentMonth = currentMonth.plusMonths(1);
            renderCalendar();
        });
        Button todayBtn = new Button("Today", e -> {
            currentMonth = LocalDate.now().withDayOfMonth(1);
            selectedDate = LocalDate.now();
            renderCalendar();
            renderDayPanel(selectedDate);
        });
        todayBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button addEventBtn = new Button("Add event", VaadinIcon.PLUS.create(), e -> openEventDialog(selectedDate));
        addEventBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        monthLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.XLARGE);

        HorizontalLayout navLeft = new HorizontalLayout(prevBtn, monthLabel, nextBtn, todayBtn);
        navLeft.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navLeft.setSpacing(true);

        HorizontalLayout bar = new HorizontalLayout(navLeft, addEventBtn);
        bar.setWidthFull();
        bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return bar;
    }

    // ── Main area: calendar grid + day sidebar ─────────────────────────────

    private HorizontalLayout buildMainArea() {
        // Calendar grid
        calendarGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(7, 1fr)")
                .set("gap", "2px")
                .set("flex", "1")
                .set("min-width", "0");

        // Day panel sidebar
        eventsSidebar.getStyle()
                .set("width", "280px")
                .set("min-width", "280px")
                .set("border-left", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-left", "var(--lumo-space-m)")
                .set("overflow-y", "auto");

        HorizontalLayout main = new HorizontalLayout(calendarGrid, eventsSidebar);
        main.setWidthFull();
        main.setSpacing(true);
        main.getStyle().set("min-height", "0").set("flex", "1");
        main.expand(calendarGrid);
        return main;
    }

    // ── Calendar grid renderer ─────────────────────────────────────────────

    private void renderCalendar() {
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + currentMonth.getYear());
        calendarGrid.removeAll();

        // Events for this month, grouped by date
        List<CalendarEvent> monthEvents = eventService.getEventsForMonth(currentUser, currentMonth);
        Map<LocalDate, List<CalendarEvent>> byDate = monthEvents.stream()
                .collect(Collectors.groupingBy(CalendarEvent::getEventDate));

        // Day name headers
        for (String day : DAY_NAMES) {
            Div header = new Div(new Span(day));
            header.getStyle()
                    .set("text-align", "center")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-weight", "600");
            calendarGrid.add(header);
        }

        // First day offset (week starts Monday)
        int startDow = currentMonth.getDayOfWeek().getValue(); // 1=Mon .. 7=Sun
        for (int i = 1; i < startDow; i++) {
            calendarGrid.add(new Div()); // empty cell
        }

        // Day cells
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = currentMonth.withDayOfMonth(d);
            List<CalendarEvent> dayEvents = byDate.getOrDefault(date, List.of());

            Div cell = buildDayCell(date, dayEvents, today);
            cell.addClickListener(e -> {
                selectedDate = date;
                renderCalendar();          // re-render to update selection highlight
                renderDayPanel(date);
            });
            calendarGrid.add(cell);
        }
    }

    private Div buildDayCell(LocalDate date, List<CalendarEvent> events, LocalDate today) {
        Div cell = new Div();
        cell.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs)")
                .set("min-height", "80px")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "2px")
                .set("transition", "background-color 0.1s");

        // Background highlights
        if (date.equals(selectedDate)) {
            cell.getStyle().set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("border", "2px solid var(--lumo-primary-color)");
        } else if (date.equals(today)) {
            cell.getStyle().set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("border", "2px solid transparent");
        } else {
            cell.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border", "2px solid transparent");
        }

        // Hover
        cell.getElement().addEventListener("mouseover", e ->
                cell.getStyle().set("background-color", "var(--lumo-primary-color-10pct)"));
        cell.getElement().addEventListener("mouseout", e -> {
            if (!date.equals(selectedDate) && !date.equals(today)) {
                cell.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
            }
        });

        // Day number
        Span dayNum = new Span(String.valueOf(date.getDayOfMonth()));
        dayNum.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", date.equals(today) ? "700" : "400")
                .set("color", date.equals(today)
                        ? "var(--lumo-primary-text-color)"
                        : "var(--lumo-body-text-color)");
        cell.add(dayNum);

        // Event pills (max 3 visible, then "+N more")
        int max = 3;
        int shown = Math.min(events.size(), max);
        for (int i = 0; i < shown; i++) {
            cell.add(buildEventPill(events.get(i)));
        }
        if (events.size() > max) {
            Span more = new Span("+" + (events.size() - max) + " more");
            more.getStyle()
                    .set("font-size", "10px")
                    .set("color", "var(--lumo-secondary-text-color)");
            cell.add(more);
        }
        return cell;
    }

    private Span buildEventPill(CalendarEvent event) {
        String label = event.getStartTime() != null
                ? event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " " + event.getTitle()
                : event.getTitle();
        Span pill = new Span(label);
        pill.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "var(--lumo-primary-contrast-color)")
                .set("border-radius", "3px")
                .set("padding", "0 4px")
                .set("font-size", "10px")
                .set("overflow", "hidden")
                .set("white-space", "nowrap")
                .set("text-overflow", "ellipsis")
                .set("max-width", "100%")
                .set("display", "block");
        return pill;
    }

    // ── Day panel (sidebar) ────────────────────────────────────────────────

    private void renderDayPanel(LocalDate date) {
        eventsSidebar.removeAll();

        String dayTitle = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH));
        H3 title = new H3(dayTitle);
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);
        eventsSidebar.add(title);

        Button addBtn = new Button("Add event", VaadinIcon.PLUS.create(), e -> openEventDialog(date));
        addBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addBtn.setWidthFull();
        eventsSidebar.add(addBtn);

        List<CalendarEvent> events = eventService.getEventsForDay(currentUser, date);
        if (events.isEmpty()) {
            Paragraph empty = new Paragraph("No events scheduled.");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-top", "var(--lumo-space-m)");
            eventsSidebar.add(empty);
        } else {
            events.forEach(ev -> eventsSidebar.add(buildEventCard(ev)));
        }
    }

    private Div buildEventCard(CalendarEvent event) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-s)");

        Span titleSpan = new Span(event.getTitle());
        titleSpan.getStyle().set("font-weight", "600").set("display", "block");

        card.add(titleSpan);

        // Time range
        if (event.getStartTime() != null) {
            String time = event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            if (event.getEndTime() != null) {
                time += " – " + event.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            Span timeSpan = new Span(time);
            timeSpan.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)");
            card.add(timeSpan);
        }

        // Description
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            Paragraph desc = new Paragraph(event.getDescription());
            desc.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin", "4px 0 0 0");
            card.add(desc);
        }

        // Delete button
        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            eventService.delete(event);
            renderDayPanel(selectedDate);
            renderCalendar();
            Notification.show("Event deleted.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.getStyle().set("margin-top", "var(--lumo-space-xs)");
        card.add(deleteBtn);

        return card;
    }

    // ── Add/Edit Event dialog ──────────────────────────────────────────────

    private void openEventDialog(LocalDate defaultDate) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New event");
        dialog.setWidth("400px");

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();
        titleField.setRequired(true);
        titleField.setPlaceholder("Event title");

        DatePicker datePicker = new DatePicker("Date");
        datePicker.setValue(defaultDate);
        datePicker.setWidthFull();
        datePicker.setRequired(true);

        TimePicker startTimePicker = new TimePicker("Start time");
        startTimePicker.setWidthFull();

        TimePicker endTimePicker = new TimePicker("End time");
        endTimePicker.setWidthFull();

        TextArea descField = new TextArea("Description");
        descField.setWidthFull();
        descField.setPlaceholder("Optional description");
        descField.setMaxLength(500);

        VerticalLayout form = new VerticalLayout(titleField, datePicker,
                new HorizontalLayout(startTimePicker, endTimePicker), descField);
        form.setPadding(false);
        form.setSpacing(true);
        dialog.add(form);

        Button saveBtn = new Button("Save", e -> {
            if (titleField.isEmpty()) {
                titleField.setErrorMessage("Title is required");
                titleField.setInvalid(true);
                return;
            }
            if (datePicker.isEmpty()) {
                datePicker.setErrorMessage("Date is required");
                datePicker.setInvalid(true);
                return;
            }

            LocalTime start = startTimePicker.isEmpty() ? null : startTimePicker.getValue();
            LocalTime end = endTimePicker.isEmpty() ? null : endTimePicker.getValue();

            CalendarEvent event = new CalendarEvent(
                    titleField.getValue(),
                    descField.getValue(),
                    datePicker.getValue(),
                    start,
                    end,
                    currentUser
            );
            eventService.save(event);
            dialog.close();

            // Navigate month view to the saved event's month
            currentMonth = datePicker.getValue().withDayOfMonth(1);
            selectedDate = datePicker.getValue();
            renderCalendar();
            renderDayPanel(selectedDate);
            Notification.show("Event saved!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }
}