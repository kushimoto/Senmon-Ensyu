import java.io.*;
import java.net.*;
import java.util.Random;
import java.security.*;
import javax.crypto.*;

public class ATM_Server {

	public static void main(String args[]) {

		byte[] buf = null;

		/* �҂��󂯃|�[�g�ԍ� */
		int PORT = 49125;

		/* ����M������ۊǗp */
		String sndmsg = null;
		String rcvmsg = null;

		/* ����ԍ�����p */
		int controlNum = 0;

		/* PassAccount�C���X�^���X�p */
		PassAccount pa = null;

		/* CryptoAccount�C���X�^���X�p */
		CryptoAccount ca = null;

		/* �������p�ϐ� */
		String bankName = null;
		String branchName = null;
		String name = null;
		String passwd = null;
		String accnum = null;

		/* �����ԍ������p */
		Random r = new Random();

		/* �t�@�C���C���X�^���X�p */
		File f = null;

		String algorithm = null;
		String keySize = null;
		String mode = null;

		try {
			/* �\�P�b�g�����ƒʐM�m�� */
			ServerSocket svsock = new ServerSocket(PORT);
			Socket socket = svsock.accept();

			/* ����M�X�g���[���p�ϐ��̏��� */
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			/* �t�@�C�����o�̓X�g���[���p�ϐ��̏��� */
			FileInputStream reader = null;
			FileOutputStream writer = null;

			/* �I�u�W�F�N�g�t�@�C���ǂݏ����p�ϐ��̏��� */
			ObjectInputStream objectIn = null;
			ObjectOutputStream objectOut = null;

			loop1:while(true) {
				/* ����ȃ��N�G�X�g�����M�����܂Ő�΂ɒ��߂Ȃ� */
				loop2:while(true) {

					/* ���������M */
					rcvmsg = communicator(in);
					controlNum = Integer.parseInt(rcvmsg);

					switch (controlNum) {

						case 0:
							/* �����J�݊J�n���b�Z�[�W */
							System.out.println("[ DEBUG ] �����J�݊J�n");

							/* ���� */
							communicator(out, "OK");

							/* ��s����M */
							bankName = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] ��s����M < " + bankName + " >");
							/* �x�X����M */
							branchName = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �x�X����M < " + branchName + " >");

							/* �������`��M */
							name = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �������`��M < " + name + " >");

							/* �����p�X���[�h��M */
							passwd = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �p�X���[�h��M < " + passwd + " >");

							/* �����J��(�C���X�^���X����) */
							pa = new PassAccount(bankName, branchName, name, passwd);

							/* �����ԍ����M */
							communicator(out, pa.accountNo);

							/* �����J�ݐ����ʒm���M */
							communicator(out, "OK");

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �����J�݊���");


							/* �A���S���Y������M */
							algorithm = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �A���S���Y������M");

							/* ���T�C�Y����M */
							keySize = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] ���T�C�Y����M");

							/* �Í������[�h����M */
							mode = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �Í������[�h����M");

							/* �Í������� */
							ca = new CryptoAccount(pa.accountNo, algorithm, Integer.parseInt(keySize));

							/* �V���A���C�Y */
							buf = serializer(pa);

							/* �Í��� */
							buf = ca.encrypto(pa.accountNo, buf, buf.length, mode);

							/* �t�@�C�������o�� */
							save(buf, pa.accountNo);

							break loop2;

						case 1:
							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] ���O�C���J�n");

							/* ���� */
							communicator(out, "OK");

							/* �����ԍ���M */
							accnum = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �����ԍ���M < " + accnum + " >");

							/* ���������� */
							ca = new CryptoAccount(accnum);

							/* �t�@�C���ǂݍ��� */
							buf = readCryptedFile(accnum);

							/* ������ */
							buf = ca.decrypto(accnum, buf, buf.length, "AUTO");

							/* �f�V���A���C�Y */
							pa = deserializer(buf);

							/* �F�ؕ�������M */
							rcvmsg = communicator(in);

							if (rcvmsg.equals("1")) {

								/* �p�X���[�h��M */
								rcvmsg = communicator(in);

								/* �p�X���[�h�F�� */
								sndmsg = pa.pwAuth(rcvmsg);

							} else if (rcvmsg.equals("2")) {

								/* �`�������W(����)���� */
								sndmsg = String.valueOf(r.nextInt(1000));
								System.out.println("[ DEBUG ] �`�������W < " + sndmsg + " >");

								/* �`�������W(����)���M */
								communicator(out, sndmsg);

								/* �n�b�V���l��M */
								byte[] cliDigest = new byte[32];
								in.read(cliDigest);

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] �n�b�V���l��M");

								/* CHAP�F�� */
								sndmsg = pa.chap(cliDigest, sndmsg);

							}

							/* �F�،��ʑ��M */
							communicator(out, sndmsg);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �F�،��� : " + sndmsg);

							if (sndmsg.equals("OK")) {

								/* ���p�ҏ����擾���A�����𑗐M */
								sndmsg = pa.getAccountInfo();
								communicator(out, sndmsg);

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] ���p�ҏ�񑗐M");

								break loop2;

							} else {

								break;

							}

						default:

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �ؒf�v����M");

							/* �G���[���� */
							communicator(out, "NG");

							break loop1;

					}

				}

				/* ����I���܂ő���Ɋւ��郊�N�G�X�g���󂯕t���� */
				while(true) {

					/* ����ԍ���M */
					rcvmsg = communicator(in);
					controlNum = Integer.parseInt(rcvmsg);

					/* �f�o�b�O�o�� */
					System.out.println("[ DEBUG ] ����ԍ���M < " + rcvmsg + " >");

					switch (controlNum) {

						case 1:

							/* ���� */
							communicator(out, "OK");

							/* �a���z����M */
							rcvmsg = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �a���z��M < " + rcvmsg + " >");

							/* �a����������s���A�����𑗐M */
							sndmsg = pa.execDeposit(Integer.parseInt(rcvmsg));
							communicator(out, sndmsg);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �a������ < " + sndmsg + " >");
							break;

						case 2:
							/* ���� */
							communicator(out, "OK");

							/* �����o�����z����M */
							rcvmsg = communicator(in);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �����o���z��M < " + rcvmsg + " >");

							/* �����o����������s���A�����𑗐M */
							sndmsg = pa.execDraw(Integer.parseInt(rcvmsg));
							communicator(out, sndmsg);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �����o���z���M < " + sndmsg + " >");
							break;

						case 3:
							/* ���� */
							communicator(out, "OK");

							/* �c���Ɖ�����s���A�����𑗐M */
							sndmsg = pa.execGetBalance();
							communicator(out, sndmsg);

							/* �f�o�b�O�o�� */
							System.out.println("[ DEBUG ] �c�����M < " + sndmsg + " >");

							break;

						case 4:
							/* ���� */
							communicator(out, "OK");

							/* �����ԍ���M */
							accnum = communicator(in);

							/* ��������łȂ����Ƃ��m�F */
							if (pa.accountNo.equals(accnum)) {

								/* �������M */
								communicator(out, "NG");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] ��������ւ̐U��ւ������m");

							} else {

								/* �������M */
								communicator(out, "OK");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] �U�֐悪�قȂ邱�Ƃ��m�F");

							}

							/* �U�֐�����p�ϐ��錾 */
							PassAccount tAccount = null;

							f = new File(accnum);

							/* �����̑��݊m�F */
							if (f.exists()) {

								/* ���������� */
								ca = new CryptoAccount(accnum);

								/* �t�@�C���ǂݍ��� */
								buf = readCryptedFile(accnum);

								/* ������ */
								buf = ca.decrypto(accnum, buf, buf.length, "AUTO");

								/* �f�V���A���C�Y */
								tAccount = deserializer(buf);

								/* �������M */
								communicator(out, "OK");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] �U�֐�����̃f�V���A���C�Y����");

							} else {

								/* �������M */
								communicator(out, "NG");
								
								/* �f�o�b�O�o�� */
								System.out.print("[ DEBUG ] �U�֐���������݂��܂���I\n\n");
								break;

							}

							/* ��s�̊m�F */
							if (pa.bankName.equals(tAccount.bankName)) {

								/* �������M */
								communicator(out, "OK");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] ��s����v");

							} else {

								/* �������M */
								communicator(out, "NG");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] ��s���s��v");

								break;

							}

							/* �x�X�̊m�F */
							if (pa.branchName.equals(tAccount.branchName)) {

								/* �������M */
								communicator(out, "OK");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] �x�X����v");

							} else {

								/* �������M */
								communicator(out, "NG");

								/* �f�o�b�O�o�� */
								System.out.println("[ DEBUG ] �x�X���s��v");

								break;

							}

							/* �U�֋��z��M */
							rcvmsg = communicator(in);

							/* �U�֌��̎c�������炷 */
							int amount;
							amount = pa.draw(Integer.parseInt(rcvmsg));

							/* �U�֐�̎c���𑝂₷ */
							tAccount.deposit(amount);

							/* �V���A���C�Y */
							buf = serializer(tAccount);

							/* �Í��� */
							buf = ca.encrypto(tAccount.accountNo, buf, buf.length, "AUTO");

							/* �t�@�C�������o�� */
							save(buf, tAccount.accountNo);

							/* �Í������� */
							ca = new CryptoAccount(pa.accountNo);

							/* �V���A���C�Y */
							buf = serializer(pa);

							/* �Í��� */
							buf = ca.encrypto(pa.accountNo, buf, buf.length, "AUTO");

							/* �t�@�C�������o�� */
							save(buf, pa.accountNo);

						

							break;

						default:
							/* �ؒf�v����M�\�� */
							System.out.println("[ DEBUG ] �ؒf�v����M");

							/* �G���[���� */
							communicator(out, "NG");

							if (pa != null) {

								/* �V���A���C�Y */
								buf = serializer(pa);

								/* �Í��� */
								buf = ca.encrypto(pa.accountNo, buf, buf.length, "AUTO");

								/* �t�@�C�������o�� */
								save(buf, pa.accountNo);
							}

							break loop1;


					}


				}

			}
			
			/* �X�g���[������� */

			if (out != null) {

				out.close();
				System.out.println("[ DEBUG ] out �N���[�Y");
				
			}

			if (in != null) {

				in.close();
				System.out.println("[ DEBUG ] in �N���[�Y");

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
			/* �V���A���C�Y */
			ByteArrayOutputStream byteos = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteos);
			objectOut.writeObject(pa);
			retObject = byteos.toByteArray();

			/* �f�o�b�O�o�� */
			System.out.println("[ DEBUG ] �V���A���C�Y����");

			/* �X�g���[������� */
			byteos.close();
			System.out.println("[ DEBUG ] byteos �N���[�Y");
			objectOut.close();
			System.out.println("[ DEBUG ] objectOut �N���[�Y");

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
			System.out.println("[ DEBUG ] writer �N���[�Y");

			/* �f�o�b�O�o�� */
			System.out.println("[ DEBUG ] �t�@�C���ւ̏o�͊���");

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
			/* �f�V���A���C�Y */
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			ObjectInputStream objectIn = new ObjectInputStream(bais);
			pa = (PassAccount)objectIn.readObject();

			/* �f�o�b�O�o�� */
			System.out.println("[ DEBUG ] �f�V���A���C�Y����");

			/* �X�g���[������� */
			bais.close();
			System.out.println("[ DEBUG ] bais �N���[�Y");
			objectIn.close();
			System.out.println("[ DEBUG ] objectIn �N���[�Y");

		} catch (IOException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return pa;

	}

}
