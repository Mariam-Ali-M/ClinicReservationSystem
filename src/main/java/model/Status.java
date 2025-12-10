package model;

public enum Status {
    Booked("Booked"),
    Cancelled_by_Patient("Cancelled by Patient"),
    Cancelled_by_Doctor("Cancelled by Doctor"),
    Completed("Completed");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    // ✅ للقراءة من الداتابيز (يستخدم الاسم القديم)
    public static Status fromDatabase(String dbValue) {
        if (dbValue == null) return Booked;
        switch (dbValue) {
            case "Booked": return Booked;
            case "Cancelled": return Cancelled_by_Patient; // ← السر هنا!
            case "Completed": return Completed;
            default: return Booked;
        }
    }

    // ✅ للكتابة في الداتابيز (يرجع الاسم القديم)
    public String toDatabaseValue() {
        switch (this) {
            case Booked: return "Booked";
            case Cancelled_by_Patient:
            case Cancelled_by_Doctor: return "Cancelled"; // ← كلاهما يُخزن كـ "Cancelled"
            case Completed: return "Completed";
            default: return "Booked";
        }
    }

    // ✅ للعرض في الـ UI والتقرير (يظهر الاسم الواضح)
    @Override
    public String toString() {
        return displayName;
    }
}