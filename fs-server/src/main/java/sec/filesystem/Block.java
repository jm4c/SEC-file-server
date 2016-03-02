package sec.filesystem;

import java.io.Serializable;
import types.*;

public class Block implements Serializable {

	private static final long serialVersionUID = 1L;
	private Pk_t blockPKey;
    private Sig_t blockSig;
    private Data_t blockData;

    public Block(Data_t data) {
        this.blockData = data;
    }

    public Block(Data_t data, Sig_t sig, Pk_t pkey) {
        this.blockPKey = pkey;
        this.blockSig = sig;
        this.blockData = data;
    }

    public Pk_t getPKey() {
        return blockPKey;
    }

    public Sig_t getSig() {
        return blockSig;
    }

    public Data_t getData() {
        return blockData;
    }
}
