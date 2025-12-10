package service;

import dao.AppointmentDAO;
import dao.TimeSlotDAO;
import java.sql.SQLException;
import java.util.List;
import model.Appointment;
import model.Patient;
import model.Status;
import model.TimeSlot;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private ClinicService clinicService;


    public void setClinicService(ClinicService clinicService) {
        this.clinicService = clinicService;
    }


    public boolean isBooked(Patient patient, TimeSlot slot) {
        if (patient == null || slot == null) return false;

        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patient.getID());

            for (Appointment a : appointments) {
                if (a.getAppointmentDateTime().equals(slot)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking booked status: " + e.getMessage());
        }

        return false;
    }

    // ✅ إلغاء عام (يدعم التمرير)
    public void cancel(Appointment appointment, Status cancelStatus) throws SQLException {
        if (appointment == null || appointment.getAppointmentDateTime() == null) return;

        appointment.setStatus(cancelStatus); // ← هنا نستخدم الـ status المرسل
        appointment.getAppointmentDateTime().markAsCancelled();

        appointmentDAO.update(appointment);
        timeSlotDAO.update(appointment.getAppointmentDateTime());

        if (appointment.getClinic() != null) {
            appointment.getClinic().getAppointments().remove(appointment);
        }
        if (appointment.getPatient() != null) {
            appointment.getPatient().getAppointmentList().remove(appointment);
        }

        // إعلام قائمة الانتظار (لو مطلوب)
        if (clinicService != null && appointment.getClinic() != null) {
            clinicService.notifyWaitingList(appointment.getClinic(), appointment.getAppointmentDateTime());
        }
    }

    // ✅ إلغاء من المريض (افتراضي — زي ما كان)
    public void cancel(Appointment appointment) throws SQLException {
        cancel(appointment, Status.Cancelled_by_Patient); // ← default
    }

    // ✅ إلغاء من الدكتور
    public void cancelByDoctor(Appointment appointment) throws SQLException {
        cancel(appointment, Status.Cancelled_by_Doctor);
    }

    // ✅ إلغاء بدون إشعار
    public void cancelWithoutNotify(Appointment appointment, Status cancelStatus) throws SQLException {
        if (appointment == null || appointment.getAppointmentDateTime() == null) return;

        appointment.setStatus(cancelStatus);
        appointment.getAppointmentDateTime().markAsCancelled();

        appointmentDAO.update(appointment);
        timeSlotDAO.update(appointment.getAppointmentDateTime());

        if (appointment.getClinic() != null) {
            appointment.getClinic().getAppointments().remove(appointment);
        }
        if (appointment.getPatient() != null) {
            appointment.getPatient().getAppointmentList().remove(appointment);
        }
    }

    // ✅ إلغاء بدون إشعار (من المريض — افتراضي)
    public void cancelWithoutNotify(Appointment appointment) throws SQLException {
        cancelWithoutNotify(appointment, Status.Cancelled_by_Patient);
    }

    // إعادة جدولة موعد
    public boolean reschedule(Appointment appointment, TimeSlot newSlot) throws SQLException {
        if (appointment == null || newSlot == null || appointment.getClinic() == null) return false;
        if (newSlot.isBooked()) return false;

        TimeSlot oldSlot = appointment.getAppointmentDateTime();
        oldSlot.markAsAvailable();
        timeSlotDAO.update(oldSlot);

        appointment.setAppointmentDateTime(newSlot);
        newSlot.markAsBooked();
        timeSlotDAO.update(newSlot);

        appointmentDAO.update(appointment);

        if (clinicService != null) {
            clinicService.notifyWaitingList(appointment.getClinic(), newSlot);
        }

        return true;
    }
}
