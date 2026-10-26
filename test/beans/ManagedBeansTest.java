package beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import entity.BookData;
import entity.Genre;
import entity.OrderBooks;
import entity.OrderState;
import entity.TypeAndId;
import entity.UserAccount;
import enterprise.BookDataFacade;
import enterprise.FetchBookData;
import enterprise.FetchOrderBooks;
import enterprise.FetchUserAccount;
import enterprise.OrderBooksFacade;
import enterprise.UserAccountFacade;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import utility.Pagenate;

class ManagedBeansTest {
    @Test
    void baseBeanInitializationCreatesSelectionMapsCartAndInitialPage() {
        BaseBean bean = new BaseBean();
        bean.bookDataFacade = new StubBookDataFacade(12);
        bean.pagenate = new Pagenate();

        bean.init();

        assertEquals(12, bean.pagenate.getNumOfData());
        assertEquals(3, bean.pagenate.getMaxPage());
        assertEquals(Integer.valueOf(1), bean.getSortItems().get("なし"));
        assertEquals(Integer.valueOf(3), bean.getSortItems().get("高い順番"));
        assertEquals(Genre.NONE, bean.getGenreItems().get("すべて"));
        assertEquals(Genre.JENRE3, bean.getGenreItems().get("ハウツー"));
        assertEquals(Integer.valueOf(1), bean.getPayItem().get("着払い"));
        assertEquals(Collections.emptyList(), bean.getCart());
    }

    @Test
    void loginBeanHandlesSuccessAlreadyAuthenticatedFailureAndLogout() {
        TestLoginBean bean = new TestLoginBean();
        bean.log = Logger.getLogger("login-test");
        bean.setUsername("alice");
        bean.setPassword("secret");

        RequestState successState = new RequestState();
        bean.request = request(successState);
        assertEquals("/index.xhtml?faces-redirect=true", bean.login());
        assertEquals("alice", successState.loginUser);
        assertEquals("secret", successState.loginPassword);

        RequestState authenticatedState = new RequestState();
        authenticatedState.principal = new Principal() {
            @Override
            public String getName() {
                return "alice";
            }
        };
        bean.request = request(authenticatedState);
        assertNull(bean.login());
        assertEquals("すでにサインインしています", bean.errorMessage);

        RequestState failedState = new RequestState();
        failedState.failLogin = true;
        bean.request = request(failedState);
        assertNull(bean.login());
        assertEquals("サインインできません", bean.errorMessage);

        RequestState logoutState = new RequestState();
        bean.request = request(logoutState);
        assertNull(bean.logout());
        assertEquals("サインインしていません", bean.errorMessage);
    }

    @Test
    void registerBeanRejectsDuplicateIdsAndPersistsNewHashedAccount() {
        TestRegisterBean duplicate = new TestRegisterBean();
        duplicate.setId("alice");
        duplicate.fetchUserAccount = new StubFetchUserAccount(1);
        StubUserAccountFacade duplicateFacade = new StubUserAccountFacade();
        duplicate.entity = duplicateFacade;

        assertNull(duplicate.registerAccount());
        assertEquals("指定したユーザーはすでに使用されています。", duplicate.message);
        assertNull(duplicateFacade.created);

        TestRegisterBean registration = new TestRegisterBean();
        registration.setId("bob");
        registration.setPasswd("password");
        registration.setName("Bob");
        registration.setMail("bob@example.com");
        registration.fetchUserAccount = new StubFetchUserAccount(0);
        StubUserAccountFacade accountFacade = new StubUserAccountFacade();
        registration.entity = accountFacade;

        assertEquals("index?faces-redirect=true", registration.registerAccount());
        assertEquals("bob", accountFacade.created.getId());
        assertEquals("Bob", accountFacade.created.getName());
        assertEquals("bob@example.com", accountFacade.created.getMail());
        assertEquals(
                new utility.SHA256Encoder().encodeBySHA256("password"),
                accountFacade.created.getPasswd());
        assertEquals("user", accountFacade.created.getType_id().getComp_type_id().getUser_type());
        assertSame(accountFacade.created, accountFacade.created.getType_id().getUser_account());
    }

