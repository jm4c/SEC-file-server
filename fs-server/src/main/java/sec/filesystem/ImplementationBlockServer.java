package sec.filesystem;

import blocks.PublicKeyBlock;
import exceptions.IDMismatchException;
import interfaces.InterfaceBlockServer;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import exceptions.InvalidSignatureException;
import exceptions.WrongHeaderSequenceException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import types.*;
import utils.HashUtils;
import utils.CryptoUtils;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

    private static final long serialVersionUID = 1L;
    private final List<Pk_t> pKeyList;
    private final List<Certificate> certList;

    public ImplementationBlockServer() throws RemoteException {
        pKeyList = new ArrayList<>();
        certList = new ArrayList<>();
    }

    private boolean verifyIntegrity(PublicKeyBlock b) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        return CryptoUtils.verify(b.getData().getValue(), b.getPKey().getValue(), b.getSig().getValue());
    }

    //for header block
    private Id_t calculateBlockID(Pk_t publicKey) throws NoSuchAlgorithmException, IOException {
        byte[] hash = HashUtils.hash(publicKey.getValue().toString(), null);
        return new Id_t(hash);
    }

    //for other blocks
    private Id_t calculateBlockID(Data_t data) throws NoSuchAlgorithmException, IOException {
        byte[] hash = HashUtils.hash(data.getValue(), null);

        return new Id_t(hash);
    }

    private boolean certAlreadyStored(List<Certificate> certList, PublicKey public_key) {
        boolean alreadyStored = false;
        for (Certificate cert : certList) {
            if (cert.getPublicKey().hashCode() == public_key.hashCode()) {
                alreadyStored = true;
            }
        }
        return alreadyStored;
    }

    private boolean pKeyAlreadyStored(List<Pk_t> pKeyList, PublicKey public_key) {
        boolean alreadyStored = false;
        for (Pk_t pkey : pKeyList) {
            if (pkey.getValue().hashCode() == public_key.hashCode()) {
                alreadyStored = true;
            }
        }
        return alreadyStored;
    }

    @Override
    public List readPubKeys() throws RemoteException {
        List<PublicKey> keyList = new ArrayList<>();

        for (Certificate cert : certList) {
            keyList.add(cert.getPublicKey());
        }
        for (Pk_t key : pKeyList) {
            keyList.add(key.getValue());
        }
        return keyList;
    }

    @Override
    public boolean storePubKey(Certificate cert) throws RemoteException {
        boolean certExists = certList.contains(cert);
        if (!certExists) {
            certList.add(cert);
            return true;
        }
        return false;
    }

    @Override
    public boolean storePubKey(Pk_t key) throws RemoteException {
        boolean keyExists = pKeyList.contains(key);
        if (!keyExists) {
            pKeyList.add(key);
            return true;
        }
        return false;
    }

    @Override
    public Data_t get(Id_t id) throws RemoteException {
        PublicKeyBlock b = null;
        // Main/Header block
        try {
            String s = id.getValue();
            FileInputStream fin = null;
            fin = new FileInputStream("./files/" + s + ".dat");
            ObjectInputStream ois = new ObjectInputStream(fin);
            Object obj = ois.readObject();
            if (obj instanceof PublicKeyBlock) {
                System.out.println("\nGot header from:./files/" + s + ".dat");
                b = (PublicKeyBlock) obj;
                ois.close();
                if (!verifyIntegrity(b)) {
                    throw new InvalidSignatureException("Invalid signature.");
                } else {
                    System.out.println("Valid signature");
                }
                return b.getData();
            } else {
                System.out.println("Got content from:./files/" + s + ".dat");
                Data_t data = (Data_t) obj;
                ois.close();
                String blockID = calculateBlockID(data).getValue();
                if (s.compareTo(blockID) != 0) {
                    throw new IDMismatchException("Content IDs are not the same.");
                }
                return data;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws RemoteException, InvalidSignatureException {

        try {
            if (!CryptoUtils.verify(data.getValue(), public_key.getValue(), signature.getValue())) {
                throw new InvalidSignatureException("Invalid signature.");
            }
            System.out.println("signature is valid");

            Id_t id = calculateBlockID(public_key);

            //check if publicKey is already stored           
            boolean headerAlreadyExists = false;
            PublicKey pKey = public_key.getValue();
            if(certAlreadyStored(certList, pKey) || pKeyAlreadyStored(pKeyList, pKey))
                headerAlreadyExists = true;
            //System.out.println("[DEBUG]    //already exists: " + headerAlreadyExists);

            //check timestamp
            if (headerAlreadyExists) {
                Timestamp oldTimestamp = ((Header_t) CryptoUtils.deserialize(get(id).getValue())).getTimestamp();
                Timestamp newTimestamp = ((Header_t) CryptoUtils.deserialize(data.getValue())).getTimestamp();
                System.out.println(oldTimestamp.toString());
                System.out.println(newTimestamp.toString());
                if (!newTimestamp.after(oldTimestamp)) {
                    throw new WrongHeaderSequenceException("New header's timestamp is older than old header's timestamp");
                }

            }

            System.out.println(id.getValue());
            PublicKeyBlock b = new PublicKeyBlock(data, signature, public_key);

            //check timestamp
            String s = id.getValue();
            new File("./files/").mkdirs();
            FileOutputStream fout = new FileOutputStream("./files/" + s + ".dat");
            System.out.println("Stored header in:./files/" + s + ".dat");

            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(b);
            oos.close();

            return id;
        } catch (InvalidSignatureException ise) {
            ise.printStackTrace();
            throw ise;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Id_t put_h(Data_t data) throws RemoteException {
        try {
            Id_t id = calculateBlockID(data);
            String s = id.getValue();
            new File("./files/").mkdirs();
            FileOutputStream fout = new FileOutputStream("./files/" + s + ".dat");
            System.out.println("Stored content in:./files/" + s + ".dat");

            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(data);
            oos.close();

            return id;

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String greeting() throws RemoteException {
        return "Hello There!";
    }

    @Override
    public Id_t getID(Pk_t pk) throws RemoteException {
        Id_t id;
        try {
            id = calculateBlockID(pk);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return id;
    }

}
