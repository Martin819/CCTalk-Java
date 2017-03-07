package CCTalk.Java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.fazecast.jSerialComm.SerialPort;

/**
 *
 * @author Cosimo Damiano Persia
 *	CCTalkConnection is useful to handle the connection between host and another CCTalk Device.
 */
public class CCTalkConnection {

	private final int RETRY_TIMES = 3;
	private final String path;
	private final SerialPort port;
	private InputStream input;
	private OutputStream output;
	private byte[] buffer;

	/**
	 *
	 * @param path to CCTalk device
	 */
	public CCTalkConnection(String path) {
		this.path = path;
		this.port = SerialPort.getCommPort(path);
		this.port.openPort();
		this.input = this.port.getInputStream();
		this.output = this.port.getOutputStream();
		this.buffer = new byte[64];
	}

	/**
	 * closes the connection
	 * @throws IOException
	 */
	public void close() throws IOException {
		input.close();
		output.close();
		port.closePort();
	}

	/**
	 *
	 * @param destination
	 * @param source
	 * @param header
	 * @param data
	 * @return responses to message
	 * @throws Exception
	 */
	public ArrayList<CCTalkMessage> sendMessage(int destination, int source, int header, byte[] data) throws Exception {
		if (destination > 255 && source > 255 && header > 255)
			throw new Exception("not a byte value. It should be >=0 and <=255");
		ArrayList<CCTalkMessage> response = new ArrayList<>();
		boolean flag = true;
		int i = 0;
		while (flag && i < RETRY_TIMES) {
			CCTalkMessage message = new CCTalkMessage((byte) destination, (byte) source, (byte) header, data);
			output.write(message.getCommand());
			Thread.sleep(70);
			input.read(buffer);
			response = parseMessages(buffer);
			if (response.size() >= 2) {
				flag = false;
			} else {
				i++;
				if (i == 3) {
					throw new Exception("Not able to send command : " + message);
				}
			}
		}
		return response;
	}

	/**
	 *
	 * @param destination
	 * @param source
	 * @param header
	 * @param data
	 * @return plain response as array of bytes
	 * @throws Exception
	 */
	public byte[] sendMessagePlain(int destination, int source, int header, byte[] data) throws Exception {
		if (destination > 255 && source > 255 && header > 255)
			throw new Exception("not a byte value. It should be >=0 and <=255");
		CCTalkMessage message = new CCTalkMessage((byte) destination, (byte) source, (byte) header, data);
		output.write(message.getCommand());
		Thread.sleep(70);
		input.read(buffer);
		return buffer;
	}

	/**
	 *
	 * @param msg
	 * @return CCTalk Messages Responses
	 * @throws Exception
	 */
	private ArrayList<CCTalkMessage> parseMessages(byte[] msg) throws Exception {
		ArrayList<CCTalkMessage> res = new ArrayList<CCTalkMessage>();
		byte destination;
		for (int i = 0; i < msg.length; i++) {
			destination = msg[i];
			if (destination == 0) {
				break;
			} else {
				int messageLength = msg[i + 1] + 5;
				res.add(parseMessage(Arrays.copyOfRange(msg, i, i + messageLength)));
				i += messageLength;
			}
		}
		for (int j = 0; j < msg.length; j++) {
			msg[j] = 0;
		}
		return res;
	}

	private CCTalkMessage parseMessage(byte[] message) throws Exception {
		if (message == null || message.length < 2) {
			throw new Exception("Message was less than two bytes long, therefore did not contain a data length byte");
		}
		// Convert unsigned byte to signed
		int dataLength = (int) (message[1] & 0xFF);
		// data + source, destination, checksum, numBytes, header
		int expectedMessageLength = dataLength + 5;

		if (message.length < expectedMessageLength) {
			throw new Exception("Message was shorter than expected length of " + expectedMessageLength + " bytes");
		}

		int sum = 0;
		for (int i = 4; i < expectedMessageLength; i++) {
			sum += (message[i] & 0xff);
		}
		if (sum % 256 != 0) {
			throw new Exception("Invalid checksum result");
		}

		byte[] data = null;
		if (dataLength != 0) {
			data = Arrays.copyOfRange(message, 4, expectedMessageLength - 1);
		}
		CCTalkMessage ccMessage = new CCTalkMessage(message[0], message[2], message[3], data);
		return ccMessage;
	}

	public SerialPort getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}
}
