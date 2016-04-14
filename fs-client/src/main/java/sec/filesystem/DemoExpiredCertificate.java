package sec.filesystem;

import exceptions.RevokedCertificateException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    with an expired certificate. 
    A smartcard with expired certificates in required in order to run this test.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.

    WARNING
    This demo App currently only works if using smartcards with revoked 
    certificates.
 */
public class DemoExpiredCertificate {

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

            // Initializing the file system
            System.out.println("// [1] Initializing the File System ...");
            swapOutStream("disable", args);
            c.setClientID(c.fs_init());
            swapOutStream("enable", args);
            System.out.println("// [2] File System has been initialized sucessfully.");
            System.out.println("// [2] Client ID assigned by the server:\n\t" + c.getClientID().getValue());
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);

        } catch (RevokedCertificateException ex) {
            swapOutStream("enable", args);
            System.out.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoExpiredCertificate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has terminated.");
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.out.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoExpiredCertificate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }
}
