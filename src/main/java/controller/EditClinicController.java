package controller;

import dao.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.geometry.Pos;  // ✅ مُضاف

public class EditClinicController {

    @FXML private TextField clinicNameField;
    @FXML private TextField clinicAddressField;
    @FXML private TextField slotDurationField;
    @FXML private TextField priceField;
    @FXML private VBox rulesContainer;
    @FXML private CheckBox resetAllCheck;
    @FXML private Label statusLabel;
    @FXML private CheckBox enableConsultationCheckBox;
    @FXML private VBox consultationFieldsBox;
    @FXML private TextField consultationPriceField;
    @FXML private Spinner<Integer> consultationDaysSpinner;

    private Clinic clinic;
    private Practitioner doctor;

    // DAOs
    private final ClinicDAO clinicDAO = new ClinicDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final WorkingHoursRuleDAO ruleDAO = new WorkingHoursRuleDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    // ✅ لتخزين القواعد المؤقتة (الـ UI rows)
    private final List<RuleRow> ruleRows = new ArrayList<>();

    // ===== Inner Class لتمثيل سطر قاعدة في الواجهة =====
    // ✅ تم إزالة كلمة 'static' — ليتمكّن من الوصول إلى ruleRows و rulesContainer
    private class RuleRow {
        final ComboBox<DayOfWeek> dayCombo;
        final ComboBox<LocalTime> fromCombo;
        final ComboBox<LocalTime> toCombo;
        final Button deleteBtn;
        final HBox container;

