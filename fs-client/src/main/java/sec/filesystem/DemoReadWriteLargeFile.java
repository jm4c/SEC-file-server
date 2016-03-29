package sec.filesystem;

import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import types.Buffer_t;
import utils.CryptoUtils;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a write and read command of a large quantity of data.

    Please refer to the output generated by the server at runtime as it
    receives and processes the Demo Apps requests, for information on the 
    exceptions caught by the file system, and the returns of the operations 
    being performed by the server.
 */
public class DemoReadWriteLargeFile {

    public static void main(String[] args) {
        try {
            Library c = new Library();
            Buffer_t buffer = new Buffer_t(CryptoUtils.serialize(""));
            
            // Initializing the file system
            System.out.println("Initializing the File System...");
            c.fs_init();
            System.out.println("Done!");
            System.out.println("Client ID assigned by server: " + c.getClientID().getValue());
            System.out.println("---------------------------------------------------------\n");
            
            // Writing to the file at position 0
            String s = "The quick brown fox jumps over the lazy dog";
            for(int i = 0; i<10; i++){
                s = s.concat(s);
            }
            buffer.setValue(CryptoUtils.serialize(s));
            System.out.println("Writing some data of size " + buffer.getValue().length + "to the file, at pos 0 ...");
            c.fs_write(0, buffer.getValue().length, buffer);
            String sent = printHexBinary(buffer.getValue());
            System.out.println("Done!");
            System.out.println("Data sent to the file system:  " + sent);
            System.out.println("---------------------------------------------------------\n"); 
            
            // Reading all the data that was just written to the file
            System.out.println("Reading the data that was just written to the file...");
            int bytesRead = c.fs_read(c.getPublicKey(), 0, buffer.getValue().length, buffer);
            String received = printHexBinary(buffer.getValue());
            System.out.println("Done!");
            System.out.println("Number of bytes that were read: " + bytesRead);
            System.out.println("Data read from the file:  " + received);
            System.out.println("---------------------------------------------------------\n");            
            
            // Comparing the output of read and write
            System.out.println("Comparing the data sent with the data received...");
            System.out.println("String.compareTo(sent,received) returns:" + sent.compareTo(received));
            System.out.println("---------------------------------------------------------\n"); 
            
        } catch (Exception ex) {
            System.out.println("[Catch] Exception: " + ex.getMessage());
            Logger.getLogger(DemoReadWriteLargeFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
