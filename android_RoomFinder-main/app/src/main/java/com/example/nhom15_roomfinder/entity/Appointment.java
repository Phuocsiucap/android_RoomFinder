package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

/**
 * Appointment Entity - Lịch hẹn xem phòng
 */
public class Appointment implements Serializable {
    
    public static final String STATUS_PENDING = "PENDING";      // Đang chờ xác nhận
    public static final String STATUS_ACCEPTED = "ACCEPTED";    // Đã chấp nhận
    public static final String STATUS_REJECTED = "REJECTED";    // Đã từ chối
    public static final String STATUS_CANCELLED = "CANCELLED";  // Đã hủy
    public static final String STATUS_COMPLETED = "COMPLETED";  // Đã hoàn thành
    
    private String id;
    private String roomId;           // ID phòng
    private String roomTitle;        // Tiêu đề phòng
    private String roomThumbnail;    // Ảnh phòng
    private String ownerId;          // Chủ phòng
    private String ownerName;
    private String ownerPhone;
    private String requesterId;      // Người yêu cầu xem phòng
    private String requesterName;
    private String requesterPhone;
    private long appointmentDate;    // Ngày hẹn (timestamp)
    private String appointmentTime;  // Giờ hẹn (VD: "14:00")
    private String note;             // Ghi chú
    private String status;           // Trạng thái
    private String rejectReason;     // Lý do từ chối (nếu có)
    private long createdAt;
    private long updatedAt;

    // Constructor mặc định (Firebase)
    public Appointment() {
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
    }

    public Appointment(String roomId, String ownerId, String requesterId) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.requesterId = requesterId;
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }

    public String getRoomThumbnail() { return roomThumbnail; }
    public void setRoomThumbnail(String roomThumbnail) { this.roomThumbnail = roomThumbnail; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }

    public long getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(long appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }
    
    public boolean isAccepted() {
        return STATUS_ACCEPTED.equals(status);
    }
    
    public boolean isRejected() {
        return STATUS_REJECTED.equals(status);
    }
}
