package sec.filesystem;


import exceptions.IDMismatchException;
import exceptions.InvalidSignatureException;
import exceptions.WrongHeaderSequenceException;
import types.Data_t;
import types.Id_t;
import types.Pk_t;
import types.Sig_t;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

class TCPServerThread {
    private Socket socket;
    TCPServerThread(Socket socket) {
        this.socket = socket;
    }


    void run() {
        try {
            ImplementationBlockServer server = new ImplementationBlockServer();
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient =
                    new DataOutputStream(socket.getOutputStream());
            String inputString = inFromClient.readLine();
            String[] input = inputString.split(":");
            try {
                Data_t data = null;
                Sig_t signature = null;
                Pk_t publicKey = null;
                Id_t id = null;

                switch (input[0]) {
                    case "put_h":
                        //TODO
                        server.put_h(data);
                        break;
                    case "put_k":
                        //TODO
                        server.put_k(data, signature, publicKey);
                        break;
                    case "get":
                        //TODO
                        server.get(id);
                        break;
                    default:

                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IDMismatchException e) {
                e.printStackTrace();
            } catch (WrongHeaderSequenceException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


