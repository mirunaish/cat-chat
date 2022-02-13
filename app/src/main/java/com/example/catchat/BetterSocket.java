package com.example.catchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * BetterSocket: wrapper around Socket
 * methods:
 *      readBytes() and readInt();
 *      writeBytes() and writeInt();
 *      destroy();
 *      printStatus();
 */
public class BetterSocket {
    private Socket sock;
    private InputStream in;
    private OutputStream out;

    /**
     * Creates a wrapper around the given socket; opens the input and output streams
     * @param sock the socket to wrap
     * @throws IOException if opening streams failed
     */
    public BetterSocket(Socket sock) throws IOException {
        this.sock = sock;
        in = sock.getInputStream();
        out = sock.getOutputStream();
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return sock;
    }

    /**
     * Reads an array of bytes from the input stream.
     * Blocks until `length` bytes are read.
     * @param length how many bytes to read
     * @return the array of bytes read
     * @throws IOException if reading failed
     * @throws NullPointerException  if input stream closed
     */
    public byte[] readBytes(int length) throws IOException, NullPointerException {
        byte[] data = new byte[length];
        int totalBytesRead = 0;

        // while i haven't read all the bytes, read and append to data
        while (totalBytesRead < length) {
            int bytesRead = in.read(data, totalBytesRead, length-totalBytesRead);

            // not really a null pointer but need to check for end of stream
            if (bytesRead == -1) throw new NullPointerException();

            totalBytesRead += bytesRead;
        }

        return data;
    }

    /**
     * Reads an integer from the input stream.
     * Blocks until integer is read.
     * @return the integer
     * @throws IOException if reading failed
     * @throws NullPointerException if stream closed
     */
    public int readInt() throws IOException, NullPointerException {
        byte[] data = new byte[4];  // java ints are 32 bits or 4 bytes

        // not really a null pointer but need to check for end of stream
        if (in.read(data) == -1) throw new NullPointerException();

        // https://stackoverflow.com/questions/2383265/convert-4-bytes-to-int#2383729
        ByteBuffer bb = ByteBuffer.wrap(data);
        return bb.getInt();
    }

    /**
     * Writes an array of bytes to the output stream.
     * @param data the array of bytes
     * @throws IOException if writing failed
     */
    public void writeBytes(byte[] data) throws IOException {
        out.write(data);
    }

    /**
     * Writes an integer to the output stream.
     * @param data the integer to write
     * @throws IOException if writing failed
     */
    public void writeInt(int data) throws IOException {
        // https://stackoverflow.com/questions/6374915/java-convert-int-to-byte-array-of-4-bytes#6374970
        out.write(ByteBuffer.allocate(4).putInt(data).array());
    }

    /**
     * Closes the input and output streams and the socket.
     * @throws IOException if closing failed
     */
    public void destroy() throws IOException {
        int errors = 0;

        // try to close each one; even if one fails; try to close the others
        try { in.close(); } catch (IOException e) { errors++; }
        try { out.close(); } catch (IOException e) { errors++; }
        try { sock.close(); } catch (IOException e) { errors++; }

        if (errors > 0) throw new IOException("Failed to close " + errors + " resources");
    }

    /**
     * Prints the state of the I/O streams and the socket
     */
    public void printStatus() {
        System.out.println("socket is " + (sock.isClosed() ? "closed" : "open"));
        System.out.println("input is " + (sock.isInputShutdown() ? "closed" : "open"));
        System.out.println("output is " + (sock.isOutputShutdown() ? "closed" : "open"));
    }
}
