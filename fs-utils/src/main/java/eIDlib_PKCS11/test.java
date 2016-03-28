package eIDlib_PKCS11;

import java.nio.charset.Charset;
import sun.security.pkcs11.wrapper.PKCS11;

public class test {
    
    public static void main(String args[])  {
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();       
        try
        {
            eIDLib_PKCS11_test.initLib();    
            PKCS11 pkcs11 = eIDLib_PKCS11_test.initPKCS11();
            long p11_session = eIDLib_PKCS11_test.initSession(pkcs11);

            // sign
            System.out.println("            //sign");
            byte[] signature = pkcs11.C_Sign(p11_session, "data".getBytes(Charset.forName("UTF-8")));
            System.out.println("            //signature:"+encoder.encode(signature));
            
            eIDLib_PKCS11_test.closeSession();

        }  catch (Throwable e)
        {
            System.out.println("[Catch] Exception: " + e.getMessage());
        }
    }
    
}
