import java.io.*;
import java.net.*;
import java.util.Random;
import java.security.*;
import javax.crypto.*;

public class ATM_Server {

	public static void main(String args[]) {

		byte[] buf = null;

		/* 待ち受けポート番号 */
		int PORT = 49125;

		/* 送受信文字列保管用 */
		String sndmsg = null;
		String rcvmsg = null;

		/* 操作番号代入用 */
		int controlNum = 0;

		/* PassAccountインスタンス用 */
		PassAccount pa = null;

		/* CryptoAccountインスタンス用 */
		CryptoAccount ca = null;

		/* 口座情報用変数 */
		String bankName = null;
		String branchName = null;
		String name = null;
		String passwd = null;
		String accnum = null;

		/* 口座番号生成用 */
		Random r = new Random();

		/* ファイルインスタンス用 */
		File f = null;

		String algorithm = null;
		String keySize = null;
		String mode = null;

		try {
			/* ソケット準備と通信確立 */
			ServerSocket svsock = new ServerSocket(PORT);
			Socket socket = svsock.accept();

			/* 送受信ストリーム用変数の準備 */
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			/* ファイル入出力ストリーム用変数の準備 */
			FileInputStream reader = null;
			FileOutputStream writer = null;

			/* オブジェクトファイル読み書き用変数の準備 */
			ObjectInputStream objectIn = null;
			ObjectOutputStream objectOut = null;

			loop1:while(true) {
				/* 正常なリクエストが送信されるまで絶対に諦めない */
				loop2:while(true) {

					/* 初期操作受信 */
					rcvmsg = communicator(in);
					controlNum = Integer.parseInt(rcvmsg);

					switch (controlNum) {

						case 0:
							/* 口座開設開始メッセージ */
							System.out.println("[ DEBUG ] 口座開設開始");

							/* 応答 */
							communicator(out, "OK");

							/* 銀行名受信 */
							bankName = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 銀行名受信 < " + bankName + " >");
							/* 支店名受信 */
							branchName = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 支店名受信 < " + branchName + " >");

							/* 口座名義受信 */
							name = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 口座名義受信 < " + name + " >");

							/* 口座パスワード受信 */
							passwd = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] パスワード受信 < " + passwd + " >");

							/* 口座開設(インスタンス生成) */
							pa = new PassAccount(bankName, branchName, name, passwd);

							/* 口座番号送信 */
							communicator(out, pa.accountNo);

							/* 口座開設成功通知送信 */
							communicator(out, "OK");

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 口座開設完了");


							/* アルゴリズムを受信 */
							algorithm = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] アルゴリズムを受信");

							/* 鍵サイズを受信 */
							keySize = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 鍵サイズを受信");

							/* 暗号化モードを受信 */
							mode = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 暗号化モードを受信");

							/* 暗号化準備 */
							ca = new CryptoAccount(pa.accountNo, algorithm, Integer.parseInt(keySize));

							/* シリアライズ */
							buf = serializer(pa);

							/* 暗号化 */
							buf = ca.encrypto(pa.accountNo, buf, buf.length, mode);

							/* ファイル書き出し */
							save(buf, pa.accountNo);

							break loop2;

						case 1:
							/* デバッグ出力 */
							System.out.println("[ DEBUG ] ログイン開始");

							/* 応答 */
							communicator(out, "OK");

							/* 口座番号受信 */
							accnum = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 口座番号受信 < " + accnum + " >");

							/* 復号化準備 */
							ca = new CryptoAccount(accnum);

							/* ファイル読み込み */
							buf = readCryptedFile(accnum);

							/* 復号化 */
							buf = ca.decrypto(accnum, buf, buf.length, "AUTO");

							/* デシリアライズ */
							pa = deserializer(buf);

							/* 認証方式を受信 */
							rcvmsg = communicator(in);

							if (rcvmsg.equals("1")) {

								/* パスワード受信 */
								rcvmsg = communicator(in);

								/* パスワード認証 */
								sndmsg = pa.pwAuth(rcvmsg);

							} else if (rcvmsg.equals("2")) {

								/* チャレンジ(乱数)生成 */
								sndmsg = String.valueOf(r.nextInt(1000));
								System.out.println("[ DEBUG ] チャレンジ < " + sndmsg + " >");

								/* チャレンジ(乱数)送信 */
								communicator(out, sndmsg);

								/* ハッシュ値受信 */
								byte[] cliDigest = new byte[32];
								in.read(cliDigest);

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] ハッシュ値受信");

								/* CHAP認証 */
								sndmsg = pa.chap(cliDigest, sndmsg);

							}

							/* 認証結果送信 */
							communicator(out, sndmsg);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 認証結果 : " + sndmsg);

							if (sndmsg.equals("OK")) {

								/* 利用者情報を取得し、応答を送信 */
								sndmsg = pa.getAccountInfo();
								communicator(out, sndmsg);

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 利用者情報送信");

								break loop2;

							} else {

								break;

							}

						default:

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 切断要求受信");

							/* エラー応答 */
							communicator(out, "NG");

							break loop1;

					}

				}

