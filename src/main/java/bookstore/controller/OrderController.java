package bookstore.controller;

import bookstore.dto.OrderRequest;
import bookstore.entity.Book;
import bookstore.entity.Order;
import bookstore.service.BookService;
import bookstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final BookService bookService;

    @Autowired
    public OrderController(OrderService orderService, BookService bookService) {
        this.orderService = orderService;
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest) {
        try {

            Order order = orderService.createOrder(orderRequest.getCustomerId(), orderRequest.getBookId(), orderRequest.getQuantity());

            Book book = bookService.getBookById(orderRequest.getBookId());

            if (book == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (book.getQuantity() < orderRequest.getQuantity()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            book.setQuantity(book.getQuantity() - orderRequest.getQuantity());

            bookService.updateBook(book.getId(), book);

            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId,
                                                   @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }
}
