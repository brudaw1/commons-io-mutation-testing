// Here below is the ReaderInputStreamTest.java for improving one mutation coverage from 95% to 100%

package org.apache.commons.io.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Test;

public class ReaderInputStreamTest {
    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    private static final String LARGE_TEST_STRING;

    static {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private final Random random = new Random();

    private void testWithSingleByteRead(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(charsetName);
        final ReaderInputStream in = new ReaderInputStream(new StringReader(testString), charsetName);
        for (final byte b : bytes) {
            final int read = in.read();
            assertTrue(read >= 0);
            assertTrue(read <= 255);
            assertEquals(b, (byte) read);
        }
        assertEquals(-1, in.read());
        in.close();
    }

    private void testWithBufferedRead(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        final ReaderInputStream in = new ReaderInputStream(new StringReader(testString), charsetName);
        final byte[] buffer = new byte[128];
        int offset = 0;
        while (true) {
            int bufferOffset = random.nextInt(64);
            final int bufferLength = random.nextInt(64);
            int read = in.read(buffer, bufferOffset, bufferLength);
            if (read == -1) {
                assertEquals(offset, expected.length);
                break;
            } else {
                assertTrue(read <= bufferLength);
                while (read > 0) {
                    assertTrue(offset < expected.length);
                    assertEquals(expected[offset], buffer[bufferOffset]);
                    offset++;
                    bufferOffset++;
                    read--;
                }
            }
        }
        in.close();
    }

    @Test
    public void testUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-8");
    }

    @Test
    public void testLargeUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(LARGE_TEST_STRING, "UTF-8");
    }

    @Test
    public void testUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(TEST_STRING, "UTF-8");
    }

    @Test
    public void testLargeUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(LARGE_TEST_STRING, "UTF-8");
    }

    @Test
    public void testUTF16WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-16");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testReadZero() throws Exception {
        final String inStr = "test";
        final ReaderInputStream r = new ReaderInputStream(new StringReader(inStr));
        final byte[] bytes = new byte[30];
        assertEquals(0, r.read(bytes, 0, 0));
        assertEquals(inStr.length(), r.read(bytes, 0, inStr.length() + 1));
        assertEquals(0, r.read(bytes, 0, 0));
        r.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testReadZeroEmptyString() throws Exception {
        final ReaderInputStream r = new ReaderInputStream(new StringReader(""));
        final byte[] bytes = new byte[30];
        assertEquals(0, r.read(bytes, 0, 0));
        assertEquals(-1, r.read(bytes, 0, 1));
        assertEquals(0, r.read(bytes, 0, 0));
        assertEquals(-1, r.read(bytes, 0, 1));
        r.close();
    }

    @Test
    public void testCharsetMismatchInfiniteLoop() throws IOException {
        final char[] inputChars = new char[]{(char) 0xE0, (char) 0xB2, (char) 0xA0};
        final Charset charset = Charset.forName("ASCII");
        try (ReaderInputStream stream = new ReaderInputStream(new CharArrayReader(inputChars), charset)) {
            while (stream.read() != -1) {
            }
        }
    }


    @Test
    public void testReadWithInvalidParameters() throws IOException {
        final ReaderInputStream in = new ReaderInputStream(new StringReader(TEST_STRING), "UTF-8");
        final byte[] buffer = new byte[10];

        try {
            in.read(buffer, -1, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            in.read(buffer, 0, -5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            in.read(buffer, 6, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        in.close();
    }


    @Test
    public void testEOFBehavior() throws IOException {
        final ReaderInputStream in = new ReaderInputStream(new StringReader(""), "UTF-8");

        assertEquals(-1, in.read());

        final byte[] buffer = new byte[10];
        assertEquals(-1, in.read(buffer, 0, buffer.length));

        in.close();
    }
}
