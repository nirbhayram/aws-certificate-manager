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

public class ExportCertificateCustom {

    private static final String PATH_TO_PASSPHRASE = "./.passphrase";

    private static final String PRIVATE_CERTIFICATE_ARN = "arn:aws:acm:us-east-1:350947464502:certificate/1b37a7df-4fbb-4bdd-a4fb-253335ab22e5";

    public static void main(String[] args) throws Exception {

        // Retrieve your credentials from the C:\Users\name\.aws\credentials file in Windows
        // or the ~/.aws/credentials in Linux.
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

        // Initialize a file descriptor for the passphrase file.
        RandomAccessFile file_passphrase = null;

        // Initialize a buffer for the passphrase.
        ByteBuffer buf_passphrase = null;

        // Create a file stream for reading the private key passphrase.
        try {
            file_passphrase = new RandomAccessFile(PATH_TO_PASSPHRASE, "r");
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (SecurityException ex) {
            throw ex;
        } catch (FileNotFoundException ex) {
            throw ex;
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
            throw ex;
        }

        // Create a request object.
        ExportCertificateRequest req = new ExportCertificateRequest();

        // Set the certificate ARN.
        req.withCertificateArn(PRIVATE_CERTIFICATE_ARN);

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

