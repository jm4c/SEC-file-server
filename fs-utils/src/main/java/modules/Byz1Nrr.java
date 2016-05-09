package modules;

import interfaces.InterfaceBlockServer;
import java.util.ArrayList;
import java.util.List;
import types.Buffer_t;
import types.Sig_t;

public class Byz1Nrr {
    int ts;
    Buffer_t val;
    Sig_t sigma;
    
    int wts;
    List acklist;
    int rid;
    List readlist;
            
    public Byz1Nrr () {
        ts = 0;
        val = null;
        sigma = null;
        wts = 0;
        acklist = new ArrayList<>();
        for (int k = 0; k < InterfaceBlockServer.REPLICAS; k++){
            acklist.add(-1);
        }  
        rid = 0;
        readlist = new ArrayList<>();
        for (int k = 0; k < InterfaceBlockServer.REPLICAS; k++){
            readlist.add(-1);
        }
    }
    
   // public void write()
    
}
