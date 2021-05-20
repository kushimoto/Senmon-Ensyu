import java.io.Serializable;
import java.security.*;
import java.io.*;

public class PassAccount extends Account implements Serializable {

	private String password = null;

	public PassAccount(String bankName, String branchName, String name, String pw) {

		super(bankName, branchName, name);
		this.password = pw;

	}

	public String pwAuth(String pw) {

		if (this.password.equals(pw)) {

			return "OK";

		}

		return "NG";

	}

	public String chap(byte[] cliDigest, String r) {

		try {

			/* �n�b�V���l�𓾂邽�߂̃C���X�^���X�𐶐� */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			/* �p�X���[�h�ƃ`�������W��������������������� */
			String base = "" + this.password + r;
			/* ��������n�b�V���� */
			byte[] srvDigest = md.digest(base.getBytes());

			/* --------------- �f�o�b�O�p --------------- */
			StringBuilder sb1 = new StringBuilder();
			for (byte d : srvDigest) {
				sb1.append(String.format("%02X", d));
			}
			String str1 = sb1.toString();
			System.out.println("[ DEBUG ] �@�@�T�[�o�[�n�b�V���l : " + str1);
			
			StringBuilder sb2 = new StringBuilder();
			for (byte d : cliDigest) {
				sb2.append(String.format("%02X", d));
			}
			String str2 = sb2.toString();
			System.out.println("[ DEBUG ] �N���C�A���g�n�b�V���l : " + str2);
			/* ------------------------------------------ */

			/* �N���C�A���g�̃n�b�V���l�ƃT�[�o�[�̃n�b�V���l���r */
			if (md.isEqual(cliDigest, srvDigest)) {

				return "OK";

			}

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();

		} 

		return "NO";

	}

}
