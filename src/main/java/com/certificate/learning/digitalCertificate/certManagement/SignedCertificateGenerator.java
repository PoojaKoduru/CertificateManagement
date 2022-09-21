package com.certificate.learning.digitalCertificate.certManagement;

import com.certificate.learning.digitalCertificate.CertificateUtils;
import com.certificate.learning.digitalCertificate.EncryptionDecryptionAES;
import com.certificate.learning.digitalCertificate.bean.Certificates;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;


@SuppressWarnings("deprecation")
public class SignedCertificateGenerator {


    public Certificates certificates1 = new Certificates();

    private static final String CERTIFICATE_ALGORITHM = CertificateUtils.CERTIFICATE_ALGORITHM;

    private static final int CERTIFICATE_BITS = CertificateUtils.CERTIFICATE_BITS;
    private static final double Issue_Years = CertificateUtils.Issue_Years;

    static {
        // adds the Bouncy castle provider to java security
        //BouncyCastle acts similar to keytool to generate certificate
        Security.addProvider(new BouncyCastleProvider());
    }

    public X509Certificate createSignedCertificate(X509Certificate certificate, PrivateKey privateKey, String Designation_Name,String CERTIFICATE_ALIAS) throws Exception{
        X509Certificate cert = null;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(CERTIFICATE_ALGORITHM);
        //key is generated with the number of bits specified...SecureRandom() is PRNG
        keyPairGenerator.initialize(CERTIFICATE_BITS, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        // GENERATE THE X509 CERTIFICATE
        X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        //issuer is ca cerificate
        v3CertGen.setIssuerDN(certificate.getIssuerX500Principal());
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date((long) (System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*Issue_Years))));
        //designation name contents are passed while method call
        v3CertGen.setSubjectDN(new X509Principal(Designation_Name));
        v3CertGen.setPublicKey(keyPair.getPublic());
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        //for self signed cert
        v3CertGen.addExtension(X509Extensions.BasicConstraints.getId(),true,new BasicConstraints(false));
        //for ca cert...place in trusted dir
        //v3CertGen.addExtension(X509Extensions.BasicConstraints.getId(),true,new BasicConstraints(true));
        //sign clients cert with ca's private key
        cert = v3CertGen.generateX509Certificate(privateKey);
        //but save cert with clients private key
        saveCert(cert,keyPair.getPrivate(),CERTIFICATE_ALIAS);
        return cert;
    }


    public Certificates saveFile(X509Certificate cert, String Filename) throws Exception {
        final FileOutputStream os = new FileOutputStream(Filename);
        os.write("-----BEGIN CERTIFICATE-----\n".getBytes("US-ASCII"));
        os.write(Base64.encode(cert.getEncoded()));
        os.write("-----END CERTIFICATE-----\n".getBytes("US-ASCII"));
        //certificateRepository.save(certificates1);
        os.close();
        System.out.println();
        return certificates1;
    }


    private void saveCert(X509Certificate cert, PrivateKey key,String CERTIFICATE_ALIAS) throws Exception {
        String s = new String(Base64.encode(cert.getEncoded()));
        String enc = EncryptionDecryptionAES.encrypt(s,cert.getPublicKey());
        certificates1.setCertificatetest(enc);
        certificates1.setCaflag("F");
        certificates1.setAliasname(CERTIFICATE_ALIAS);
        certificates1.setPrivatekey(new String(Base64.encode(key.getEncoded())));
        certificates1.setPublickey(new String(Base64.encode(cert.getPublicKey().getEncoded())));

    }}


