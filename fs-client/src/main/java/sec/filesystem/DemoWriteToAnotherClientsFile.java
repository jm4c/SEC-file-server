package sec.filesystem;

import exceptions.InvalidSignatureException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import types.Buffer_t;
import utils.CryptoUtils;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a write command on a file that does not belong to him.
    This aims to simulate an attacker that "steals" another user's ID.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.

    WARNING
    This demo App currently does not work with smartcards.
 */
public class DemoWriteToAnotherClientsFile {

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
            Library c1 = new Library();
            Library c2 = new Library();
            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));

            // Initializing the file system for client 1
            System.out.println("// [1] CLIENT 1");
            System.out.println("// [1] Initializing the File System ...");
            swapOutStream("disable", args);
            c1.setClientID(c1.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [2] File System has been initialized sucessfully.");
            System.out.println("// [2] Client ID assigned by the server:\n\t" + c1.getClientID().getValue());

            // Initializing the file system for client 2
            System.out.println("// [3] CLIENT 2");
            System.out.println("// [3] Initializing the File System ...");
            swapOutStream("disable", args);
            c2.setClientID(c2.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [4] File System has been initialized sucessfully.");
            System.out.println("// [4] Client ID assigned by the server:\n\t" + c2.getClientID().getValue());

            // Writing to client 1's file, at position 0.
            swapOutStream("disable", args);
            final String s = "The quick brown fox jumps over the lazy dog";
            buffer.setValue(CryptoUtils.serialize(s));
            swapOutStream("enable", args);
            System.out.println("// [5] CLIENT 1");
            System.out.println("// [5] Writing some data of size " + buffer.getValue().length + " to client 1's file, at pos 0 ...");
            swapOutStream("disable", args);
            c1.fs_write(0, buffer.getValue().length, buffer);
            swapOutStream("enable", args);
            System.out.println("// [6] Write request has been performed successfully.");
            System.out.println("// [6] Data sent to the file system:\n\t" + printHexBinary(buffer.getValue()));

            //Stealing client 1's Public Key
            swapOutStream("disable", args);
            c2.setPublicKey(c1.getPublicKey());
            swapOutStream("enable", args);

            // Writing to client 1's file, as client 2.
            System.out.println("// [7] CLIENT 2");
            System.out.println("// [7] Writing some data of size " + buffer.getValue().length + " to client 1's file, as client 2 ...");
            swapOutStream("disable", args);
            c2.fs_write(0, buffer.getValue().length, buffer);
            swapOutStream("enable", args);
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);

        } catch (InvalidSignatureException ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoWriteToAnotherClientsFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has terminated.");
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoWriteToAnotherClientsFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }

    public static void promptEnterKey() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
