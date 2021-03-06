package sec.filesystem;

import types.Buffer_t;
import utils.CryptoUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static interfaces.InterfaceBlockServer.REPLICAS;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/*  Demo Class used for demonstrating that the client can support up to
    N/2-1 faulty replicas. In this case, only N/2+1 replicas are online
    and the other remaining replicas are simulated but do not exist.

    This test uses a custom version of DemoReadWriteOwnFile in order to
    change the number of Replicas the client think exists.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.
 */

public class DemoSomeReplicasDown {

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
        //simulates 2 nReplicas -1 available, but only nReplicas will be online
        int simulatedReplicas = 2 * REPLICAS - 1;
        TCPClient c = new TCPClient(simulatedReplicas);
        CustomDemoReadWriteOwnFile(args, c);


    }

    private static void CustomDemoReadWriteOwnFile(String[] args, TCPClient c) {
        try {
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
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoReadWriteOwnFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);

        }
    }
}
