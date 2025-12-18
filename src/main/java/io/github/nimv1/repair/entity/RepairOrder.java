package io.github.nimv1.repair.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Заявка на ремонт бытовой техники.
 */
@Entity
@Table(name = "repair_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    // Информация о клиенте
    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String clientPhone;

    private String clientEmail;

    private String clientAddress;

    // Информация о технике
    @Column(nullable = false)
    private String applianceType;  // Тип техники (холодильник, стиральная машина и т.д.)

    private String applianceBrand;  // Бренд

    private String applianceModel;  // Модель

    private String serialNumber;    // Серийный номер

    // Описание проблемы
    @Column(length = 2000)
    private String problemDescription;

    // Статус заявки
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Приоритет
    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Назначенный техник
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    // Менеджер, принявший заявку
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    // Финансовая информация
    private BigDecimal estimatedCost;  // Предварительная стоимость

    private BigDecimal finalCost;      // Итоговая стоимость

    // Даты
    private LocalDateTime createdAt;

    private LocalDateTime assignedAt;

    private LocalDateTime scheduledAt;  // Запланированная дата визита

    private LocalDateTime startedAt;    // Начало ремонта

    private LocalDateTime completedAt;  // Завершение ремонта

    // Результат ремонта
    @Column(length = 2000)
    private String repairNotes;

    @Column(length = 1000)
    private String partsUsed;  // Использованные запчасти

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.NEW;
        }
        if (priority == null) {
            priority = Priority.NORMAL;
        }
    }

    public enum OrderStatus {
        NEW,            // Новая заявка
        ACCEPTED,       // Принята менеджером
        ASSIGNED,       // Назначен техник
        SCHEDULED,      // Запланирован визит
        IN_PROGRESS,    // В работе
        WAITING_PARTS,  // Ожидание запчастей
        COMPLETED,      // Завершена
        CANCELLED       // Отменена
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
