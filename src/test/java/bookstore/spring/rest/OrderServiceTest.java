package bookstore.spring.rest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import bookstore.dto.OrderRequest;
import bookstore.entity.Book;
import bookstore.entity.Order;
import bookstore.repository.OrderRepository;
import bookstore.service.BookService;
import bookstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private OrderService orderService;

    private Book mockBook;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockBook = new Book();
        mockBook.setId(101L);
        mockBook.setTitle("Book Title");
        mockBook.setAuthor("Author");
        mockBook.setIsbn("1234567890");
        mockBook.setQuantity(10);
        mockBook.setPrice(20.0);
        mockBook.setDescription("A great book");

        when(bookService.getBookById(101L)).thenReturn(mockBook);
    }

    @Test
    public void testCreateOrder_Success() {
        OrderRequest orderRequest = new OrderRequest(1L, 1L, 2);

        Order mockOrder = new Order(1L, 1L, 1L, 2, "on processing", LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(bookService.getBookById(anyLong())).thenReturn(mockBook);

        Order createdOrder = orderService.createOrder(orderRequest.getCustomerId(), orderRequest.getBookId(), orderRequest.getQuantity());

        assertNotNull(createdOrder.getId());
        assertEquals(orderRequest.getCustomerId(), createdOrder.getCustomerId());
        assertEquals(orderRequest.getBookId(), createdOrder.getBookId());
        assertEquals(orderRequest.getQuantity(), createdOrder.getQuantity());
    }

    @Test
    public void testCreateOrder_BookNotFound() {
        when(bookService.getBookById(101L)).thenReturn(null);

        OrderRequest orderRequest = new OrderRequest(1L, 101L, 2);

        assertThrows(NoSuchElementException.class, () -> {
            orderService.createOrder(orderRequest.getCustomerId(), orderRequest.getBookId(), orderRequest.getQuantity());
        });
    }

    @Test
    public void testCreateOrder_InsufficientStock() {
        mockBook.setQuantity(1);

        when(bookService.getBookById(101L)).thenReturn(mockBook);

        OrderRequest orderRequest = new OrderRequest(1L, 101L, 2);

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(orderRequest.getCustomerId(), orderRequest.getBookId(), orderRequest.getQuantity());
        });
    }
    @Test
    public void testUpdateOrderStatus_Success() {
        Order existingOrder = new Order(1L, 1L, 1L, 2, "on processing", LocalDateTime.now());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        Order updatedOrder = orderService.updateOrderStatus(1L, "sent");

        assertEquals("sent", updatedOrder.getStatus());
    }

    @Test
    public void testUpdateOrderStatus_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            orderService.updateOrderStatus(1L, "sent");
        });
    }
    @Test
    public void testGetOrdersByCustomerId_Success() {
        Order order1 = new Order(1L, 1L, 1L, 2, "on processing", LocalDateTime.now());
        Order order2 = new Order(2L, 1L, 1L, 2, "on processing", LocalDateTime.now());
        List<Order> customerOrders = Arrays.asList(order1, order2);

        when(orderRepository.findByCustomerId(1L)).thenReturn(customerOrders);

        List<Order> orders = orderService.getOrdersByCustomerId(1L);

        assertEquals(2, orders.size());
        assertEquals(1L, orders.get(0).getCustomerId());
    }

    @Test
    public void testGetOrdersByCustomerId_NoOrders() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(Collections.emptyList());

        List<Order> orders = orderService.getOrdersByCustomerId(1L);

        assertTrue(orders.isEmpty());
    }

}
