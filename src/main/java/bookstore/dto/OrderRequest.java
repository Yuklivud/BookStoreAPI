package bookstore.dto;

public class OrderRequest {
    private Long customerId;
    private Long bookId;
    private int quantity;

    public OrderRequest(Long customerId, Long bookId, int quantity) {
        this.customerId = customerId;
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public OrderRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
