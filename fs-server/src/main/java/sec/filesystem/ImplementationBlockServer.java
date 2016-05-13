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
import exceptions.RevokedCertificateException;
import exceptions.WrongHeaderSequenceException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import types.*;
import utils.HashUtils;
import utils.CryptoUtils;
import eIDlib_PKCS11.EIDLib_PKCS11;

public class ImplementationBlockServer extends UnicastRemoteObject implements InterfaceBlockServer {

    private static final long serialVersionUID = 1L;
    private final List<Pk_t> pKeyList;
    private final List<Certificate> certList;
    private int serverID;

    public ImplementationBlockServer(int serverID) throws RemoteException {
        pKeyList = new ArrayList<>();
        certList = new ArrayList<>();
        this.serverID=serverID;
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
    public boolean storePubKey(Certificate cert) throws RemoteException, RevokedCertificateException {
        if (!EIDLib_PKCS11.isCertificateValid((X509Certificate) cert)) {
            certList.remove(cert);
            throw new RevokedCertificateException("Certificate has been revoked by its certification authority");
        };
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
    public Data_t get(Id_t id) throws IOException, InvalidKeyException, InvalidSignatureException, NoSuchAlgorithmException, ClassNotFoundException, SignatureException, IDMismatchException {
        PublicKeyBlock b;
        // Main/Header block
        String s = id.getValue();
        FileInputStream fin;
        fin = new FileInputStream("./files/server" + serverID + "/" + s + ".dat");
        ObjectInputStream ois = new ObjectInputStream(fin);
        Object obj = ois.readObject();
        if (obj instanceof PublicKeyBlock) {
            System.out.println("\n[Server" + serverID+"] Got header from:./files/server" + serverID + "/" + s + ".dat");
            b = (PublicKeyBlock) obj;
            ois.close();
            if (!verifyIntegrity(b)) {
                throw new InvalidSignatureException("Invalid signature.");
            } else {
                System.out.println("[Server" + serverID+"] Valid signature");
            }
            return b.getData();
        } else {
            System.out.println("[Server" + serverID+"] Got content from:./files/server" + serverID + "/" + s + ".dat");
            Data_t data = (Data_t) obj;
            ois.close();
            String blockID = calculateBlockID(data).getValue();
            if (s.compareTo(blockID) != 0) {
                throw new IDMismatchException("Content IDs are not the same.");
            }
            return data;
        }
    }

    @Override
    public Id_t put_k(Data_t data, Sig_t signature, Pk_t public_key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSignatureException, IOException, ClassNotFoundException, IDMismatchException, WrongHeaderSequenceException {

        verifySignedData(data, signature, public_key);
        System.out.println("[Server" + serverID+"] signature is valid");

        Id_t id = calculateBlockID(public_key);

        //check if publicKey is already stored           
        boolean headerAlreadyExists = false;
        PublicKey pKey = public_key.getValue();
        if (certAlreadyStored(certList, pKey) || pKeyAlreadyStored(pKeyList, pKey)) {
            headerAlreadyExists = true;
        }
        //check timestamp, in order to defend against replay attacks
        if (headerAlreadyExists) {
            Timestamp oldTimestamp = getTimestampFromFileByID(id);
            System.out.println("[Server" + serverID+"] OLD: " + oldTimestamp.toString());
            Timestamp newTimestamp = getTimestampFromData(data);
            System.out.println("[Server" + serverID+"] NEW: " + newTimestamp.toString());
            if (!newTimestamp.after(oldTimestamp)) {
                throw new WrongHeaderSequenceException("New header's timestamp: "
                        + newTimestamp.toString() + "\n"
                        + "\tis older than previous header's timestamp: "
                        + oldTimestamp.toString());
            }

        }

        System.out.println("[Server" + serverID + "]" + id.getValue());
        PublicKeyBlock b = new PublicKeyBlock(data, signature, public_key);

        String s = id.getValue();
        new File("./files/server" + serverID + "/").mkdirs();
        FileOutputStream fout = new FileOutputStream("./files/server" + serverID + "/" + s + ".dat");
        System.out.println("[Server" + serverID+"] Stored header in:./files/server" + serverID + "/" + s + ".dat");

        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(b);
        oos.close();

        return id;
    }

    private void verifySignedData(Data_t data, Sig_t signature, Pk_t public_key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSignatureException {
        if (!CryptoUtils.verify(data.getValue(), public_key.getValue(), signature.getValue())) {
            throw new InvalidSignatureException("Invalid signature.");
        }
    }

    private Timestamp getTimestampFromData(Data_t data) throws IOException, ClassNotFoundException {
        return ((Header_t) CryptoUtils.deserialize(data.getValue())).getTimestamp();
    }

    private Timestamp getTimestampFromFileByID(Id_t id) throws IOException, ClassNotFoundException, InvalidKeyException, InvalidSignatureException, NoSuchAlgorithmException, SignatureException, IDMismatchException {
        return getTimestampFromData(get(id));
    }

    @Override
    public Id_t put_h(Data_t data) throws NoSuchAlgorithmException, IOException {
        Id_t id = calculateBlockID(data);
        String s = id.getValue();
        new File("./files/server" + serverID + "/").mkdirs();
        FileOutputStream fout = new FileOutputStream("./files/server" + serverID + "/" + s + ".dat");
        System.out.println("[Server" + serverID+"] Stored content in:./files/server" + serverID + "/" + s + ".dat");

        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(data);
        oos.close();

        return id;
    }

    @Override
    public String greeting() throws RemoteException {
        return "Hello There!";
    }

    @Override
    public Id_t getID(Pk_t pk) throws RemoteException, NoSuchAlgorithmException, IOException {
        Id_t id;
        id = calculateBlockID(pk);
        return id;
    }

    public int getServerID(){
        return this.serverID;
    }
}