    @Test
    void orderHandlerManagesCartAndNavigation() {
        TestOrderHandler handler = new TestOrderHandler();
        handler.cart = new ArrayList<>();
        BookData book = new BookData();
        book.setId(3L);
        handler.setDisp_detail(book);

        assertEquals("index", handler.showCart());
        assertEquals("ショッピングカートに商品はありません。", handler.message);
        assertEquals("detail?faces-redirect=true", handler.detail(book));
        assertSame(book, handler.getDisp_detail());
        assertEquals("cart?faces-redirect=true", handler.addToCart());
        assertEquals(1, handler.cart.size());
        assertEquals(Integer.valueOf(1), handler.cart.get(0).getQuantity());

        OrderState existing = handler.addNewOrder();
        assertSame(handler.cart.get(0), existing);
        assertEquals(Integer.valueOf(2), existing.getQuantity());
        assertEquals("cart?faces-redirect=true", handler.showCart());
        assertEquals("orderForm?faces-redirect=true", handler.redirectOrderPage());
        assertNull(handler.deleteOrder(existing));
        assertTrue(handler.cart.isEmpty());
        assertEquals("cart", handler.redirectOrderPage());
        assertEquals("現在、カートはからです。", handler.message);
    }

    @Test
    void orderHandlerLoadsBooksAndResetsPagination() {
        TestOrderHandler handler = new TestOrderHandler();
        handler.pagenate = new Pagenate();
        handler.pagenate.initPagination(12, 5);
        handler.fetchBookData = new StubFetchBookData();
        handler.setSelectedGenreItem(Genre.JENRE2);
        handler.setSelectedSortItgem(2);

        assertSame(((StubFetchBookData) handler.fetchBookData).expectedBooks, handler.getBookRecord());
        assertEquals(2, ((StubFetchBookData) handler.fetchBookData).lastSort);
        assertEquals(Genre.JENRE2, ((StubFetchBookData) handler.fetchBookData).lastGenre);

        handler.resetRecordsNumber();
        assertEquals(6, handler.pagenate.getNumOfData());
        assertEquals(2, handler.pagenate.getMaxPage());
    }

    @Test
    void orderHandlerPersistsOrderTotalAndClearsCartForGuestCheckout() {
        TestOrderHandler handler = new TestOrderHandler();
        UserAccount account = new UserAccount("alice", "hash", "Alice", "a@example.com", null);
        StubUserAccountFacade accounts = new StubUserAccountFacade();
        accounts.found = account;
        StubOrderBooksFacade orders = new StubOrderBooksFacade();
        handler.userAccountFacade = accounts;
        handler.orderBooksFacade = orders;
        handler.cart = new ArrayList<>();
        handler.cart.add(new OrderState(new BookData("First", 1000, Genre.JENRE1, "", null, null), 2));
        handler.cart.add(new OrderState(new BookData("Second", 500, Genre.JENRE2, "", null, null), 3));
        handler.setCustomer_name("Alice");
        handler.setCustomer_mail("a@example.com");
        handler.setCustomer_msg("Please deliver carefully");
        handler.setSelectedPayItem(2);

        handler.registerOrder("alice");

        assertEquals(3500, orders.created.getTotal_amount());
        assertSame(account, orders.created.getAccount());
        assertEquals("Alice", orders.created.getCust_name());
        assertEquals("a@example.com", orders.created.getCust_mail());
        assertEquals("Please deliver carefully", orders.created.getCust_demand());
        assertEquals(Integer.valueOf(2), orders.created.getCust_payment());
        assertSame(handler.cart, orders.created.getOrderState());

        handler.userId = null;
        assertEquals("confirm?faces-redirect=true", handler.buy());
        assertEquals("guest", handler.registeredUser);
        assertTrue(handler.cart.isEmpty());
        assertNull(handler.getCustomer_name());
        assertNull(handler.getCustomer_mail());
        assertNull(handler.getCustomer_msg());
    }

    @Test
    void orderHandlerReturnsOrderHistoryForTheCurrentUser() {
        TestOrderHandler handler = new TestOrderHandler();
        List<OrderBooks> history = Collections.singletonList(new OrderBooks());
        handler.fetchOrderBooks = new StubFetchOrderBooks(history);
        handler.userId = "alice";

        assertSame(history, handler.getOrderHistory());
        assertEquals("alice", ((StubFetchOrderBooks) handler.fetchOrderBooks).lastUserId);
    }

