package bookstore.service;

import bookstore.entity.Book;
import bookstore.entity.Order;
import bookstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookService bookService;

    @Autowired
    public OrderService(OrderRepository orderRepository, BookService bookService) {
        this.orderRepository = orderRepository;
        this.bookService = bookService;
    }

    public Order createOrder(Long customerId, Long bookId, Integer quantity) {
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new NoSuchElementException("Book with ID " + bookId + " not found.");
        }

        if (book.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough quantity.");
        }

        book.setQuantity(book.getQuantity() - quantity);
        bookService.updateBook(bookId, book);

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setBookId(bookId);
        order.setQuantity(quantity);
        order.setStatus("on processing");
        order.setOrderDate(LocalDateTime.now());


        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order with ID " + orderId + " not found."));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order with ID " + orderId + " not found."));
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}
