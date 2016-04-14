package sec.filesystem;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import types.Buffer_t;
import utils.CryptoUtils;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a write request of a large file.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.
 */
public class DemoReadWriteLargeFile {

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
        } else {
            System.out.println("INVALID MODE, TERMINATING DEMOAPP");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        try {
            Library c = new Library();
            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));

            // Initializing the file system
            System.out.println("// [1] Initializing the File System ...");
            swapOutStream("disable", args);
            c.setClientID(c.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [2] File System has been initialized sucessfully.");
            System.out.println("// [2] Client ID assigned by the server:\n\t" + c.getClientID().getValue());

            // Writing to the file at position 0
            swapOutStream("disable", args);
            String s = "The quick brown fox jumps over the lazy dog";
            for (int i = 0; i < 10; i++) {
                s = s.concat(s);
            }
            buffer.setValue(CryptoUtils.serialize(s));
            swapOutStream("enable", args);
            System.out.println("// [3] Performing a write request ...");
            System.out.println("// [3] Writing some data of size " + buffer.getValue().length + " to the file, at pos 0 ...");
            swapOutStream("disable", args);
            c.fs_write(0, buffer.getValue().length, buffer);
            String sent = printHexBinary(buffer.getValue());
            swapOutStream("enable", args);
            System.out.println("// [4] Write request has been performed successfully.");
            System.out.println("// [4] Data sent to the file system:\n\t" + sent);

            // Reading all the data that was just written to the file
            System.out.println("// [5] Performing a read request ...");
            System.out.println("// [5] Reading the data that was just written to the file ...");
            swapOutStream("disable", args);
            int bytesRead = c.fs_read(c.getPublicKey(), 0, buffer.getValue().length, buffer);
            String received = printHexBinary(buffer.getValue());
            swapOutStream("enable", args);
            System.out.println("// [6] Read request has been performed successfully.");
            System.out.println("// [6] Number of bytes that were read:\n\t" + bytesRead);
            System.out.println("// [6] Data read from the file:\n\t" + received);

            // Comparing the output of read and write
            System.out.println("// [7] Comparing the data sent with the data received...");
            System.out.println("// [7] String.compareTo(sent,received) returns:\n\t" + sent.compareTo(received));
            System.out.println("// [ ] DemoApp has terminated.");
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.out.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoReadWriteLargeFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }
}