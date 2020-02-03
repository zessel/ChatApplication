package edu.temple.chatapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KeyService extends Service {

    private final IBinder binder = new LocalBinder();
    private KeyPair keyPair;
    private KeyPairGenerator keyPairGenerator;
    private KeyFactory keyFactory;
    private Map<String, RSAPublicKey> keyMaps = new HashMap<>();
    //private KeyStore keyStore;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public KeyService() throws NoSuchAlgorithmException, KeyStoreException {
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyFactory = KeyFactory.getInstance("RSA");
        //keyStore = KeyStore.getInstance("ServiceKeyStore");
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //editor = sharedPref.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        return binder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        KeyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return KeyService.this;
        }
    }


    public KeyPair getMyKeyPair() {
        this.keyPair = keyPairGenerator.generateKeyPair();
        editor.putString("myPublic", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        editor.putString("myPrivate", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        editor.commit();
        return keyPair;
    }

    public void storePublicKey (String partnerName, String publicKey) throws InvalidKeySpecException {
        //X509EncodedKeySpec partnerKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        //RSAPublicKey partnerRSAPublicKey = (RSAPublicKey) keyFactory.generatePublic(partnerKeySpec);
        //keyMaps.put(partnerName, partnerRSAPublicKey);
        //keyStore.setEntry(partnerName, partnerRSAPublicKey, );
        editor.putString(partnerName, publicKey).commit();
    }

    public RSAPublicKey getPublicKey(String partnerName) throws InvalidKeySpecException {

        RSAPublicKey returnKey = null;
        if (sharedPref.contains(partnerName))
            returnKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(
                    Base64.getDecoder().decode(sharedPref.getString(partnerName, null))));
        return returnKey;
    }

    public void resetMyKeyPair() {
        keyPair = keyPairGenerator.generateKeyPair();
        editor.putString("myPublic", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        editor.putString("myPrivate", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        editor.commit();
    }

    public void resetKey(String partnerName) {
        editor.remove(partnerName).commit();
        //keyMaps.remove(partnerName);
    }
}
