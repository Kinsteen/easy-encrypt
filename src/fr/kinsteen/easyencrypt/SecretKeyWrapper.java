package fr.kinsteen.easyencrypt;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecretKeyWrapper {
	public final static int KEY_SIZE = 256;
	
	private String location;
	private SecretKey secretKey;
	
	public SecretKeyWrapper(String location, SecretKey secretKey) {
		super();
		this.location = location;
		this.secretKey = secretKey;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public SecretKey getSecretKey() {
		return secretKey;
	}
	
	public void setSecretKey(SecretKey publicKey) {
		this.secretKey = secretKey;
	}
	
	public void saveKeys() {
		try (FileOutputStream out = new FileOutputStream(this.location + "/easy-encrypt.key")) {
		    out.write(Base64.getEncoder().encode(this.getSecretKey().getEncoded()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static SecretKeyWrapper generateKeys(String location) {
		try {
			KeyGenerator kpg = KeyGenerator.getInstance("AES");
			kpg.init(KEY_SIZE);
			SecretKey secretKey = kpg.generateKey();

			SecretKeyWrapper kpw = new SecretKeyWrapper(location, secretKey);
			kpw.saveKeys();
			return kpw;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static SecretKeyWrapper loadKeys(String location) {
		try {
			byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(location + "/easy-encrypt.key")));
			return new SecretKeyWrapper(location, new SecretKeySpec(bytes, "AES"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
