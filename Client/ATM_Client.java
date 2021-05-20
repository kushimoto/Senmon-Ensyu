import java.io.*;
import java.net.*;
import javax.crypto.*;
import java.security.*;

public class ATM_Client {

	public static void main (String[] args) {

		byte[] buf = new byte[1024];
		int PORT = 49125;
		String IP = "127.0.0.1";
		String sndmsg = null;
		String rcvmsg = null;
		String r = null;


		int controlNum = 0;

		try {
			InetAddress IPaddr = InetAddress.getByName(IP);
			Socket socket = new Socket(IPaddr, PORT);

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			loop1:while(true) {
				loop2:while(true) {

					System.out.println("******************************");
					System.out.println("*                            *");
					System.out.println("*       大日本銀行組合       *");
					System.out.println("*                            *");
					System.out.println("******************************");

					System.out.print("\n");
					System.out.println("各行のATMは大日本銀行組合の直轄となり、統一されました。");
					System.out.print("\n");
					System.out.println("操作番号をお選びください ) 0 : 口座開設、1 : ログイン");
					System.out.print(">");

					/* 読み込んだ番号を送信 */
					sndmsg = br.readLine();
					communicator(out, sndmsg);

					/* サーバーからの応答を受信 */
					rcvmsg = communicator(in);


					if (rcvmsg.equals("OK")) {

						switch (Integer.parseInt(sndmsg)) {

							case 0:
								System.out.println("新規口座開設を開始します。");
								System.out.println("必要事項を記入してください。");
								System.out.print("\n");

								/* 銀行名送信 */
								System.out.print("銀行名：");
								communicator(out, br.readLine());

								/* 支店名送信 */
								System.out.print("支店名：");
								communicator(out, br.readLine());

								/* 口座名義送信 */
								System.out.print("口座名義：");
								communicator(out, br.readLine());

								/* 暗証番号送信 */
								System.out.print("暗証番号：");
								communicator(out, br.readLine());

								/* 口座番号を受信 */
								rcvmsg = communicator(in);
								System.out.println("口座番号は " + rcvmsg + " です。");

								/* サーバーの応答を受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("OK")) {

									System.out.println("口座開設が完了しました。");

									System.out.println("暗号化アルゴリズムを入力してください。");
									System.out.print(">");
									communicator(out, br.readLine());

									System.out.println("鍵サイズを入力してください。");
									System.out.print(">");
									communicator(out, br.readLine());

									System.out.println("暗号化モードを入力してください。");
									System.out.print(">");
									communicator(out, br.readLine());

								} else {

									System.out.println("口座開設に失敗しました。");
								}

								break loop2;

							case 1:
								/* 口座番号送信 */
								System.out.println("口座番号を入力してください。");
								System.out.print(">");
								communicator(out, br.readLine());

								/* 本人認証方式選択 */
								System.out.println("認証方式を選択して下さい ) 1 : 平パス認証、2 : CHAP認証");
								System.out.print(">");

								/* 認証方式送信 */
								sndmsg = br.readLine();
								communicator(out, sndmsg);

								System.out.println("パスワードを入力してください。");

								if (sndmsg.equals("1")) {

									/* プロンプト表示 */
									System.out.print(">");

									/* 平パス送信 */
									communicator(out, br.readLine());

								} else if (sndmsg.equals("2")) {

									/* チャレンジ(乱数)受信 */
									r = communicator(in);
									System.out.println("[ DEBUG ] チャレンジ < " + r + " >");

									/* プロンプト表示 */
									System.out.print(">");

									/* ハッシュ値送信 */
									sndmsg = "" + br.readLine() + r;
									buf = md.digest(sndmsg.getBytes());
									out.write(buf, 0, buf.length);

									/* --------------- デバッグ用 --------------- */
									StringBuilder sb = new StringBuilder();
									for (byte d : buf) {
										sb.append(String.format("%02X", d));
									}
									String str = sb.toString();

									System.out.println("[ DEBUG ] 送信ハッシュ : " + str);
									/* ------------------------------------------ */

								}

								/* 認証結果受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("OK")) {

									System.out.print("認証が完了しました。\n\n");

									System.out.print("---------- 口座情報 ----------\n\n");
									/* 銀行名を受信 */
									rcvmsg = communicator(in);
									String[] line = rcvmsg.split(",");

									/* 銀行名を表示 */
									System.out.println("銀行名 : " + line[0]);

									/* 支店名を表示 */
									System.out.println("支店名 : " + line[1]);

									/* 口座番号を表示 */
									System.out.println("口座番号 : " + line[2]);

									/* 口座名義を表示 */
									System.out.println("口座名義 : " + line[3]);

									System.out.print("\n------------------------------\n\n");

									break loop2;

								} else {

									System.out.print("認証に失敗しました。\n\n");
									break;

								}

						} 

					} else {

						System.out.print("\n");
						System.out.println("またのご利用お待ちしております。");
						System.out.print("\n");
						break loop1;

					}

				}

				while(true) {

					System.out.println("操作番号をお選びください ) 1 : 預け入れ、2 : お引き出し、3 : 残高照会、4 : 口座振替、その他 : 終了");
					System.out.print(">");

					/* 読み込んだ番号を送信 */
					sndmsg = br.readLine();
					communicator(out, sndmsg);

					/* サーバーからの応答を受信 */
					rcvmsg = communicator(in);

					if (rcvmsg.equals("OK")) {

						switch (Integer.parseInt(sndmsg)) {

							case 1:
								/* 預け入れ金額を送信 */
								System.out.println("預け入れをする金額を入力して下さい。");
								System.out.print(">");
								communicator(out, br.readLine());

								/* 応答を受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("OK")) {

									System.out.print("預け入れが完了しました。\n\n");

								}

								break;

							case 2:
								/* 引き出し金額を送信 */
								System.out.println("引き出す金額を入力してください。");
								System.out.print(">");
								communicator(out, br.readLine());

								/* 応答を受信 */
								rcvmsg = communicator(in);
								System.out.print("お引き出し金額 : " + rcvmsg + "円\n\n");

								break;

							case 3:
								/* 応答を受信 */
								rcvmsg = communicator(in);
								System.out.print("残高 : " + rcvmsg + " 円\n\n");

								break;

							case 4:
								/* 振替先口座番号を送信 */
								System.out.println("振替先口座番号を入力してください。");
								System.out.print(">");
								communicator(out, br.readLine());

								/* 応答を受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("同一口座に振り替えることはできません。");
									break;

								}

								/* 応答を受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("振替先口座が存在しません。");
									break;

								}

								/* 応答を受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("他行へは振替できません。");
									break;

								}

								/* 応答を受信 */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("他の支店へは振替できません。");
									break;

								}
									
								/* 振替金額を送信 */
								System.out.println("振替金額を入力してください。");
								System.out.print(">");
								communicator(out, br.readLine());

								System.out.print("\n振替処理を完了しました。残高をご確認ください。\n\n");

								break;

						}

					} else {

						System.out.print("\n");
						System.out.println("[ DEBUG ] 受信文字列 < " + rcvmsg + " >");
						System.out.println("またのご利用お待ちしております。");
						System.out.print("\n");
						break loop1;

					}


				}

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

}


