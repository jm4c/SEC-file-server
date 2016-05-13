package sec.filesystem;

import exceptions.IDMismatchException;
import types.Buffer_t;
import types.Data_t;
import types.Id_t;
import utils.CryptoUtils;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static interfaces.InterfaceBlockServer.REPLICAS;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a read request for content that has suffered data corruption.

    Supported runtime arguments:
        -more       Shows more detailed information during runtime.
        -log        Shows the logging of relevant exceptions during runtime.
 */
public class DemoReadCorruptContentInOneServer {

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
            for (int i = 0; i < 7; i++) {
                s = s.concat(s);
            }
            buffer.setValue(CryptoUtils.serialize(s));
            int size = buffer.getValue().length;
            swapOutStream("enable", args);
            System.out.println("// [3] Performing a write request ...");
            System.out.println("// [3] Writing some data of size " + buffer.getValue().length + " to the file, at pos 0 ...");
            swapOutStream("disable", args);
            c.fs_write(0, buffer.getValue().length, buffer);
            String sent = printHexBinary(buffer.getValue());
            swapOutStream("enable", args);
            System.out.println("// [4] Write request has been performed successfully.");
            System.out.println("// [4] Data sent to the file system:\n\t" + sent);

            // Manually altering one of the content data files.
            swapOutStream("disable", args);
            List<Id_t> list = (List<Id_t>) c.getFileList();

            final String path = "./../fs-server/files/server0/" + list.get(0).getValue()+".dat";
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            Object obj = ois.readObject();
            Data_t data = (Data_t) obj;
            byte[] buff = CryptoUtils.serialize("corruption");
            System.arraycopy(buff, 0, data.getValue(), 0, buff.length);
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(data);
            ois.close();
            oos.close();



            swapOutStream("enable", args);

            // Reading all the data that was just written to the file
            System.out.println("// [5] Performing a read request ...");
            System.out.println("// [5] Reading the data that was just written to the file ...");
            swapOutStream("disable", args);
            buffer.setValue(new byte[size]);
            c.fs_read(c.getPublicKey(), 0, size, buffer);
            swapOutStream("enable", args);
            System.out.println("// [ ] DemoApp has terminated.");
            System.exit(0);

        } catch (Exception ex) {
            swapOutStream("enable", args);
            System.err.println("// [ ] [Catch] Exception:\n\t" + ex.getMessage());
            for (String s : args) {
                if (s.equalsIgnoreCase("-log")) {
                    Logger.getLogger(DemoReadCorruptContentInOneServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("// [ ] DemoApp has INCORRECTLY terminated.");
            System.exit(-1);
        }
    }
}
