package sec.filesystem;

import types.Buffer_t;
import utils.CryptoUtils;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static interfaces.InterfaceBlockServer.REPLICAS;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a write command on his file, but the server is unable to find
    the header block assigned to that file.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.
 */
public class DemoReadingNonExistentFile {

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
            //Library c = new Library();
            TCPClient c = new TCPClient();
            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));

            // Initializing the file system
            System.out.println("// [1] Initializing the File System ...");
            swapOutStream("disable", args);
            c.setClientID(c.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [2] File System has been initialized successfully.");
            System.out.println("// [2] Client ID assigned by the server:\n\t" + c.getClientID().getValue());

            // Writing to the file at position 0
            swapOutStream("disable", args);
            String s = "The quick brown fox jumps over the lazy dog";
            buffer.setValue(CryptoUtils.serialize(s));
            int size = buffer.getValue().length;
            swapOutStream("enable", args);
            System.out.println("// [3] Performing a write request ...");
            System.out.println("// [3] Writing some data of size " + size + " to the file, at pos 0 ...");
            swapOutStream("disable", args);
            c.fs_write(0, size, buffer);
            String sent = printHexBinary(buffer.getValue());
            swapOutStream("enable", args);
            System.out.println("// [4] Write request has been performed successfully.");
            System.out.println("// [4] Data sent to the file system:\n\t" + sent);

            // Manually getting rid of the header data file.
            swapOutStream("disable", args);

            // TODO BUG - CANT REMOVE DUE TO FILE BEING CURRENTLY IN USE
            for (int i = 0; i < REPLICAS; i++) {
                final Path path = Paths.get("./../fs-server/files/server" + i + "/" + c.getClientID().getValue() + ".dat");
                Files.delete(path);
            }

            swapOutStream("enable", args);

            // Reading all the data that was just written to the file
            System.out.println("// [5] Performing a read request ...");
            System.out.println("// [5] Reading the data that was just written to the file ...");
            swapOutStream("disable", args);
            buffer.setValue(new byte[size]);
            c.fs_read(c.getPublicKey(), 0, size, buffer);
            swapOutStream("enable", args);
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);

        } catch (FileNotFoundException ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoReadingNonExistentFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has terminated.");
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoReadingNonExistentFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }
}
