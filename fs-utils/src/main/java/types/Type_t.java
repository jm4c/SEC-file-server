package types;

import java.io.Serializable;

public abstract class Type_t implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract void print();

    public abstract Object getValue();

}
