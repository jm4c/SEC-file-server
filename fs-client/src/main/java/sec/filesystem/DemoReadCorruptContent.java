package sec.filesystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import types.Buffer_t;
import types.Data_t;
import types.Id_t;
import utils.CryptoUtils;

/*  Demo Class used for demonstrating a client connecting to the File Server, 
    and issuing a write command on his file, but one of the content data blocks 
    assigned to that file is corrupt.

    Please refer to the output generated by the server at runtime as it
    receives and processes the Demo Apps requests, for information on the 
    exceptions caught by the file system, and the returns of the operations 
    being performed by the server.
 */
public class DemoReadCorruptContent {

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
            for (int i = 0; i < 7; i++) {
                s = s.concat(s);
            }
            buffer.setValue(CryptoUtils.serialize(s));
            int size = buffer.getValue().length;
            System.out.println("Writing some data of size " + size + " to the file, at pos 0 ...");
            c.fs_write(0, buffer.getValue().length, buffer);
            String sent = printHexBinary(buffer.getValue());
            System.out.println("Done!");
            System.out.println("Data sent to the file system:  " + sent);
            System.out.println("---------------------------------------------------------\n");

            // Manually altering one of the content data files.
            List<Id_t> list = (List<Id_t>) c.getFileList();
            final String path = "./../fs-server/files/"+list.get(0).getValue()+".dat";
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
            
            // Reading all the data that was just written to the file
            System.out.println("Reading the data that was just written to the file...");
            buffer.setValue(new byte[size]);
            c.fs_read(c.getPublicKey(), 0, size, buffer);  
            
        } catch (Exception ex) {
            Logger.getLogger(DemoReadWriteLargeFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
