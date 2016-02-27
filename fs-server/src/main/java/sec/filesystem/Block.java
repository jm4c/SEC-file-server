package sec.filesystem;

import java.util.Random;
import types.*;

public class Block {

    private Id_t blockID;
    private Sig_t blockSig;
    private Data_t blockData;

    public Block(Data_t data) {
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        this.blockID = new Id_t(b);
        this.blockData = data;
    }

    public Block(Data_t data, Sig_t sig, Pk_t pkey) {
        byte[] b = new byte[20];
        new Random().nextBytes(b);
        this.blockID = new Id_t(b);
        this.blockSig = sig;
        this.blockData = data;
    }

    public Id_t getID() {
        return blockID;
    }

    public Sig_t getSig() {
        return blockSig;
    }

    public Data_t getData() {
        return blockData;
    }
}