				/* 取引終了まで操作に関するリクエストを受け付ける */
				while(true) {

					/* 操作番号受信 */
					rcvmsg = communicator(in);
					controlNum = Integer.parseInt(rcvmsg);

					/* デバッグ出力 */
					System.out.println("[ DEBUG ] 操作番号受信 < " + rcvmsg + " >");

					switch (controlNum) {

						case 1:

							/* 応答 */
							communicator(out, "OK");

							/* 預金額を受信 */
							rcvmsg = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 預金額受信 < " + rcvmsg + " >");

							/* 預金操作を実行し、応答を送信 */
							sndmsg = pa.execDeposit(Integer.parseInt(rcvmsg));
							communicator(out, sndmsg);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 預金操作 < " + sndmsg + " >");
							break;

						case 2:
							/* 応答 */
							communicator(out, "OK");

							/* 払い出し金額を受信 */
							rcvmsg = communicator(in);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 払い出し額受信 < " + rcvmsg + " >");

							/* 払い出し操作を実行し、応答を送信 */
							sndmsg = pa.execDraw(Integer.parseInt(rcvmsg));
							communicator(out, sndmsg);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 払い出し額送信 < " + sndmsg + " >");
							break;

						case 3:
							/* 応答 */
							communicator(out, "OK");

							/* 残高照会を実行し、応答を送信 */
							sndmsg = pa.execGetBalance();
							communicator(out, sndmsg);

							/* デバッグ出力 */
							System.out.println("[ DEBUG ] 残高送信 < " + sndmsg + " >");

							break;

						case 4:
							/* 応答 */
							communicator(out, "OK");

							/* 口座番号受信 */
							accnum = communicator(in);

							/* 同一口座でないことを確認 */
							if (pa.accountNo.equals(accnum)) {

								/* 応答送信 */
								communicator(out, "NG");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 同一口座への振り替えを検知");

							} else {

								/* 応答送信 */
								communicator(out, "OK");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 振替先が異なることを確認");

							}

							/* 振替先口座用変数宣言 */
							PassAccount tAccount = null;

							f = new File(accnum);

							/* 口座の存在確認 */
							if (f.exists()) {

								/* 復号化準備 */
								ca = new CryptoAccount(accnum);

								/* ファイル読み込み */
								buf = readCryptedFile(accnum);

								/* 復号化 */
								buf = ca.decrypto(accnum, buf, buf.length, "AUTO");

								/* デシリアライズ */
								tAccount = deserializer(buf);

								/* 応答送信 */
								communicator(out, "OK");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 振替先口座のデシリアライズ完了");

							} else {

								/* 応答送信 */
								communicator(out, "NG");
								
								/* デバッグ出力 */
								System.out.print("[ DEBUG ] 振替先口座が存在しません！\n\n");
								break;

							}

							/* 銀行の確認 */
							if (pa.bankName.equals(tAccount.bankName)) {

								/* 応答送信 */
								communicator(out, "OK");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 銀行名一致");

							} else {

								/* 応答送信 */
								communicator(out, "NG");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 銀行名不一致");

								break;

							}

							/* 支店の確認 */
							if (pa.branchName.equals(tAccount.branchName)) {

								/* 応答送信 */
								communicator(out, "OK");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 支店名一致");

							} else {

								/* 応答送信 */
								communicator(out, "NG");

								/* デバッグ出力 */
								System.out.println("[ DEBUG ] 支店名不一致");

								break;

							}

							/* 振替金額受信 */
							rcvmsg = communicator(in);

							/* 振替元の残高を減らす */
							int amount;
							amount = pa.draw(Integer.parseInt(rcvmsg));

							/* 振替先の残高を増やす */
							tAccount.deposit(amount);

							/* シリアライズ */
							buf = serializer(tAccount);

							/* 暗号化 */
							buf = ca.encrypto(tAccount.accountNo, buf, buf.length, "AUTO");

							/* ファイル書き出し */
							save(buf, tAccount.accountNo);

							/* 暗号化準備 */
							ca = new CryptoAccount(pa.accountNo);

							/* シリアライズ */
							buf = serializer(pa);

							/* 暗号化 */
							buf = ca.encrypto(pa.accountNo, buf, buf.length, "AUTO");

							/* ファイル書き出し */
							save(buf, pa.accountNo);

						

							break;

						default:
							/* 切断要求受信表示 */
							System.out.println("[ DEBUG ] 切断要求受信");

							/* エラー応答 */
							communicator(out, "NG");

							if (pa != null) {

								/* シリアライズ */
								buf = serializer(pa);

								/* 暗号化 */
								buf = ca.encrypto(pa.accountNo, buf, buf.length, "AUTO");

								/* ファイル書き出し */
								save(buf, pa.accountNo);
							}

							break loop1;


					}


				}

			}
			
			/* ストリームを閉じる */

			if (out != null) {

				out.close();
				System.out.println("[ DEBUG ] out クローズ");
				
			}

			if (in != null) {

				in.close();
				System.out.println("[ DEBUG ] in クローズ");

			}

		} catch (SocketException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	private static String communicator (OutputStream out, String msg) {

		byte[] buf = new byte[1024];
		int len = 0;
		int off = 0;

		try {
			buf = msg.getBytes();
			len = buf.length;
			out.write(buf, off, len);

		} catch (SocketException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return "OK";

	}

	private static String communicator (InputStream in) {

		byte[] buf = new byte[1024];
		int len = 0;
		int off = 0;

		try {

			len = in.read(buf);

		} catch (SocketException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return (new String(buf, off, len));

	}

	private static byte[] serializer (PassAccount pa) {

		byte[] retObject = null;

		try {
			/* シリアライズ */
			ByteArrayOutputStream byteos = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteos);
			objectOut.writeObject(pa);
			retObject = byteos.toByteArray();

			/* デバッグ出力 */
			System.out.println("[ DEBUG ] シリアライズ完了");

			/* ストリームを閉じる */
			byteos.close();
			System.out.println("[ DEBUG ] byteos クローズ");
			objectOut.close();
			System.out.println("[ DEBUG ] objectOut クローズ");

		} catch (IOException e) {

			e.printStackTrace();

		}

		return retObject;

	}

	private static void save (byte[] b, String accnum) {

		try {
			FileOutputStream writer = new FileOutputStream(accnum);
			writer.write(b, 0, b.length);

			writer.close();
			System.out.println("[ DEBUG ] writer クローズ");

			/* デバッグ出力 */
			System.out.println("[ DEBUG ] ファイルへの出力完了");

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	private static byte[] readCryptedFile (String accnum) {

		byte[] b = new byte[256];
		byte[] buf = null;
		int len;

		try {
			FileInputStream reader = new FileInputStream(accnum);
			len = reader.read(b);
			buf = new byte[len];
			for (int i = 0; i < buf.length; i++) {
				buf[i] = b[i];
			}
			reader.close();

		} catch (IOException e) {

			e.printStackTrace();

		}

		return buf;

	}

	private static PassAccount deserializer (byte[] b) {

		PassAccount pa = null;

		try {
			byte[] buf = null;
			int i;

			for (i = 0; b[i] == 0; i++);
			buf = new byte[b.length - i];
			for (int j = 0; j < buf.length; j++) {
				buf[j] = b[j + i];
			}
			/* デシリアライズ */
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			ObjectInputStream objectIn = new ObjectInputStream(bais);
			pa = (PassAccount)objectIn.readObject();

			/* デバッグ出力 */
			System.out.println("[ DEBUG ] デシリアライズ完了");

			/* ストリームを閉じる */
			bais.close();
			System.out.println("[ DEBUG ] bais クローズ");
			objectIn.close();
			System.out.println("[ DEBUG ] objectIn クローズ");

		} catch (IOException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return pa;

	}

}
