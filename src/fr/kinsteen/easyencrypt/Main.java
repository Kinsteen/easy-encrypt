package fr.kinsteen.easyencrypt;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends JFrame {
	private static final long serialVersionUID = 5531302332028610095L;
	
	public static final String DEFAULT_KEY_LOCATION = ".";  // Where the executable is
	private SecretKeyWrapper kpw = null;

	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		super();
		
	    try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception ex) {
	        ex.printStackTrace();
	    }

		JButton encryptBtn = new JButton("Encrypt");
		JButton decryptBtn = new JButton("Decrypt");

		encryptBtn.setPreferredSize(new Dimension(120,50));
		decryptBtn.setPreferredSize(new Dimension(120,50));

		Insets margins = new Insets(10,10,10,10);
		encryptBtn.setMargin(margins);
		decryptBtn.setMargin(margins);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = margins;

		encryptBtn.addActionListener(e -> {
		    final JFileChooser fc = new JFileChooser();
		    fc.setMultiSelectionEnabled(true);
			fc.setFileFilter(new InvertedFileFilter(new FileNameExtensionFilter("Encrypted file", "enc")));
		    int returnVal = fc.showOpenDialog(this);
		    
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File[] files = fc.getSelectedFiles();

				try {
					for (File f : files) {
						System.out.println("Opening: " + f.getName() + ".");
						Cipher cipher = Cipher.getInstance("AES");
						cipher.init(Cipher.ENCRYPT_MODE, kpw.getSecretKey());
						File encFile = new File(f.getAbsolutePath() + ".enc");
						try (FileInputStream in = new FileInputStream(f);
							 FileOutputStream out = new FileOutputStream(encFile)) {
							processFile(cipher, in, out);
							JOptionPane.showMessageDialog(this, "Encrypted successfully!");
						}
					}
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "There was a problem with the encryption. No files were harmed.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		decryptBtn.addActionListener(e -> {
		    final JFileChooser fc = new JFileChooser();
		    fc.setMultiSelectionEnabled(true);
		    fc.setFileFilter(new FileNameExtensionFilter("Encrypted file", "enc"));
		    int returnVal = fc.showOpenDialog(this);
		    
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File[] files = fc.getSelectedFiles();

				try {
					for (File f : files) {
						System.out.println("Opening: " + f.getName() + ".");
						Cipher cipher = Cipher.getInstance("AES");
						cipher.init(Cipher.DECRYPT_MODE, kpw.getSecretKey());
						String absPath = f.getAbsolutePath();
						File encFile = new File(absPath.substring(0, absPath.length() - 4));
						try (FileInputStream in = new FileInputStream(f);
							 FileOutputStream out = new FileOutputStream(encFile)) {
							processFile(cipher, in, out);
							JOptionPane.showMessageDialog(this, "Decrypted successfully!");
						}
					}
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "There was a problem with the decryption. No files were harmed.", "Error", JOptionPane.ERROR_MESSAGE);
				}
	        }
		});

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.add(encryptBtn, c);
		panel.add(decryptBtn, c);
		setContentPane(panel);
		setSize(320,120);
		setTitle("Easy Encrypt");
		setVisible(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Preferences prefs = Preferences.userNodeForPackage(Main.class);

		final String PREF_NAME = "keys_location";
		String savedLocation = prefs.get(PREF_NAME, DEFAULT_KEY_LOCATION);

		if (Files.notExists(Paths.get(savedLocation + "/easy-encrypt.key"))) {
			System.err.println("Keys do not exist");
			JOptionPane.showMessageDialog(this, "No key was found. Please pick a place to save/load it.");
			final JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogTitle("Choose where to save keys");
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.CANCEL_OPTION) {
				JOptionPane.showMessageDialog(this, "You need to choose a folder to save/load your key.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}

			// Set the value of the preference
			String newValue = fc.getSelectedFile().getAbsolutePath();
			prefs.put(PREF_NAME, newValue);
			savedLocation = fc.getSelectedFile().getAbsolutePath();
		}
		
		if (Files.exists(Paths.get(savedLocation + "/easy-encrypt.key"))) {
			System.out.println("Keys exists!");
			kpw = SecretKeyWrapper.loadKeys(savedLocation);
			System.out.println(kpw.getSecretKey().getEncoded()[5]);
		} else {
			System.out.println("Saving key at " + savedLocation);
			kpw = SecretKeyWrapper.generateKeys(savedLocation);
		}
	}

	private void processFile(Cipher cipher, FileInputStream in, FileOutputStream out) {
		try {
			byte[] ibuf = new byte[256];
			int len;
			while ((len = in.read(ibuf)) != -1) {
				byte[] obuf = cipher.update(ibuf, 0, len);
				if (obuf != null) out.write(obuf);
			}
			byte[] obuf = cipher.doFinal();
			if ( obuf != null ) out.write(obuf);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}

}
