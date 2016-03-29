package types;

import java.sql.Timestamp;
import java.util.List;

public class Header_t extends Type_t {

	private static final long serialVersionUID = 1L;
	
	List<Id_t> fileList;
	Timestamp timestamp;
	
	public Header_t(List<Id_t> fileList){
		this.fileList = fileList;
		this.timestamp = new Timestamp(System.currentTimeMillis());
	}

	@Override
	public void print() {
		System.out.println();
        System.out.println("File List: " + fileList.toString());
        System.out.println("Timestamp: " + timestamp.toString());
        System.out.println();
		
	}

	@Override
	public List<Id_t> getValue() {
		return fileList;
	}
	
	public Timestamp getTimestamp(){
		return timestamp;
	}


}
