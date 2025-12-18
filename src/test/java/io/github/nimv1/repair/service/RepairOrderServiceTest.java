package io.github.nimv1.repair.service;

import io.github.nimv1.repair.entity.RepairOrder;
import io.github.nimv1.repair.entity.User;
import io.github.nimv1.repair.repository.RepairOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository orderRepository;

    @InjectMocks
    private RepairOrderService orderService;

    private RepairOrder testOrder;
    private User testManager;
    private User testTechnician;

    @BeforeEach
    void setUp() {
        testOrder = RepairOrder.builder()
                .id(1L)
                .clientName("Тестовый Клиент")
                .clientPhone("+7 999 123-45-67")
                .applianceType("Холодильник")
                .problemDescription("Не охлаждает")
                .status(RepairOrder.OrderStatus.NEW)
                .priority(RepairOrder.Priority.NORMAL)
                .build();

        testManager = User.builder()
                .id(1L)
                .username("manager")
                .fullName("Менеджер Тест")
                .role(User.Role.MANAGER)
                .build();

        testTechnician = User.builder()
                .id(2L)
                .username("tech")
                .fullName("Техник Тест")
                .role(User.Role.TECHNICIAN)
                .build();
    }

    @Test
    void shouldCreateOrder() {
        when(orderRepository.save(any(RepairOrder.class))).thenReturn(testOrder);

        RepairOrder created = orderService.createOrder(testOrder);

        assertNotNull(created);
        assertNotNull(created.getOrderNumber());
        assertEquals(RepairOrder.OrderStatus.NEW, created.getStatus());
        verify(orderRepository).save(any(RepairOrder.class));
    }

    @Test
    void shouldFindOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Optional<RepairOrder> found = orderService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Тестовый Клиент", found.get().getClientName());
    }

    @Test
    void shouldAcceptOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(i -> i.getArgument(0));

        RepairOrder accepted = orderService.acceptOrder(1L, testManager);

        assertEquals(RepairOrder.OrderStatus.ACCEPTED, accepted.getStatus());
        assertEquals(testManager, accepted.getManager());
    }

    @Test
    void shouldAssignTechnician() {
        testOrder.setStatus(RepairOrder.OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(i -> i.getArgument(0));

        RepairOrder assigned = orderService.assignTechnician(1L, testTechnician);

        assertEquals(RepairOrder.OrderStatus.ASSIGNED, assigned.getStatus());
        assertEquals(testTechnician, assigned.getTechnician());
        assertNotNull(assigned.getAssignedAt());
    }

    @Test
    void shouldStartRepair() {
        testOrder.setStatus(RepairOrder.OrderStatus.ASSIGNED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(i -> i.getArgument(0));

        RepairOrder started = orderService.startRepair(1L);

        assertEquals(RepairOrder.OrderStatus.IN_PROGRESS, started.getStatus());
        assertNotNull(started.getStartedAt());
    }

    @Test
    void shouldCompleteRepair() {
        testOrder.setStatus(RepairOrder.OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(i -> i.getArgument(0));

        RepairOrder completed = orderService.completeRepair(
                1L, "Заменён компрессор", "Компрессор XYZ", new BigDecimal("5000"));

        assertEquals(RepairOrder.OrderStatus.COMPLETED, completed.getStatus());
        assertEquals("Заменён компрессор", completed.getRepairNotes());
        assertEquals("Компрессор XYZ", completed.getPartsUsed());
        assertEquals(new BigDecimal("5000"), completed.getFinalCost());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    void shouldCancelOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(i -> i.getArgument(0));

        RepairOrder cancelled = orderService.cancelOrder(1L);

        assertEquals(RepairOrder.OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.acceptOrder(999L, testManager));
    }
}
