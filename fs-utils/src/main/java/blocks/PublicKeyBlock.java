package blocks;

import types.Data_t;
import types.Pk_t;
import types.Sig_t;

import java.io.Serializable;

public class PublicKeyBlock implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Data_t blockData;
    private Pk_t blockPKey;
    private Sig_t blockSig;

    public PublicKeyBlock(Data_t data) {
        this.blockData = data;
    }

    public PublicKeyBlock(Data_t data, Sig_t sig, Pk_t pkey) {
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
