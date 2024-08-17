package io.codlibs.opensearch.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.testcontainers.shaded.org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class CertificateUtil {

    private static final List<String> subjectAltNamesList = List.of(
            "localstack",
            "localhost",
            "localhost.localstack.cloud"
    );

    public static void generateCertificateAndTrustStore(String certificateFilePath,
                                                   String truststoreFilePath,
                                                   String truststorePassword) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Generate Key Pair
        KeyPair keyPair = generateKeyPair();

        // Generate certificate
        X509Certificate certificate = generateCertificate(keyPair);

        // Write private key and certificate to a file
        writePrivateKeyAndCertificateToFile(keyPair, certificate, certificateFilePath);

        // Build Keystore
        buildTrustStoreAndAddCertificate(certificate, truststoreFilePath, truststorePassword);
    }

    private static void writePrivateKeyAndCertificateToFile(KeyPair keyPair, X509Certificate certificate, String certificateFilePath) throws IOException,
            CertificateEncodingException {
        try (PemWriter pemWriter = new PemWriter(new FileWriter(certificateFilePath))) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
            pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
        }
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate generateCertificate(KeyPair keyPair) throws Exception {
        // Get RSA key pair
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Issuer and Subject
        String subjectLine = "C=IND, ST=DEL, L=DEL, O=codlibs, CN=localstack";
        X500Name issuer = new X500Name(subjectLine);
        X500Name subject = new X500Name(subjectLine);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        // Validity Period
        Date notBefore = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date notAfter = calendar.getTime();

        // Initiate Cert Builder
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())
        );

        // Add certificate subjectAltNames
        GeneralNames subjectAltNames = new GeneralNames(
                subjectAltNamesList.stream().map(name -> new GeneralName(GeneralName.dNSName, name))
                        .toArray(GeneralName[]::new)
        );
        certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);

        // Cert Signing
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(privateKey);

        // Build Certificate
        X509CertificateHolder certHolder = certBuilder.build(signer);

        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certHolder);
    }

    private static void buildTrustStoreAndAddCertificate(X509Certificate certificate, String truststoreFilePath, String truststorePassword) throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        keystore.setCertificateEntry("localhost", certificate);
        keystore.store(new FileOutputStream(truststoreFilePath), truststorePassword.toCharArray());
        Thread.sleep(1000);
    }
}
