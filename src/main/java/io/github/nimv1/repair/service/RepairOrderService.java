package io.github.nimv1.repair.service;

import io.github.nimv1.repair.entity.RepairOrder;
import io.github.nimv1.repair.entity.User;
import io.github.nimv1.repair.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для работы с заявками на ремонт.
 */
@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderRepository orderRepository;
    private final AtomicLong orderCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public RepairOrder createOrder(RepairOrder order) {
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(RepairOrder.OrderStatus.NEW);
        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "REP-" + date + "-" + String.format("%05d", orderCounter.incrementAndGet());
    }

    public Optional<RepairOrder> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<RepairOrder> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Page<RepairOrder> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public List<RepairOrder> findByStatus(RepairOrder.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Page<RepairOrder> findByTechnician(Long technicianId, Pageable pageable) {
        return orderRepository.findByTechnicianId(technicianId, pageable);
    }

    public Page<RepairOrder> findByManager(Long managerId, Pageable pageable) {
        return orderRepository.findByManagerId(managerId, pageable);
    }

    public List<RepairOrder> findNewOrders() {
        return orderRepository.findByStatus(RepairOrder.OrderStatus.NEW);
    }

    public List<RepairOrder> findActiveOrdersForTechnician(Long technicianId) {
        return orderRepository.findByTechnicianIdAndStatusIn(
                technicianId,
                List.of(RepairOrder.OrderStatus.ASSIGNED,
                        RepairOrder.OrderStatus.SCHEDULED,
                        RepairOrder.OrderStatus.IN_PROGRESS,
                        RepairOrder.OrderStatus.WAITING_PARTS)
        );
    }

    @Transactional
    public RepairOrder acceptOrder(Long orderId, User manager) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.ACCEPTED);
        order.setManager(manager);
        return orderRepository.save(order);
    }

    @Transactional
    public RepairOrder assignTechnician(Long orderId, User technician) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.ASSIGNED);
        order.setTechnician(technician);
        order.setAssignedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public RepairOrder scheduleVisit(Long orderId, LocalDateTime scheduledAt) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.SCHEDULED);
        order.setScheduledAt(scheduledAt);
        return orderRepository.save(order);
    }

    @Transactional
    public RepairOrder startRepair(Long orderId) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.IN_PROGRESS);
        order.setStartedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public RepairOrder completeRepair(Long orderId, String repairNotes, String partsUsed, java.math.BigDecimal finalCost) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setRepairNotes(repairNotes);
        order.setPartsUsed(partsUsed);
        order.setFinalCost(finalCost);
        return orderRepository.save(order);
    }

    @Transactional
    public RepairOrder cancelOrder(Long orderId) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Transactional
    public RepairOrder setWaitingParts(Long orderId) {
        RepairOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        order.setStatus(RepairOrder.OrderStatus.WAITING_PARTS);
        return orderRepository.save(order);
    }

    // Статистика
    public long countByStatus(RepairOrder.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public long countActiveOrdersForTechnician(Long technicianId) {
        return orderRepository.countByTechnicianIdAndStatus(technicianId, RepairOrder.OrderStatus.IN_PROGRESS);
    }
}
