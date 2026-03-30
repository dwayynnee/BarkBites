package models;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents an order in the BarkBites system
 * Maps to the 'orders' Firestore collection
 */
public class Order {
    private String id;              // Document ID in Firestore
    private String student_id;      // Student who placed the order
    private List<Map<String, Object>> items;  // Array of items: [{menu_item_id, quantity, price}, ...]
    private double total_price;     // Total order cost
    private String status;          // "pending", "in_progress", "ready", "completed", "cancelled"
    private Instant created_at;     // When order was placed
    private Instant ready_at;       // When staff marked it ready (null if not ready yet)
    private Instant picked_up_at;   // When student picked it up (null if not picked up)
    private String order_number;    // Human-readable order ID for display (e.g., "#001")

    // Constructor
    public Order() {
    }

    public Order(String student_id, List<Map<String, Object>> items, double total_price) {
        this.student_id = student_id;
        this.items = items;
        this.total_price = total_price;
        this.status = "pending";
        this.created_at = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getReady_at() {
        return ready_at;
    }

    public void setReady_at(Instant ready_at) {
        this.ready_at = ready_at;
    }

    public Instant getPicked_up_at() {
        return picked_up_at;
    }

    public void setPicked_up_at(Instant picked_up_at) {
        this.picked_up_at = picked_up_at;
    }

    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", student_id='" + student_id + '\'' +
                ", total_price=" + total_price +
                ", status='" + status + '\'' +
                ", order_number='" + order_number + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}
