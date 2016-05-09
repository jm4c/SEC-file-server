package modules;

import types.Message;

/* 
    Stubburn Point To Point Links
*/

public class Sp2p {

        FLp2pL flp2pl;
        int sent, startTimer;
        
    	public Sp2p(int delay) {
            sent = 0;
            startTimer = delay;
            flp2pl = new FLp2pL();
	}

	public void deliver(String src, Message msg) {
            // todo
	}

	public void send(String dest, Message msg) {
            while (true){
                flp2pl.send(dest, msg);
            }
	}
        
}
