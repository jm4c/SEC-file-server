package eIDlib_PKCS11;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.cert.*;
import java.security.cert.PKIXRevocationChecker.Option;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class EIDLib_PKCS11 {

    // Initializes the EIDLib
    public static PKCS11 initLib() throws Exception {

        System.out.println("    //Initializing the eID Lib ...");
        System.loadLibrary("pteidlibj");
        pteid.Init(""); // Initializes the eID Lib
        pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
        PKCS11 pkcs11;

        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");

        String libName = "libbeidpkcs11.so";

        if (-1 != osName.indexOf("Windows")) {
            libName = "pteidpkcs11.dll";
        } else if (-1 != osName.indexOf("Mac")) {
            libName = "pteidpkcs11.dylib";
        }
        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        if (javaVersion.startsWith("1.5.")) {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, null, false});
        } else {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, "C_GetFunctionList", null, false});
        }
        System.out.println("    //Done!\n");

        return pkcs11;
    }

    // Begins a signing session. Asks for authentication
    public static long initSession(PKCS11 pkcs11) throws Exception {
        //Open the PKCS11 session
        System.out.println("    //Opening a PKCS11 session...");
        long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

        // Token login 
        pkcs11.C_Login(p11_session, 1, null);

        // Get available keys
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

        pkcs11.C_FindObjectsInit(p11_session, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

        // points to auth_key
        long signatureKey = keyHandles[0];
        pkcs11.C_FindObjectsFinal(p11_session);

        // initialize the signature method
        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
        mechanism.pParameter = null;
        pkcs11.C_SignInit(p11_session, mechanism, signatureKey);

        System.out.println("    //Done!\n");
        return p11_session;
    }

    // Closes the EIDLib. MUST be called after use
    public static void closeLib(PKCS11 pkcs11, long p11_session) throws Exception {
        System.out.println("    //Closing the PKCS11 session ...");
        System.out.println("    //Closing the eID Lib ...");
        pkcs11.C_CloseSession(p11_session);
        pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
        System.out.println("    //Done!\n");
    }

    //Encodes the n-th certificate in ByteArray form, starting from 0
    private static byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

            //pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a eID Lib
        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }

    //Returns the encoded CITIZEN AUTHENTICATION CERTIFICATE
    public static byte[] getCitizenAuthCertInBytes() {
        return getCertificateInBytes(0); //certificado 0 no Cartao do Cidadao eh o de autenticacao
    }

    //Decodes the certificate from its ByteArray form
    public static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException {
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        X509Certificate cert = (X509Certificate) f.generateCertificate(in);
        return cert;
    }

    public static boolean isCertificateValid(X509Certificate cert) {
        try {

            System.out.println("            //Load the PTEidlibj");

            System.loadLibrary("pteidlibj");
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)


            PKCS11 pkcs11;
            String osName = System.getProperty("os.name");
            String javaVersion = System.getProperty("java.version");
            System.out.println("Java version: " + javaVersion);

            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

            String libName = "libbeidpkcs11.so";

            // access the ID and Address data via the pteidlib
//            System.out.println("Citized Authentication Certificate "+cert);
            System.out.println("===>Issuer: " + cert.getIssuerX500Principal().getName());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");


            String issuerID = cert.getIssuerX500Principal().getName().substring(43, 47);
            System.out.println("Issuer ID = " + issuerID);
            FileInputStream in = new FileInputStream("./authcerts/EC de Autenticacao do Cartao de Cidadao " + issuerID + ".cer");
            Certificate trust = cf.generateCertificate(in);
            
            /* Construct a CertPathBuilder */
            TrustAnchor anchor = new TrustAnchor((X509Certificate) trust, null);
            Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
            trustAnchors.add(anchor);

            X509CertSelector certSelector = new X509CertSelector();
            certSelector.setCertificate(cert);


            PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, certSelector);
            CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
            
            /* Enable usage of revocation lists */
            PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
            rc.setOptions(EnumSet.of(Option.PREFER_CRLS));
            params.addCertPathChecker(rc);


            CertPathBuilderResult cpbr = cpb.build(params);
            System.out.println("CertPathBuilderResult" + cpbr);

            System.out.println("****************************");
            
            /* Now Validate the Certificate Path */

            CertPath cp = cpbr.getCertPath();
            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            CertPathValidatorResult cpvr = cpv.validate(cp, params);
            
            /* If no exception is generated here, it means that validation was successful */
            System.out.println("Validation successful");
            return true;

//            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib

        } catch (Throwable e) {
            System.err.println("[Catch] Exception: " + e.getMessage());
            e.printStackTrace();
            return false;

        }

    }

}
