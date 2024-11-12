package bookstore.spring.rest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bookstore.dto.OrderRequest;
import bookstore.entity.Book;
import bookstore.entity.Order;
import bookstore.service.BookService;
import bookstore.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private OrderService orderService;

    @Test
    public void testOrderBook_FailWhenQuantityExceedsStock() throws Exception {
        Book book = new Book(1L, "Sample Book", "Author", "ISBN123", 100.0, 5, "Description");

        when(bookService.getBookById(book.getId())).thenReturn(book);
        when(orderService.createOrder(1L, book.getId(), 10)).thenThrow(new IllegalArgumentException("Not enough books in stock"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .param("customerId", "1")
                        .param("bookId", "1")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testOrderBook_SuccessWhenInStock() throws Exception {
        Book book = new Book(1L, "Sample Book", "Author", "ISBN123", 100.0, 10, "Description");
        when(bookService.getBookById(book.getId())).thenReturn(book);

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setBookId(book.getId());
        order.setQuantity(5);

        when(orderService.createOrder(1L, book.getId(), 5)).thenReturn(order);

        OrderRequest orderRequest = new OrderRequest(1L, 1L, 5);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testOrderBook_FailWhenBookNotInInventory() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(null);

        OrderRequest orderRequest = new OrderRequest(1L, 1L, 1);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound());
    }



    @Test
    public void testInventoryReductionAfterPurchase() throws Exception {
        Book book = new Book(1L, "Sample Book", "Author", "ISBN123", 100.0, 10, "Description");

        when(bookService.getBookById(book.getId())).thenReturn(book);

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setBookId(book.getId());
        order.setQuantity(3);

        when(orderService.createOrder(1L, book.getId(), 3)).thenReturn(order);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"bookId\":1,\"quantity\":3}"))
                .andExpect(status().isCreated());

        verify(bookService).updateBook(eq(book.getId()), any(Book.class));

        assertEquals(7, book.getQuantity());
    }
}

