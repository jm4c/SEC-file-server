package types;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class Id_t extends Type_t{

	private static final long serialVersionUID = 1L;
	public String value;

    public Id_t(byte[] hash) {
        this.value = printHexBinary(hash);
    }
    
    @Override
    public void print(){
    	System.out.println();
        System.out.println("Client ID: " + value);
		System.out.println();
    }
    
    @Override
    public String getValue(){
    	return value;
    }
    
    public boolean equals(Id_t id){
    	return this.getValue().equals(id.getValue());
    }
}