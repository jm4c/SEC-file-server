package types;

import java.security.PublicKey;
import java.util.Objects;

public class Pk_t extends Type_t {

    private static final long serialVersionUID = 1L;
    public PublicKey value;

    public Pk_t(PublicKey id) {
        this.value = id;
    }

    @Override
    public void print() {
        System.out.println();
        System.out.println("Public Key: ");
        System.out.println(value.toString());
        System.out.println();
    }

    @Override
    public PublicKey getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pk_t other = (Pk_t) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