        RuleRow(boolean isNew) {
            dayCombo = new ComboBox<>();
            dayCombo.getItems().addAll(DayOfWeek.values());
            dayCombo.setPrefWidth(110);
            dayCombo.setPromptText("Day");

            fromCombo = new ComboBox<>();
            toCombo = new ComboBox<>();
            initTimeCombos();

            deleteBtn = new Button("✖");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            deleteBtn.setPrefWidth(30);

            container = new HBox(8);
            container.setAlignment(Pos.CENTER_LEFT);  // ✅ Pos معروف الآن
            container.setStyle("-fx-padding: 8 10 8 10; -fx-background-color: #f8fbf9; -fx-border-color: #e0ebeb; -fx-border-width: 1; -fx-border-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
            container.getChildren().addAll(
                    new Label("Day:"), dayCombo,
                    new Label("From:"), fromCombo,
                    new Label("To:"), toCombo,
                    new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                    deleteBtn
            );

            if (isNew) {
                deleteBtn.setOnAction(e -> {
                    rulesContainer.getChildren().remove(container);
                    ruleRows.remove(this);  // ✅ تم تصحيح ; ; إلى ;
                });
            } else {
                deleteBtn.setDisable(true); // القواعد القديمة لا تُحذف مباشرة — فقط عبر reset أو تعديل
                container.setStyle("-fx-padding: 8 10 8 10; -fx-background-color: #f0f7ff; -fx-border-color: #d0e3f5; -fx-border-width: 1; -fx-border-radius: 6; -fx-effect: none;");
            }
        }

        void setRule(WorkingHoursRule rule) {
            if (rule != null) {
                dayCombo.setValue(rule.getDay());
                fromCombo.setValue(rule.getStartTime());
                toCombo.setValue(rule.getEndTime());
            }
        }

        WorkingHoursRule getRule() {
            DayOfWeek d = dayCombo.getValue();
            LocalTime from = fromCombo.getValue();
            LocalTime to = toCombo.getValue();
            if (d == null || from == null || to == null || !from.isBefore(to)) return null;
            return new WorkingHoursRule(0, d, from, to);
        }

        private void initTimeCombos() {
            List<LocalTime> times = new ArrayList<>();
            for (int h = 6; h <= 22; h++) {
                times.add(LocalTime.of(h, 0));
                if (h < 22) times.add(LocalTime.of(h, 30));
            }
            fromCombo.getItems().setAll(times);
            toCombo.getItems().setAll(times);
            fromCombo.setPrefWidth(90);
            toCombo.setPrefWidth(90);
            fromCombo.setPromptText("HH:mm");
            toCombo.setPromptText("HH:mm");
        }
    }
    @FXML
    public void initialize() {
        consultationDaysSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 7)
        );
    }

    // ===== Controller Lifecycle =====
    public void setClinic(Clinic clinic, Practitioner doctor) {
        this.clinic = clinic;
        this.doctor = doctor;

        clinicNameField.setText(clinic.getName());
        clinicAddressField.setText(clinic.getAddress());
        priceField.setText(String.format("%.2f", clinic.getPrice()));

        if (clinic.getSchedule() != null) {
            slotDurationField.setText(String.valueOf(clinic.getSchedule().getSlotDurationInMinutes()));
        }

        loadExistingRulesToUI();
        // ★★ تحميل حالة الاستشارة من العيادة ★★
        boolean hasConsultation = clinic.getConsultationPrice() > 0
                && clinic.getConsultationDurationDays() > 0;
        enableConsultationCheckBox.setSelected(hasConsultation);
        consultationPriceField.setText(String.format("%.2f", clinic.getConsultationPrice()));
        consultationDaysSpinner.getValueFactory().setValue(clinic.getConsultationDurationDays());
        onConsultationToggle(); // تحديث ظهور الحقول
    }

    private void loadExistingRulesToUI() {
        rulesContainer.getChildren().clear();
        ruleRows.clear();

        List<WorkingHoursRule> existing = (clinic.getSchedule() != null)
                ? clinic.getSchedule().getWeeklyRules()
                : new ArrayList<>();

        if (existing.isEmpty()) {
            // لا توجد قواعد → ابدأ بسطر فارغ
            RuleRow newRow = new RuleRow(true);
            ruleRows.add(newRow);
            rulesContainer.getChildren().add(newRow.container);
        } else {
            // عرض القواعد الحالية كـ "read-only" rows (لا تُحذف مباشرة)
            for (WorkingHoursRule rule : existing) {
                RuleRow row = new RuleRow(false);
                row.setRule(rule);
                ruleRows.add(row);
                rulesContainer.getChildren().add(row.container);
            }
            // أضف سطرًا فارغًا للإضافة
            addNewRuleRow();
        }
    }

    @FXML
    private void onAddRule() {
        addNewRuleRow();
    }

    private void addNewRuleRow() {
        RuleRow newRow = new RuleRow(true);
        ruleRows.add(newRow);
        rulesContainer.getChildren().add(newRow.container);
    }

    // ===== Save Logic مع دعم الجدولين (Active + Pending) — ✅ بدون تعديل الـ Active الآن =====
    @FXML
    private void onSave() {
        try {
            String name = clinicNameField.getText().trim();
            String address = clinicAddressField.getText().trim();
            String slotStr = slotDurationField.getText().trim();
            String priceStr = priceField.getText().trim();

            if (name.isEmpty() || address.isEmpty() || slotStr.isEmpty() || priceStr.isEmpty()) {
                showAlert("Validation Error", "All fields are required.");
                return;
            }

            int slotDuration;
            double price;
            try {
                slotDuration = Integer.parseInt(slotStr);
                price = Double.parseDouble(priceStr);
                if (slotDuration <= 0 || price < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Slot duration must be a positive integer. Price must be non-negative.");
                return;
            }

            // ★★ نظام الاستشارة ★★
            boolean enableConsultation = enableConsultationCheckBox.isSelected();
            double consultationPrice = 0.0;
            int consultationDays = 0;

            if (enableConsultation) {
                try {
                    consultationPrice = Double.parseDouble(consultationPriceField.getText().trim());
                } catch (NumberFormatException e) {
                    consultationPrice = 0.0;
                }
                consultationDays = consultationDaysSpinner.getValue();
            }

            // جمع القواعد...
            List<WorkingHoursRule> newRules = new ArrayList<>();
            for (RuleRow row : ruleRows) {
                WorkingHoursRule r = row.getRule();
                if (r != null) newRules.add(r);
            }

            if (newRules.isEmpty()) {
                showAlert("Validation Error", "At least one working hour rule is required.");
                return;
            }

            // إنشاء schedule جديد (Pending)
            Schedule newPending = new Schedule(0, slotDuration);
            scheduleDAO.add(newPending);

            for (WorkingHoursRule r : newRules) {
                ruleDAO.insertRule(newPending.getID(), r.getDay(), r.getStartTime(), r.getEndTime());
            }

            // ✅ تحديث العيادة (الأساسي + الاستشارة)
            clinic.setName(name);
            clinic.setAddress(address);
            clinic.setPrice(price);
            clinic.setConsultationPrice(consultationPrice);
            clinic.setConsultationDurationDays(consultationDays);
            clinicDAO.update(clinic);

            // ربط الـ pending schedule
            clinicDAO.updatePendingScheduleId(clinic.getID(), newPending.getID());

            // رسالة نجاح
            LocalDate nextMonthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            String formattedDate = nextMonthStart.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            statusLabel.setText("✅ Saved! Changes will be active from " + formattedDate + ".");
            statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    // ✅ دالة التحقق من وجود حجوزات مستقبلية في فترة زمنية
    private boolean hasUpcomingAppointmentsInRule(WorkingHoursRule rule) throws SQLException {
        int clinicId = clinic.getID();
        DayOfWeek day = rule.getDay();
        LocalTime from = rule.getStartTime();
        LocalTime to = rule.getEndTime();

        // تحويل DayOfWeek (Mon=1 → Sun=7) إلى DAYOFWEEK (Sun=1 → Sat=7)
        int sqlDay = (day.getValue() % 7) + 1;

        String sql = """
            SELECT 1 FROM Appointments a
            JOIN TimeSlots ts ON a.time_slot_id = ts.id
            WHERE a.clinic_id = ?
              AND DATE(ts.appointment_time) >= CURDATE()
              AND DAYOFWEEK(ts.appointment_time) = ?
              AND TIME(ts.appointment_time) >= ?
              AND TIME(ts.appointment_time) < ?
              AND a.status NOT IN ('CANCELLED', 'COMPLETED')
            LIMIT 1
            """;

        try (var con = database.DBConnection.getConnection();
             var ps = con.prepareStatement(sql)) {

            ps.setInt(1, clinicId);
            ps.setInt(2, sqlDay);
            ps.setTime(3, java.sql.Time.valueOf(from));
            ps.setTime(4, java.sql.Time.valueOf(to));

            return ps.executeQuery().next();
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) clinicNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }



    @FXML
    private void onConsultationToggle() {
        boolean enabled = enableConsultationCheckBox.isSelected();
        consultationFieldsBox.setVisible(enabled);
        consultationFieldsBox.setManaged(enabled);
    }

    /*
    * private void handleBookAppointment(Practitioner selectedDoctor, TimeSlot slot) {
    try {
        Clinic clinic = selectedDoctor.getClinic();
        boolean hasConsultation = clinic.getConsultationPrice() > 0
                               && clinic.getConsultationDurationDays() > 0;

        // جيب آخر كشف (VISIT) مكتمل للمريض مع الدكتور ده
        Appointment lastVisit = appointmentDAO.getLastCompletedVisit(
            currentPatient.getID(),
            selectedDoctor.getID()
        );

        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(currentPatient);
        newAppointment.setPractitioner(selectedDoctor);
        newAppointment.setAppointmentDateTime(slot);
        newAppointment.setClinic(clinic);
        newAppointment.setStatus(Status.Booked);

        if (!hasConsultation || lastVisit == null) {
            // → كشف جديد
            newAppointment.setAppointmentType(AppointmentType.VISIT);
            newAppointment.setPrice(clinic.getPrice()); // سعر الكشف
        } else {
            // تحقق: هل مدة الاستشارة لسه ما انتهتش؟
            LocalDate expiry = lastVisit.getAppointmentDateTime().getDate()
                    .plusDays(clinic.getConsultationDurationDays());

            if (!LocalDate.now().isAfter(expiry)) {
                // → استشارة
                newAppointment.setAppointmentType(AppointmentType.CONSULTATION);
                newAppointment.setPrice(clinic.getConsultationPrice());
                newAppointment.setConsultationExpiryDate(expiry); // مهم للإحصاءات
            } else {
                // → كشف جديد (انتهت المدة)
                newAppointment.setAppointmentType(AppointmentType.VISIT);
                newAppointment.setPrice(clinic.getPrice());
            }
        }

        // احفظ الموعد
        appointmentDAO.saveAppointment(newAppointment);
        showAlert("Success", "Appointment booked successfully!");

    } catch (Exception ex) {
        ex.printStackTrace();
        showAlert("Error", "Failed to book appointment.");
    }
}*/
}