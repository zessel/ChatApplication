package edu.temple.chatapplication;

import android.content.Intent;
import android.os.IBinder;
import android.util.Base64;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ServiceTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class KeyServiceTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    /**
     *  Evidently this is all depreciated and android just keeps it in their official
     *  documentation ???

     @Test
    public void testWithStartedService() {
        mServiceRule.startService(new Intent(InstrumentationRegistry.getTargetContext(),
                        MyService.class));
        // test code
    }
     */

    @Test
    public void testWithBoundService() throws TimeoutException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {


        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Cipher cipher = Cipher.getInstance("RSA");

        // Create the service Intent.
        Intent serviceIntent =
                new Intent(ApplicationProvider.getApplicationContext(),
                        KeyService.class);

        // Bind the service and grab a reference to the binder.
        IBinder binder = serviceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        KeyService service = ((KeyService.LocalBinder) binder).getService();

        System.out.println("Testing getMyKeyPair()...");
        assertThat(service.getMyKeyPair(), is(any(KeyPair.class)));



        KeyPair testKeyPair = service.getMyKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) testKeyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) testKeyPair.getPrivate();

        String testString = "Hello KeyService";
        System.out.println("Encrypting '" + testString + "'...");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBinary = cipher.doFinal(testString.getBytes());
        System.out.println(Base64.encodeToString(encryptedBinary, Base64.DEFAULT));
        System.out.println("Decrypting...");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBinary = cipher.doFinal(encryptedBinary);
        String decryptedString = new String(decryptedBinary);
        System.out.println(decryptedString);

        assertEquals(testString, decryptedString);

        service.storePublicKey("testing", java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        assertEquals(keyPair.getPublic(), service.getPublicKey("testing"));

        service.resetKey("testing");
        assertEquals(null, service.getPublicKey("testing"));
    }

}
