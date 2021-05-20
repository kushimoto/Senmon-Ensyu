import javax.crypto.Cipher;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.io.*;
import java.util.Arrays;

public class CryptoAccount {

	private KeyGenerator kg = null;
	private SecretKey skey = null;
	private byte[] iv = null;
	private String algorithm = null;
	private String mode = null;
	private String transformation = null;
	private Cipher c = null;
	private IvParameterSpec ivParamSpec = null;

	public CryptoAccount(String accnum) {

		try {
			String fname = accnum + ".key"; 

			/* ファイルから読込 */
			ObjectInputStream skeyFile = new ObjectInputStream(new FileInputStream(fname));
			this.skey = (SecretKey)skeyFile.readObject();
			this.algorithm = this.skey.getAlgorithm();
			skeyFile.close();
			System.out.println("[ DEBUG ] algorithm < " + this.algorithm + " >");

		} catch (ClassNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public CryptoAccount(String accnum, String algorithm, int keySize) {

		try {
			String fname = accnum + ".key";

			this.algorithm = algorithm;

			/* KeyGenerator オブジェクトの生成 */
			this.kg = KeyGenerator.getInstance(this.algorithm);

			/* KeyGenerator オブジェクトの初期化 */
			this.kg.init(keySize);

			/* 共通鍵生成 */
			this.skey = this.kg.generateKey();

			/* ファイルに出力　*/
			ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(fname));
			objectOut.writeObject(this.skey);
			objectOut.close();

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

	private byte[] crypto(String accnum, int opmode, byte[] buf, int len, String mode) {

		byte[] sbuf = new byte[1024];
		byte[] rbuf = null;
		byte[] b = null;
		int l = 0;

		try {
			String fname = accnum + ".iv";
			File f = new File(fname);

			if (f.exists() || mode.equals("CBC")) {

				this.transformation = "" + this.algorithm + "/CBC/PKCS5Padding";
				this.c = Cipher.getInstance(this.transformation);

				if (opmode == Cipher.ENCRYPT_MODE) {

					this.c.init(opmode, this.skey);

					FileOutputStream writer = new FileOutputStream(fname);
					this.iv = this.c.getIV();
					writer.write(this.iv, 0, this.iv.length);
					writer.close();

				} else if (opmode == Cipher.DECRYPT_MODE) {

					FileInputStream reader = new FileInputStream(fname);
					l = reader.read(sbuf);
					reader.close();

					this.ivParamSpec = new IvParameterSpec(sbuf, 0, l);
					this.c.init(opmode, this.skey, this.ivParamSpec);

				}

			} else {

				this.transformation = "" + this.algorithm + "/ECB/PKCS5Padding";
				this.c = Cipher.getInstance(this.transformation);
				this.c.init(opmode, this.skey);

			}

			int s = 0;
			int e = this.c.getBlockSize();
			int remnants = len;

			if (opmode == Cipher.ENCRYPT_MODE) {

				rbuf = new byte[(len % e) != 0 ? len + e - (len % e) : len];

			} else {

				rbuf = new byte[512];
				
			}
			while (true) {

					this.c.update(buf, s, e, rbuf, s);
					s += e;
					remnants -= e;

					if (remnants < e) {

						e = remnants;
						break;

					}

				}

			this.c.doFinal(buf, s, e, rbuf, s);

		} catch (ShortBufferException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();

		} catch (NoSuchPaddingException e) {

			e.printStackTrace();

		} catch (InvalidKeyException e) {

			e.printStackTrace();

		} catch (InvalidAlgorithmParameterException e) {

			e.printStackTrace();

		} catch (IllegalBlockSizeException e) {

			e.printStackTrace();

		} catch (BadPaddingException e) {

			e.printStackTrace();

		}

		return rbuf;
	}

	public byte[] encrypto(String accnum, byte[] buf, int len, String mode) {

		byte[] b = this.crypto(accnum, Cipher.ENCRYPT_MODE, buf, len, mode);

		return b;

	}

	public byte[] decrypto(String accnum, byte[] buf, int len, String mode) {

		byte[] b = this.crypto(accnum, Cipher.DECRYPT_MODE, buf, len, mode);

		return b;

	}

}



