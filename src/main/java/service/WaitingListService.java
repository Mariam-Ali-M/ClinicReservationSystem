/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.WaitingListDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.DBConnection;
import model.WaitingList;


public class WaitingListService {

    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    public void addPatient(WaitingList item) throws SQLException {
        waitingListDAO.add(item);
        item.getClinic().getWaitingList().add(item);
    }

    public void removePatient(WaitingList item) throws SQLException {
        waitingListDAO.delete(item.getId());
        item.getClinic().getWaitingList().remove(item);
    }

    public List<WaitingList> getPatientWaitingList(int patientId) throws SQLException {

        return waitingListDAO.getPatientPendingRequests(patientId);
    }

    public boolean existsPendingRequest(int patientId, int clinicId, LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM WaitingList WHERE patient_id = ? AND clinic_id = ? AND date = ? AND status = 'PENDING' LIMIT 1";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, patientId);
            ps.setInt(2, clinicId);
            ps.setDate(3, java.sql.Date.valueOf(date));

            rs = ps.executeQuery();


            return rs.next();

        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            DBConnection.closeConnection(con);
        }
    }

    public boolean isPatientAlreadyWaiting(int patientId, int clinicId, LocalDate selectedDate) throws SQLException {

        return waitingListDAO.existsPendingRequest(patientId, clinicId, selectedDate);
    }

}

