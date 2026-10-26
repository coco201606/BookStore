package enterprise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.EntityManager;
import entity.BookData;
import entity.Genre;
import entity.OrderBooks;
import entity.OrderState;
import entity.TypeAndId;
import entity.UserAccount;
import org.junit.jupiter.api.Test;
import testsupport.JpaTestDoubles;
import utility.Pagenate;

class EnterpriseLayerTest {
    @Test
    void abstractFacadeDelegatesCrudAndCriteriaOperations() {
        JpaTestDoubles jpa = JpaTestDoubles.create();
        TestFacade<BookData> facade = new TestFacade<>(BookData.class, jpa.entityManager());
        BookData first = new BookData();
        BookData second = new BookData();
        jpa.setFindResult(second);
        jpa.setResults(Arrays.asList(first, second));

        facade.create(first);
        facade.edit(second);
        assertSame(second, facade.find(4L));
        assertEquals(BookData.class, jpa.foundType());
        assertEquals(Long.valueOf(4), jpa.foundId());
        assertEquals(Arrays.asList(first), jpa.persisted());
        assertEquals(Arrays.asList(second), jpa.merged());

        facade.remove(first);
        assertEquals(Arrays.asList(first), jpa.removed());
        assertSame(jpa.results(), facade.findAll());
        assertSame(jpa.results(), facade.findRange(new int[]{5, 7}));
        assertEquals(5, jpa.firstResult());
        assertEquals(3, jpa.maxResults());

        jpa.setSingleResult(Long.valueOf(11));
        assertEquals(11, facade.count());
    }

    @Test
    void concreteFacadesBindTheCorrectEntityType() throws Exception {
        assertFacadeEntityType(new BookDataFacade(), BookData.class);
        assertFacadeEntityType(new OrderBooksFacade(), OrderBooks.class);
        assertFacadeEntityType(new OrderStateFacade(), OrderState.class);
        assertFacadeEntityType(new TypeAndIdFacade(), TypeAndId.class);
        assertFacadeEntityType(new UserAccountFacade(), UserAccount.class);
    }

    @Test
    void bookDataQueriesSelectSortGenreAndPagination() {
        int[] sorts = new int[]{1, 2, 3};
        String[] allQueries = new String[]{
            BookData.FAIND_BY_ID, BookData.FAIND_BY_PRICE, BookData.FAIND_BY_PRICE_DESC
        };
        String[] genreQueries = new String[]{
            BookData.FAIND_GENRE, BookData.GENRE_BY_PRICE, BookData.GENRE_BY_PRICE_DESC
        };

        for (int index = 0; index < sorts.length; index++) {
            assertBookQuery(sorts[index], Genre.NONE, allQueries[index], false);
            assertBookQuery(sorts[index], Genre.JENRE2, genreQueries[index], true);
        }
    }

    @Test
    void preparePageCountsAllBooksOrSelectedGenre() {
        JpaTestDoubles allBooksJpa = JpaTestDoubles.create();
        allBooksJpa.setSingleResult(Long.valueOf(12));
        FetchBookData allBooks = new FetchBookData();
        allBooks.em = allBooksJpa.entityManager();
        Pagenate allBooksPage = new Pagenate();

        allBooks.preparePage(Genre.NONE, allBooksPage);

        assertEquals(BookData.COUNT_ALL, allBooksJpa.namedQuery());
        assertEquals(3, allBooksPage.getMaxPage());
        assertEquals(5, allBooksPage.getPer());

        JpaTestDoubles genreJpa = JpaTestDoubles.create();
        genreJpa.setSingleResult(Long.valueOf(6));
        FetchBookData genreBooks = new FetchBookData();
        genreBooks.em = genreJpa.entityManager();
        Pagenate genrePage = new Pagenate();

        genreBooks.preparePage(Genre.JENRE1, genrePage);

        assertEquals(BookData.COUNT_GENRE, genreJpa.namedQuery());
        assertEquals(Collections.singletonList("genreItem"), genreJpa.parameterNames());
        assertEquals(Collections.<Object>singletonList(Genre.JENRE1), genreJpa.parameterValues());
        assertEquals(2, genrePage.getMaxPage());
    }

    @Test
    void orderHistoryUsesTheNamedQueryParameterDeclaredByTheEntity() {
        JpaTestDoubles jpa = JpaTestDoubles.create();
        List<OrderBooks> expected = Collections.singletonList(new OrderBooks());
        jpa.setResults(expected);
        FetchOrderBooks fetch = new FetchOrderBooks();
        fetch.em = jpa.entityManager();

        assertSame(expected, fetch.getHistory("alice"));

        assertEquals(OrderBooks.ORDER_HISTORY, jpa.namedQuery());
        assertEquals(Collections.singletonList("userId"), jpa.parameterNames());
        assertEquals(Collections.<Object>singletonList("alice"), jpa.parameterValues());
    }

    @Test
    void accountCountUsesTheUserIdNamedQueryParameter() {
        JpaTestDoubles jpa = JpaTestDoubles.create();
        jpa.setSingleResult(Long.valueOf(2));
        FetchUserAccount fetch = new FetchUserAccount();
        fetch.em = jpa.entityManager();

        assertEquals(2L, fetch.countAccount("alice"));

        assertEquals(UserAccount.COUNT_ID, jpa.namedQuery());
        assertEquals(Collections.singletonList("userId"), jpa.parameterNames());
        assertEquals(Collections.<Object>singletonList("alice"), jpa.parameterValues());
    }

    private static void assertBookQuery(
            int sort,
            Genre genre,
            String expectedQuery,
            boolean expectsGenreParameter) {
        JpaTestDoubles jpa = JpaTestDoubles.create();
        List<BookData> expected = Collections.singletonList(new BookData());
        jpa.setResults(expected);
        FetchBookData fetch = new FetchBookData();
        fetch.em = jpa.entityManager();
        Pagenate page = new Pagenate();
        page.initPagination(10, 5);
        page.moveNext();

        assertSame(expected, fetch.getBookList(sort, genre, page));

        assertEquals(expectedQuery, jpa.namedQuery());
        assertEquals(5, jpa.firstResult());
        assertEquals(5, jpa.maxResults());
        if (expectsGenreParameter) {
            assertEquals(Collections.singletonList("genreItem"), jpa.parameterNames());
            assertEquals(Collections.<Object>singletonList(genre), jpa.parameterValues());
        } else {
            assertEquals(Collections.emptyList(), jpa.parameterNames());
        }
    }

    private static void assertFacadeEntityType(AbstractFacade<?> facade, Class<?> entityType) throws Exception {
        JpaTestDoubles jpa = JpaTestDoubles.create();
        jpa.setFindResult(new Object());
        setEntityManager(facade, jpa.entityManager());

        facade.find("id");

        assertEquals(entityType, jpa.foundType());
    }

    private static void setEntityManager(Object facade, EntityManager entityManager) throws Exception {
        Field field = facade.getClass().getDeclaredField("em");
        field.setAccessible(true);
        field.set(facade, entityManager);
    }

    private static final class TestFacade<T> extends AbstractFacade<T> {
        private final EntityManager entityManager;

        private TestFacade(Class<T> entityType, EntityManager entityManager) {
            super(entityType);
            this.entityManager = entityManager;
        }

        @Override
        protected EntityManager getEntityManager() {
            return entityManager;
        }
    }
}
