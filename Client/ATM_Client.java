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
					System.out.println("*       ����{��s�g��       *");
					System.out.println("*                            *");
					System.out.println("******************************");

					System.out.print("\n");
					System.out.println("�e�s��ATM�͑���{��s�g���̒����ƂȂ�A���ꂳ��܂����B");
					System.out.print("\n");
					System.out.println("����ԍ������I�т������� ) 0 : �����J�݁A1 : ���O�C��");
					System.out.print(">");

					/* �ǂݍ��񂾔ԍ��𑗐M */
					sndmsg = br.readLine();
					communicator(out, sndmsg);

					/* �T�[�o�[����̉�������M */
					rcvmsg = communicator(in);


					if (rcvmsg.equals("OK")) {

						switch (Integer.parseInt(sndmsg)) {

							case 0:
								System.out.println("�V�K�����J�݂��J�n���܂��B");
								System.out.println("�K�v�������L�����Ă��������B");
								System.out.print("\n");

								/* ��s�����M */
								System.out.print("��s���F");
								communicator(out, br.readLine());

								/* �x�X�����M */
								System.out.print("�x�X���F");
								communicator(out, br.readLine());

								/* �������`���M */
								System.out.print("�������`�F");
								communicator(out, br.readLine());

								/* �Ïؔԍ����M */
								System.out.print("�Ïؔԍ��F");
								communicator(out, br.readLine());

								/* �����ԍ�����M */
								rcvmsg = communicator(in);
								System.out.println("�����ԍ��� " + rcvmsg + " �ł��B");

								/* �T�[�o�[�̉�������M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("OK")) {

									System.out.println("�����J�݂��������܂����B");

									System.out.println("�Í����A���S���Y������͂��Ă��������B");
									System.out.print(">");
									communicator(out, br.readLine());

									System.out.println("���T�C�Y����͂��Ă��������B");
									System.out.print(">");
									communicator(out, br.readLine());

									System.out.println("�Í������[�h����͂��Ă��������B");
									System.out.print(">");
									communicator(out, br.readLine());

								} else {

									System.out.println("�����J�݂Ɏ��s���܂����B");
								}

								break loop2;

							case 1:
								/* �����ԍ����M */
								System.out.println("�����ԍ�����͂��Ă��������B");
								System.out.print(">");
								communicator(out, br.readLine());

								/* �{�l�F�ؕ����I�� */
								System.out.println("�F�ؕ�����I�����ĉ����� ) 1 : ���p�X�F�؁A2 : CHAP�F��");
								System.out.print(">");

								/* �F�ؕ������M */
								sndmsg = br.readLine();
								communicator(out, sndmsg);

								System.out.println("�p�X���[�h����͂��Ă��������B");

								if (sndmsg.equals("1")) {

									/* �v�����v�g�\�� */
									System.out.print(">");

									/* ���p�X���M */
									communicator(out, br.readLine());

								} else if (sndmsg.equals("2")) {

									/* �`�������W(����)��M */
									r = communicator(in);
									System.out.println("[ DEBUG ] �`�������W < " + r + " >");

									/* �v�����v�g�\�� */
									System.out.print(">");

									/* �n�b�V���l���M */
									sndmsg = "" + br.readLine() + r;
									buf = md.digest(sndmsg.getBytes());
									out.write(buf, 0, buf.length);

									/* --------------- �f�o�b�O�p --------------- */
									StringBuilder sb = new StringBuilder();
									for (byte d : buf) {
										sb.append(String.format("%02X", d));
									}
									String str = sb.toString();

									System.out.println("[ DEBUG ] ���M�n�b�V�� : " + str);
									/* ------------------------------------------ */

								}

								/* �F�،��ʎ�M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("OK")) {

									System.out.print("�F�؂��������܂����B\n\n");

									System.out.print("---------- ������� ----------\n\n");
									/* ��s������M */
									rcvmsg = communicator(in);
									String[] line = rcvmsg.split(",");

									/* ��s����\�� */
									System.out.println("��s�� : " + line[0]);

									/* �x�X����\�� */
									System.out.println("�x�X�� : " + line[1]);

									/* �����ԍ���\�� */
									System.out.println("�����ԍ� : " + line[2]);

									/* �������`��\�� */
									System.out.println("�������` : " + line[3]);

									System.out.print("\n------------------------------\n\n");

									break loop2;

								} else {

									System.out.print("�F�؂Ɏ��s���܂����B\n\n");
									break;

								}

						} 

					} else {

						System.out.print("\n");
						System.out.println("�܂��̂����p���҂����Ă���܂��B");
						System.out.print("\n");
						break loop1;

					}

				}

				while(true) {

					System.out.println("����ԍ������I�т������� ) 1 : �a������A2 : �������o���A3 : �c���Ɖ�A4 : �����U�ցA���̑� : �I��");
					System.out.print(">");

					/* �ǂݍ��񂾔ԍ��𑗐M */
					sndmsg = br.readLine();
					communicator(out, sndmsg);

					/* �T�[�o�[����̉�������M */
					rcvmsg = communicator(in);

					if (rcvmsg.equals("OK")) {

						switch (Integer.parseInt(sndmsg)) {

							case 1:
								/* �a��������z�𑗐M */
								System.out.println("�a�������������z����͂��ĉ������B");
								System.out.print(">");
								communicator(out, br.readLine());

								/* ��������M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("OK")) {

									System.out.print("�a�����ꂪ�������܂����B\n\n");

								}

								break;

							case 2:
								/* �����o�����z�𑗐M */
								System.out.println("�����o�����z����͂��Ă��������B");
								System.out.print(">");
								communicator(out, br.readLine());

								/* ��������M */
								rcvmsg = communicator(in);
								System.out.print("�������o�����z : " + rcvmsg + "�~\n\n");

								break;

							case 3:
								/* ��������M */
								rcvmsg = communicator(in);
								System.out.print("�c�� : " + rcvmsg + " �~\n\n");

								break;

							case 4:
								/* �U�֐�����ԍ��𑗐M */
								System.out.println("�U�֐�����ԍ�����͂��Ă��������B");
								System.out.print(">");
								communicator(out, br.readLine());

								/* ��������M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("��������ɐU��ւ��邱�Ƃ͂ł��܂���B");
									break;

								}

								/* ��������M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("�U�֐���������݂��܂���B");
									break;

								}

								/* ��������M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("���s�ւ͐U�ւł��܂���B");
									break;

								}

								/* ��������M */
								rcvmsg = communicator(in);

								if (rcvmsg.equals("NG")) {

									System.out.println("���̎x�X�ւ͐U�ւł��܂���B");
									break;

								}
									
								/* �U�֋��z�𑗐M */
								System.out.println("�U�֋��z����͂��Ă��������B");
								System.out.print(">");
								communicator(out, br.readLine());

								System.out.print("\n�U�֏������������܂����B�c�������m�F���������B\n\n");

								break;

						}

					} else {

						System.out.print("\n");
						System.out.println("[ DEBUG ] ��M������ < " + rcvmsg + " >");
						System.out.println("�܂��̂����p���҂����Ă���܂��B");
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


