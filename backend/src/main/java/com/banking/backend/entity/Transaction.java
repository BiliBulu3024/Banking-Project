package com.banking.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ==========================================
 * Transaction Entity - FYP Banking System
 * ==========================================
 * Dùng để lưu toàn bộ lịch sử giao dịch của tài khoản:
 * - DEPOSIT: Nạp tiền
 * - WITHDRAW: Rút tiền
 * - TRANSFER_IN: Nhận tiền
 * - TRANSFER_OUT: Chuyển tiền
 *
 * Tự động ghi lại thời gian thực hiện bằng @PrePersist.
 * Kết hợp với TransactionRepository để thống kê, báo cáo.
 * ==========================================
 * Author: Lê Thị Trúc Linh
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Loại giao dịch: DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT */
    @Column(nullable = false)
    private String type;

    /** Số tiền giao dịch */
    @Column(nullable = false)
    private double amount;

    /** Ghi chú / mô tả giao dịch */
    private String description;

    /** Tài khoản đối tác (nếu là chuyển khoản) */
    @Column(nullable = true)
    private String targetAccount;  // nullable = true là đúng chuẩn

    /** Thời điểm thực hiện giao dịch */
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /** Liên kết đến Account thực hiện giao dịch */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Gán timestamp tự động khi lưu */
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
