/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.clinicsystem;
import java.util.*;

/**
 *
 * @author noursameh
 */
public class ClinicSystem {
    
    private static final Scanner in = new Scanner(System.in);
    private static  Practitioner cur_Practitioner = null;
    private static  Patient cur_Patient = null;
    private static List<Practitioner> Practitioners;
    private static List<Patient> patients;
    public static void main(String[] args) {
        
        
    }
    
    private static void start() {
        
        while (true) {
            System.out.println("=== Welcome to Weekly Clinic System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            int choice = in.nextInt();
            //scanner.nextLine();

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    Register();
                    break;
                case 3:
                    System.exit(0);
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }
    static void login() {
        
        System.out.println("-- Login --");
        System.out.print("email: ");
        String email = in.nextLine();
        System.out.print("Password: ");
        String password = in.nextLine();

        for (Practitioner doc : Practitioners) {
            if (doc.email.equals(email) && doc.password.equals(password)) {
                cur_Practitioner = doc;
                break;
            }
        }
        if (cur_Practitioner != null) {
            doctorMenu();/////////////////////
            return;
        }

        for (Patient pat : patients) {
            if (pat.email.equals(email) && pat.password.equals(password)) {
                cur_Patient = pat;
                break;
            }
        }

        if (cur_Patient != null) {
            patientMenu(); ///////////////////
        } else {
            System.out.println("Invalid login ❌");
        }
        
}

    private static void Register() {
        
        System.out.println("-- Register --");
        System.out.println("Choose Role: ");
        System.out.println("1. Doctor");
        System.out.println("2. Patient");
        System.out.print("> ");
        int role = in.nextInt();
        in.nextLine();

        while(role != 1 && role != 2) {
            System.out.println("❌ Invalid role! Must be 1 or 2.");
            role = in.nextInt();
            in.nextLine();
        }

        System.out.print("Enter name: ");
        String name = in.nextLine().trim();
        while(name.isEmpty() || name.length() < 3) {
            System.out.println("❌ Name must be at least 3 characters.");
            name = in.nextLine().trim();
        }

        System.out.print("Enter phone (Egyptian): ");
        String phone = in.nextLine().trim();
        while(!phone.matches("^(010|011|012|015)[0-9]{8}$")) {
            System.out.println("❌ Invalid phone number format.");
            phone = in.nextLine().trim();
        }

        System.out.print("Enter email: ");
        String email = in.nextLine().trim();
        while(!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            System.out.println("❌ Invalid email format.");
            email = in.nextLine().trim();
        }
        
        while(emailExists(email)) {
            System.out.println("❌ Email already exists.");
            email = in.nextLine().trim();
            while(!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                System.out.println("❌ Invalid email format.");
                email = in.nextLine().trim();
            }   
        }
        
        System.out.print("Enter password: ");
        String password = in.nextLine().trim();
        while (password.length() < 4) {
            System.out.println("❌ Password must be at least 4 characters.");
            password = in.nextLine().trim();
        }

        int newID = generateID(); ///////////////////////////// validithion

        if (role == 1) {
            Clinic clinic = null;
            Practitioner doctor = new Practitioner(newID, name, phone, email, password, clinic);
            Practitioners.add(doctor);
            System.out.println("✅ Doctor registered successfully!");
        } else {
            Patient patient = new Patient(newID, name, phone, email, password);
            patients.add(patient);
            System.out.println("✅ Patient registered successfully!");
        }

        login();
    }
    
    private static boolean emailExists(String email) {
        for (Practitioner d : Practitioners)
            if (d.getEmail().equalsIgnoreCase(email)) return true;
        for (Patient p : patients)
            if (p.getEmail().equalsIgnoreCase(email)) return true;
        return false;
    }
    private static int generateID() {
        return new Random().nextInt(10000) + 1;
    }


}
