package types;

public class Sig_t extends Type_t{

	private static final long serialVersionUID = 1L;
	public byte[] value;

    public Sig_t(byte[] id) {
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
    public byte[] getValue(){
    	return value;
    }
}