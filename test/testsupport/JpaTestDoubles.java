package testsupport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JpaTestDoubles implements InvocationHandler {
    private final EntityManager entityManager;
    private String namedQuery;
    private Class<?> foundType;
    private Object foundId;
    private Object findResult;
    private Object singleResult = Long.valueOf(0);
    private List<?> results = Collections.emptyList();
    private int firstResult = -1;
    private int maxResults = -1;
    private final List<Object> persisted = new ArrayList<>();
    private final List<Object> merged = new ArrayList<>();
    private final List<Object> removed = new ArrayList<>();
    private final List<String> parameterNames = new ArrayList<>();
    private final List<Object> parameterValues = new ArrayList<>();

    private JpaTestDoubles() {
        entityManager = proxy(EntityManager.class, this);
    }

    public static JpaTestDoubles create() {
        return new JpaTestDoubles();
    }

    public EntityManager entityManager() {
        return entityManager;
    }

    public String namedQuery() {
        return namedQuery;
    }

    public Class<?> foundType() {
        return foundType;
    }

    public Object foundId() {
        return foundId;
    }

    public Object findResult() {
        return findResult;
    }

    public void setFindResult(Object findResult) {
        this.findResult = findResult;
    }

    public Object singleResult() {
        return singleResult;
    }

    public void setSingleResult(Object singleResult) {
        this.singleResult = singleResult;
    }

    public List<?> results() {
        return results;
    }

    public void setResults(List<?> results) {
        this.results = results;
    }

    public int firstResult() {
        return firstResult;
    }

    public int maxResults() {
        return maxResults;
    }

    public List<Object> persisted() {
        return persisted;
    }

    public List<Object> merged() {
        return merged;
    }

    public List<Object> removed() {
        return removed;
    }

    public List<String> parameterNames() {
        return parameterNames;
    }

    public List<Object> parameterValues() {
        return parameterValues;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String name = method.getName();
        if (method.getDeclaringClass() == Object.class) {
            if ("toString".equals(name)) {
                return method.getDeclaringClass().getSimpleName() + " test double";
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
        }
        if ("persist".equals(name)) {
            persisted.add(args[0]);
            return null;
        }
        if ("merge".equals(name)) {
            merged.add(args[0]);
            return args[0];
        }
        if ("remove".equals(name)) {
            removed.add(args[0]);
            return null;
        }
        if ("find".equals(name)) {
            foundType = (Class<?>) args[0];
            foundId = args[1];
            return findResult;
        }
        if ("createNamedQuery".equals(name)) {
            namedQuery = (String) args[0];
            return typedQuery();
        }
        if ("getCriteriaBuilder".equals(name)) {
            return criteriaBuilder();
        }
        if ("createQuery".equals(name)) {
            return typedQuery();
        }
        return defaultValue(method.getReturnType());
    }

    private TypedQuery<?> typedQuery() {
        return proxy(TypedQuery.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if ("setParameter".equals(name)) {
                    parameterNames.add(String.valueOf(args[0]));
                    parameterValues.add(args[1]);
                    return proxy;
                }
                if ("setFirstResult".equals(name)) {
                    firstResult = (Integer) args[0];
                    return proxy;
                }
                if ("setMaxResults".equals(name)) {
                    maxResults = (Integer) args[0];
                    return proxy;
                }
                if ("getResultList".equals(name)) {
                    return results;
                }
                if ("getSingleResult".equals(name)) {
                    return singleResult;
                }
                if (method.getDeclaringClass() == Object.class && "toString".equals(name)) {
                    return "TypedQuery test double";
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private CriteriaBuilder criteriaBuilder() {
        return proxy(CriteriaBuilder.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("createQuery".equals(method.getName())) {
                    return criteriaQuery();
                }
                if ("count".equals(method.getName())) {
                    return expression();
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private CriteriaQuery<?> criteriaQuery() {
        final CriteriaQuery<?>[] self = new CriteriaQuery<?>[1];
        self[0] = proxy(CriteriaQuery.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("from".equals(method.getName())) {
                    return root();
                }
                if ("select".equals(method.getName())) {
                    return self[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
        return self[0];
    }

    private Root<?> root() {
        return proxy(Root.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return defaultValue(method.getReturnType());
            }
        });
    }

    private Expression<?> expression() {
        return proxy(Expression.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler));
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
}
