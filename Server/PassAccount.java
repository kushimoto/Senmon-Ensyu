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

			/* ハッシュ値を得るためのインスタンスを生成 */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			/* パスワードとチャレンジを結合した文字列を準備 */
			String base = "" + this.password + r;
			/* 文字列をハッシュ化 */
			byte[] srvDigest = md.digest(base.getBytes());

			/* --------------- デバッグ用 --------------- */
			StringBuilder sb1 = new StringBuilder();
			for (byte d : srvDigest) {
				sb1.append(String.format("%02X", d));
			}
			String str1 = sb1.toString();
			System.out.println("[ DEBUG ] 　　サーバーハッシュ値 : " + str1);
			
			StringBuilder sb2 = new StringBuilder();
			for (byte d : cliDigest) {
				sb2.append(String.format("%02X", d));
			}
			String str2 = sb2.toString();
			System.out.println("[ DEBUG ] クライアントハッシュ値 : " + str2);
			/* ------------------------------------------ */

			/* クライアントのハッシュ値とサーバーのハッシュ値を比較 */
			if (md.isEqual(cliDigest, srvDigest)) {

				return "OK";

			}

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();

		} 

		return "NO";

	}

}
