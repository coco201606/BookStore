package entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityModelTest {
    @Test
    void bookDataConstructorAndPropertiesPreserveValues() {
        byte[] thumbnail = new byte[]{1, 2};
        byte[] image = new byte[]{3, 4};
        BookData book = new BookData("Jakarta", 1200, Genre.JENRE1, "Description", thumbnail, image);

        assertEquals("Jakarta", book.getTitle());
        assertEquals(Integer.valueOf(1200), book.getPrice());
        assertEquals(Genre.JENRE1, book.getGenre());
        assertEquals("Description", book.getExp());
        assertArrayEquals(thumbnail, book.getImg1());
        assertArrayEquals(image, book.getImg2());

        book.setId(5L);
        book.setTitle("Updated");
        assertEquals(Long.valueOf(5), book.getId());
        assertEquals("Updated", book.getTitle());
    }

    @Test
    void compositeTypeIdEqualityAndHashCodeUseBothKeyParts() {
        CompositeTypeId key = new CompositeTypeId("user", "alice");
        CompositeTypeId sameKey = new CompositeTypeId("user", "alice");

        assertEquals(key, sameKey);
        assertEquals(key.hashCode(), sameKey.hashCode());
        assertNotEquals(key, new CompositeTypeId("admin", "alice"));
        assertNotEquals(key, new CompositeTypeId("user", "bob"));
        assertFalse(key.equals(null));
        assertFalse(key.equals("user"));
    }

    @Test
    void genreContainsTheSupportedValues() {
        assertArrayEquals(
                new Genre[]{Genre.NONE, Genre.JENRE1, Genre.JENRE2, Genre.JENRE3},
                Genre.values());
    }

    @Test
    void orderBooksConstructorAndPropertiesPreserveValues() {
        UserAccount account = new UserAccount();
        Date orderedAt = new Date(1234L);
        List<OrderState> items = new ArrayList<>();
        OrderBooks order = new OrderBooks(
                account, "Alice", "alice@example.com", 2, "Gift wrap",
                orderedAt, 2400, items);

        assertSame(account, order.getAccount());
        assertEquals("Alice", order.getCust_name());
        assertEquals("alice@example.com", order.getCust_mail());
        assertEquals(Integer.valueOf(2), order.getCust_payment());
        assertEquals("Gift wrap", order.getCust_demand());
        assertSame(orderedAt, order.getPo_date());
        assertEquals(Integer.valueOf(2400), order.getTotal_amount());
        assertSame(items, order.getOrderState());

        order.setId(9L);
        assertEquals(Long.valueOf(9), order.getId());
    }

    @Test
    void orderStateAdjustsQuantityWithoutGoingBelowZero() {
        BookData book = new BookData();
        OrderState order = new OrderState(book, 1);

        assertSame(book, order.getBookData());
        assertEquals(2, order.adding());
        assertEquals(1, order.subtraction());
        assertEquals(0, order.subtraction());
        assertEquals(0, order.subtraction());

        order.setId(7L);
        order.setQuantity(3);
        assertEquals(Long.valueOf(7), order.getId());
        assertEquals(Integer.valueOf(3), order.getQuantity());
    }

    @Test
    void typeAndIdConstructorsMaintainKeyAndAccountRelations() {
        UserAccount account = new UserAccount("alice", "hash", "Alice", "a@example.com", null);
        TypeAndId type = new TypeAndId("user", account);

        assertEquals("user", type.getComp_type_id().getUser_type());
        assertEquals("alice", type.getComp_type_id().getUser_id());
        assertSame(account, type.getUser_account());

        CompositeTypeId key = new CompositeTypeId("admin", "root");
        type.setComp_type_id(key);
        assertSame(key, type.getComp_type_id());
        assertTrue(type.getComp_type_id().equals(new CompositeTypeId("admin", "root")));
    }

    @Test
    void userAccountConstructorAndPropertiesPreserveValues() {
        TypeAndId type = new TypeAndId(new CompositeTypeId("user", "alice"), null);
        UserAccount account = new UserAccount("alice", "hash", "Alice", "a@example.com", type);

        assertEquals("alice", account.getId());
        assertEquals("hash", account.getPasswd());
        assertEquals("Alice", account.getName());
        assertEquals("a@example.com", account.getMail());
        assertSame(type, account.getType_id());

        account.setId("bob");
        account.setPasswd("updated");
        assertEquals("bob", account.getId());
        assertEquals("updated", account.getPasswd());
    }
}
