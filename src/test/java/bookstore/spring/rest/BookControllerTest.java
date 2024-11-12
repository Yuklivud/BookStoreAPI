package bookstore.spring.rest;

import bookstore.controller.BookController;
import bookstore.entity.Book;
import bookstore.repository.BookRepository;
import bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private BookService bookService;


    @Test
    public void testAddBook_Success() throws Exception {
        Book book = new Book(null, "New Book", "Author", "123456789", 29.99, 10, "A new adventure book");
        when(bookService.addBook(any(Book.class))).thenReturn(book);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Book\",\"author\":\"Author\",\"isbn\":\"123456789\",\"price\":29.99,\"quantity\":10,\"description\":\"A new adventure book\"}"))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("New Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isbn").value("123456789"));
    }

    @Test
    public void testAddBook_Conflict() throws Exception {
        when(bookService.addBook(any(Book.class))).thenThrow(new IllegalArgumentException("Book with this ISBN already exists"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Duplicate Book\",\"author\":\"Author\",\"isbn\":\"123456789\",\"price\":29.99,\"quantity\":10,\"description\":\"Duplicate entry\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateBook_Success() throws Exception {
        Book updatedBook = new Book(1L, "Updated Book", "Author", "123456789", 35.99, 15, "Updated description");
        when(bookService.updateBook(anyLong(), any(Book.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Book\",\"author\":\"Author\",\"isbn\":\"123456789\",\"price\":35.99,\"quantity\":15,\"description\":\"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated Book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(35.99));
    }

    @Test
    public void testUpdateBook_DontChangeISBN() {
        Book originBook = new Book(1L, "Origin Book", "Author", "9781234567897", 15.10, 10, "Origin description");
        Book updatedBook = new Book(1L, "Updated Book", "Author", "123456789", 35.99, 15, "Updated description");

        when(bookRepository.findById(originBook.getId())).thenReturn(Optional.of(originBook));

        bookService.updateBook(originBook.getId(), updatedBook);

        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    public void testDeleteBook_Success() throws Exception {
        Mockito.doNothing().when(bookService).deleteBook(anyLong());

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteBook_NotFound() throws Exception {
        Mockito.doThrow(new NoSuchElementException("Book not found")).when(bookService).deleteBook(anyLong());

        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
    }
}
