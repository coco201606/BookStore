package utility;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Logger;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.junit.jupiter.api.Test;

class UtilityTest {
    @Test
    void paginationMovesWithinFirstAndLastPageBoundaries() {
        Pagenate pagination = new Pagenate();
        pagination.initPagination(12, 5);

        assertEquals(3, pagination.getMaxPage());
        assertEquals(0, pagination.findTopData());
        assertArrayEquals(new int[]{0, 4}, pagination.ProcRange());

        pagination.movePrev();
        assertEquals(1, pagination.getCurrent());
        pagination.moveNext();
        pagination.moveNext();
        pagination.moveNext();
        assertEquals(3, pagination.getCurrent());
        assertEquals(10, pagination.findTopData());
        assertArrayEquals(new int[]{10, 14}, pagination.ProcRange());

        pagination.moveLast();
        pagination.movePrev();
        assertEquals(2, pagination.getCurrent());
        pagination.moveTop();
        assertEquals(1, pagination.getCurrent());
    }

    @Test
    void sha256EncoderReturnsLowercaseHexDigest() throws Exception {
        String expected = toHex(MessageDigest.getInstance("SHA-256").digest("password".getBytes()));

        assertEquals(expected, new SHA256Encoder().encodeBySHA256("password"));
        assertEquals(64, new SHA256Encoder().encodeBySHA256("password").length());
    }

    @Test
    void binaryFileReaderLoadsBytesAndReturnsNullWhenFileIsMissing() throws Exception {
        Path file = Files.createTempFile("bookstore-image", ".bin");
        byte[] contents = new byte[]{0, 1, 2, (byte) 255};
        try {
            Files.write(file, contents);
            assertArrayEquals(contents, BinaryDataUtility.readBinaryFile(file));
            assertNull(BinaryDataUtility.readBinaryFile(file.resolveSibling("missing-image.bin")));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loggerProducerUsesInjectionPointDeclaringClass() throws Exception {
        GenerateLogger producer = new GenerateLogger();
        final Method member = UtilityTest.class.getDeclaredMethod("loggerProducerUsesInjectionPointDeclaringClass");
        producer.point = (InjectionPoint) Proxy.newProxyInstance(
                InjectionPoint.class.getClassLoader(),
                new Class<?>[]{InjectionPoint.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        return "getMember".equals(method.getName()) ? member : null;
                    }
                });

        Logger logger = producer.getLogger();

        assertNotNull(logger);
        assertEquals(UtilityTest.class.getName(), logger.getName());
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) {
            String hex = Integer.toHexString(value & 0xff);
            if (hex.length() == 1) {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }
}
