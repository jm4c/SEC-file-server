package eIDlib_PKCS11;

import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import sun.security.pkcs11.wrapper.PKCS11;

public class test {
    
    public static void main(String args[])  {      
        try
        {  
            //Initializing the Library
            System.out.println("        //Initializing the Library");
            PKCS11 pkcs11 = EIDLib_PKCS11.initLib(); 
            
            //Initializing the Session
            System.out.println("        //Initializing the Session");
            long p11_session = EIDLib_PKCS11.initSession(pkcs11);

            //Signing the data
            System.out.println("        //Signing the data");
            byte[] signature = pkcs11.C_Sign(p11_session, "data".getBytes(Charset.forName("UTF-8")));
            
            //Retrieving the cert
            System.out.println("        //Retrieving the cert");
            X509Certificate cert = EIDLib_PKCS11.getCertFromByteArray(EIDLib_PKCS11.getCitizenAuthCertInBytes());
            PublicKey PKey = cert.getPublicKey();
            
            //Closing the session
            System.out.println("        //Closing the session");
            EIDLib_PKCS11.closeSession();

        }  catch (Throwable e)
        {
            System.out.println("[Catch] Exception: " + e.getMessage());
        }
    }
    
}
