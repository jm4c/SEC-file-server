package types;

public class Id_t extends Type_t{

	private static final long serialVersionUID = 1L;
	public byte[] value;

    public Id_t(byte[] id) {
        this.value = id;
    }
    
    @Override
    public void print(){
    	System.out.println();
        System.out.println("Client ID: " + value.toString());
		System.out.println();
    }
    
    @Override
    public byte[] getValue(){
    	return value;
    }
}