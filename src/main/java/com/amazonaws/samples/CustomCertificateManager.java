//https://docs.aws.amazon.com/acm/latest/userguide/sdk-request.html
//https://docs.aws.amazon.com/acm/latest/userguide/sdk-export.html

package com.amazonaws.samples;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClientBuilder;
import com.amazonaws.services.certificatemanager.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This sample demonstrates how to use the RequestCertificate function in the AWS Certificate
 * Manager service.
 * <p>
 * Input parameters:
 * DomainName - FQDN of your site.
 * DomainValidationOptions - Domain name for email validation.
 * IdempotencyToken - Distinguishes between calls to RequestCertificate.
 * SubjectAlternativeNames - Additional FQDNs for the subject alternative names extension.
 * <p>
 * Output parameter:
 * Certificate ARN - The Amazon Resource Name (ARN) of the certificate you requested.
 */

public class CustomCertificateManager {

    private static final String PRIVATE_CERTIFICATE_AUTHORITY = "";

    private static final String DOMAIN = "nirbhay.io";

    private static final String IDEMPOTENCY_TOKEN = "1Q2W3E4R";

    private static final String PATH_TO_PASSPHRASE = "./.passphrase";

    public static void main(String[] args) {

        // Retrieve your credentials from the C:\Users\name\.aws\credentials file in Windows
        // or the ~/.aws/credentials file in Linux.
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception ex) {
            throw new AmazonClientException("Cannot load your credentials from file.", ex);
        }

        // Create a client.
        AWSCertificateManager client = AWSCertificateManagerClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        // Create a request object and set the input parameters.
        RequestCertificateRequest req = new RequestCertificateRequest();
        req.setDomainName(DOMAIN);
        req.setCertificateAuthorityArn(PRIVATE_CERTIFICATE_AUTHORITY);
        req.setIdempotencyToken(IDEMPOTENCY_TOKEN);

        // Create a result object and display the certificate ARN.
        RequestCertificateResult result = null;
        try {
            result = client.requestCertificate(req);
            exportCertificate(client, result.getCertificateArn());
        } catch (InvalidDomainValidationOptionsException ex) {
            throw ex;
        } catch (LimitExceededException ex) {
            throw ex;
        }
    }

    private static void exportCertificate(AWSCertificateManager client, String certificateArn) {
        // Initialize a file descriptor for the passphrase file.
        RandomAccessFile file_passphrase = null;

        // Initialize a buffer for the passphrase.
        ByteBuffer buf_passphrase = null;

        // Create a file stream for reading the private key passphrase.
        try {
            file_passphrase = new RandomAccessFile(PATH_TO_PASSPHRASE, "r");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        // Create a channel to map the file.
        FileChannel channel_passphrase = file_passphrase.getChannel();

        // Map the file to the buffer.
        try {
            buf_passphrase = channel_passphrase.map(FileChannel.MapMode.READ_ONLY, 0, channel_passphrase.size());

            // Clean up after the file is mapped.
            channel_passphrase.close();
            file_passphrase.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Create a request object.
        ExportCertificateRequest req = new ExportCertificateRequest();

        // Set the certificate ARN.
        req.withCertificateArn(certificateArn);

        // Set the passphrase.
        req.withPassphrase(buf_passphrase);

        // Export the certificate.
        ExportCertificateResult result = null;

        try {
            result = client.exportCertificate(req);
        } catch (InvalidArnException ex) {
            throw ex;
        } catch (InvalidTagException ex) {
            throw ex;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        }

        // Clear the buffer.
        buf_passphrase.clear();

        // Display the certificate and certificate chain.
        String certificate = result.getCertificate();
        System.out.println(certificate);

        String certificate_chain = result.getCertificateChain();
        System.out.println(certificate_chain);

        // This example retrieves but does not display the private key.
        String private_key = result.getPrivateKey();
    }

}

