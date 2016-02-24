package types;

import java.security.Signature;

public class Sig_t extends Type_t{

	private static final long serialVersionUID = 1L;
	public Signature value;

    public Sig_t(Signature id) {
        this.value = id;
    }
    
    @Override
    public void print(){
    	System.out.println();
        System.out.println("Signature: ");
        System.out.println(value.toString());
		System.out.println();
    }
    
    @Override
    public Signature getValue(){
    	return value;
    }
}