    private static HttpServletRequest request(final RequestState state) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("getUserPrincipal".equals(name)) {
                            return state.principal;
                        }
                        if ("getMethod".equals(name)) {
                            return "POST";
                        }
                        if ("getContextPath".equals(name)) {
                            return "/bookstore";
                        }
                        if ("getQueryString".equals(name)) {
                            return null;
                        }
                        if ("getRemoteUser".equals(name)) {
                            return state.principal == null ? null : state.principal.getName();
                        }
                        if ("getAuthType".equals(name)) {
                            return "FORM";
                        }
                        if ("getRequestURI".equals(name)) {
                            return "/bookstore/login";
                        }
                        if ("getRequestURL".equals(name)) {
                            return new StringBuffer("http://localhost/bookstore/login");
                        }
                        if ("login".equals(name)) {
                            state.loginUser = (String) args[0];
                            state.loginPassword = (String) args[1];
                            if (state.failLogin) {
                                throw new ServletException("Rejected credentials");
                            }
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    }
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == Void.TYPE) {
            return null;
        }
        if (type == Boolean.TYPE) {
            return false;
        }
        if (type == Character.TYPE) {
            return '\0';
        }
        if (type == Byte.TYPE) {
            return (byte) 0;
        }
        if (type == Short.TYPE) {
            return (short) 0;
        }
        if (type == Integer.TYPE) {
            return 0;
        }
        if (type == Long.TYPE) {
            return 0L;
        }
        if (type == Float.TYPE) {
            return 0F;
        }
        if (type == Double.TYPE) {
            return 0D;
        }
        return null;
    }

    private static final class RequestState {
        private Principal principal;
        private boolean failLogin;
        private String loginUser;
        private String loginPassword;
    }

    private static final class StubBookDataFacade extends BookDataFacade {
        private final int count;

        private StubBookDataFacade(int count) {
            this.count = count;
        }

        @Override
        public int count() {
            return count;
        }
    }

    private static final class StubFetchUserAccount extends FetchUserAccount {
        private final long count;

        private StubFetchUserAccount(long count) {
            this.count = count;
        }

        @Override
        public long countAccount(String id) {
            return count;
        }
    }

    private static final class StubUserAccountFacade extends UserAccountFacade {
        private UserAccount created;
        private UserAccount found;

        @Override
        public void create(UserAccount account) {
            created = account;
        }

        @Override
        public UserAccount find(Object id) {
            return found;
        }
    }

    private static final class StubOrderBooksFacade extends OrderBooksFacade {
        private OrderBooks created;

        @Override
        public void create(OrderBooks order) {
            created = order;
        }
    }

    private static final class StubFetchBookData extends FetchBookData {
        private final List<BookData> expectedBooks = Collections.singletonList(new BookData());
        private int lastSort;
        private Genre lastGenre;

        @Override
        public List<BookData> getBookList(int sortItem, Genre genreItem, Pagenate pagenate) {
            lastSort = sortItem;
            lastGenre = genreItem;
            return expectedBooks;
        }

        @Override
        public void preparePage(Genre genre, Pagenate pagenate) {
            pagenate.initPagination(6, 5);
        }
    }

    private static final class StubFetchOrderBooks extends FetchOrderBooks {
        private final List<OrderBooks> history;
        private String lastUserId;

        private StubFetchOrderBooks(List<OrderBooks> history) {
            this.history = history;
        }

        @Override
        public List<OrderBooks> getHistory(String id) {
            lastUserId = id;
            return history;
        }
    }

    private static final class TestLoginBean extends LoginBean {
        private HttpServletRequest request;
        private String errorMessage;

        @Override
        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public void setErrorMessage(String message) {
            errorMessage = message;
        }
    }

    private static final class TestRegisterBean extends RegisterBean {
        private String message;

        @Override
        public void setMessage(String message) {
            this.message = message;
        }
    }

    private static final class TestOrderHandler extends OrderHandler {
        private String message;
        private String userId;
        private String registeredUser;

        @Override
        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public void registerOrder(String userId) {
            registeredUser = userId;
            if ("alice".equals(userId)) {
                super.registerOrder(userId);
            }
        }
    }
}
