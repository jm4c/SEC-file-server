package sec.filesystem;

import types.Buffer_t;
import utils.CryptoUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a read command on a file belonging to another user.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.
 */
public class DemoReadFileByPKey {

    static PrintStream dummyStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            //NO-OP
        }
    });
    static PrintStream originalStream = System.out;

    private static void swapOutStream(String mode, String[] in) {
        if (mode.equalsIgnoreCase("enable")) {
            System.setOut(originalStream);
            return;
        }
        if (mode.equalsIgnoreCase("disable")) {
            System.setOut(dummyStream);
            for (String s : in) {
                if (s.equalsIgnoreCase("-more")) {
                    System.setOut(originalStream);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            //Library c1 = new Library();
            //Library c2 = new Library();
            TCPClient c1 = new TCPClient();
            TCPClient c2 = new TCPClient();

            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));

            // Initializing the file system
            System.out.println("// [1] CLIENT 1");
            System.out.println("// [1] Initializing the File System ...");
            swapOutStream("disable", args);
            c1.setClientID(c1.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [2] File System has been initialized sucessfully.");
            System.out.println("// [2] Client ID assigned by the server:\n\t" + c1.getClientID().getValue());

            // Initializing the file system
            System.out.println("// [3] CLIENT 2");
            System.out.println("// [3] Initializing the File System ...");
            swapOutStream("disable", args);
            c2.setClientID(c2.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [4] File System has been initialized sucessfully.");
            System.out.println("// [4] Client ID assigned by the server:\n\t" + c2.getClientID().getValue());

            // Writing to client 1's file, at position 0.
            System.out.println("// [5] CLIENT 1");
            swapOutStream("disable", args);
            String s = "The quick brown fox jumps over the lazy dog";
            buffer.setValue(CryptoUtils.serialize(s));
            int size = buffer.getValue().length;
            swapOutStream("enable", args);
            System.out.println("// [5] Writing some data of size " + size + " to the file, at pos 0 ...");
            swapOutStream("disable", args);
            c1.fs_write(0, size, buffer);
            String sent = printHexBinary(buffer.getValue());
            swapOutStream("enable", args);
            System.out.println("// [6] Write request has been performed successfully.");
            System.out.println("// [6] Data sent to the file system:\n\t" + sent);

            // Reading, as client 2, all the data that was just written to client 1's file.
            System.out.println("// [7] CLIENT 2");
            System.out.println("// [7] Performing a read request on client 1's file ...");
            swapOutStream("disable", args);
            buffer.setValue(new byte[size]);
            int bytesRead = c2.fs_read(c1.getPublicKey(), 0, size, buffer);
            String received = printHexBinary(buffer.getValue());
            swapOutStream("enable", args);
            System.out.println("// [8] Read request has been performed successfully.");
            System.out.println("// [8] Number of bytes that were read:\n\t" + bytesRead);
            System.out.println("// [8] Data read from the file:\n\t" + received);
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoReadFileByPKey.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }
}