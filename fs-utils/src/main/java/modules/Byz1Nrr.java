package modules;

import interfaces.InterfaceBlockServer;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import types.Buffer_t;
import types.Sig_t;

/*
    Implements:
        (1,N)-ByzantineRegularRegister, instance bonrr, with writer w.
    Uses:
        AuthPerfectPointToPointLinks, instance al.
*/
public class Byz1Nrr {
    
    private static final String SERVERLISTPATH = "../fs-utils/serverlist.dat";
    protected static InterfaceBlockServer server;
    
    int ts;
    Buffer_t val;
    Sig_t sigma;
    
    int wts;
    List acklist;
    int rid;
    List readlist;
    
/*  
    upon event <bonrr, Init> do
*/
    public Byz1Nrr () {
        //  (ts, val, σ) := (0,⊥,⊥);
        ts = 0;
        val = null;
        sigma = null;
        
        //  wts := 0;
        wts = 0;
        
        //  acklist := [⊥]N;
        acklist = new ArrayList<>();
        for (int k = 0; k < InterfaceBlockServer.REPLICAS; k++){
            acklist.add(-1);
        }  
        
        //  rid := 0;
        rid = 0;
        
        //  readlist := [⊥]N;
        readlist = new ArrayList<>();
        for (int k = 0; k < InterfaceBlockServer.REPLICAS; k++){
            readlist.add(null);
        }
    }

/*
    upon event <bonrr, Write | v > do
    // only process w
*/
    public void write(Buffer_t v) throws Exception {
        // wts := wts + 1;
        wts++;
        
        // acklist := [⊥]N;
        acklist = new ArrayList<>();
        for (int k = 0; k < InterfaceBlockServer.REPLICAS; k++){
            acklist.add(-1);
        }
        //sigma = sign(self, bonrr||self||WRITE||wts||v);
        // TODO
        
        // forall q ∈ Π do
        //    trigger <al, Send | q, [WRITE, wts, v, sigma]>;
        FileInputStream fin = new FileInputStream(SERVERLISTPATH);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<String> serverList = (ArrayList) ois.readObject();
        for (String servername : serverList) {
            Registry myReg = LocateRegistry.getRegistry("localhost");
            System.out.println("Contacting fs." + servername);
            try {
                server = (InterfaceBlockServer) myReg.lookup("fs." + servername);
                //TODO
                //authenticatedLink.send(this.id(), [WRITE, wts, v, sigma]);
            } 
            catch (NotBoundException | ConnectException rme) {
                System.out.println("fs." + servername + " is unresponsive...");
            }
        }
    }
 
/*
    upon event <bonrr, Read> do
*/
    public void read() throws Exception {
        // rid := rid + 1;
        rid++;
        
        // readlist := [⊥]N;
        readlist = new ArrayList<>();
        for (int k = 0; k < InterfaceBlockServer.REPLICAS; k++){
            readlist.add(null);
        }
        
         // forall q ∈ Π do
        //    trigger <al, Send | q, [READ, rid]>;
        FileInputStream fin = new FileInputStream(SERVERLISTPATH);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<String> serverList = (ArrayList) ois.readObject();
        for (String servername : serverList) {
            Registry myReg = LocateRegistry.getRegistry("localhost");
            System.out.println("Contacting fs." + servername);
            try {
                server = (InterfaceBlockServer) myReg.lookup("fs." + servername);
                //TODO
                // messagetype = READ;
                //authenticatedLink.send(this.id(), [READ, rid]);
            } 
            catch (NotBoundException | ConnectException rme) {
                System.out.println("fs." + servername + " is unresponsive...");
            }
        }
    }
}
