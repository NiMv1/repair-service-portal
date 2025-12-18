package io.github.nimv1.repair.repository;

import io.github.nimv1.repair.entity.RepairOrder;
import io.github.nimv1.repair.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с заявками на ремонт.
 */
@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    Optional<RepairOrder> findByOrderNumber(String orderNumber);

    List<RepairOrder> findByStatus(RepairOrder.OrderStatus status);

    List<RepairOrder> findByTechnician(User technician);

    List<RepairOrder> findByManager(User manager);

    Page<RepairOrder> findByTechnicianId(Long technicianId, Pageable pageable);

    Page<RepairOrder> findByManagerId(Long managerId, Pageable pageable);

    @Query("SELECT r FROM RepairOrder r WHERE r.status IN :statuses")
    List<RepairOrder> findByStatusIn(@Param("statuses") List<RepairOrder.OrderStatus> statuses);

    @Query("SELECT r FROM RepairOrder r WHERE r.technician.id = :technicianId AND r.status IN :statuses")
    List<RepairOrder> findByTechnicianIdAndStatusIn(
            @Param("technicianId") Long technicianId,
            @Param("statuses") List<RepairOrder.OrderStatus> statuses);

    @Query("SELECT COUNT(r) FROM RepairOrder r WHERE r.status = :status")
    long countByStatus(@Param("status") RepairOrder.OrderStatus status);

    @Query("SELECT COUNT(r) FROM RepairOrder r WHERE r.technician.id = :technicianId AND r.status = :status")
    long countByTechnicianIdAndStatus(
            @Param("technicianId") Long technicianId,
            @Param("status") RepairOrder.OrderStatus status);

    List<RepairOrder> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM RepairOrder r WHERE r.clientPhone = :phone ORDER BY r.createdAt DESC")
    List<RepairOrder> findByClientPhone(@Param("phone") String phone);
}
