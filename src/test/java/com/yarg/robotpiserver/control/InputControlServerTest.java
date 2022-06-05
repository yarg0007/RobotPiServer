package com.yarg.robotpiserver.control;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InputControlServerTest {

	DatagramSocket datagramSocket;
	PWMController controller;
	ArgumentCaptor<Integer> intInputCaptor;
	ArgumentCaptor<Integer> secondInputCaptor;
	ArgumentCaptor<Integer> thirdInputCaptor;

	@BeforeMethod
	public void setupMocks() {
		datagramSocket = mock(DatagramSocket.class);
		controller = mock(PWMController.class);

		intInputCaptor = ArgumentCaptor.forClass(Integer.class);
		secondInputCaptor = ArgumentCaptor.forClass(Integer.class);
		thirdInputCaptor = ArgumentCaptor.forClass(Integer.class);
	}

	@Test
	public void startAndStopInputControlServer() throws Exception {

		InputControlServer inputControlServer = new InputControlServer(controller, datagramSocket);
		inputControlServer.startInputControlServer();
		Thread.sleep(1000);
		inputControlServer.stopInputControlServer();

		verify(datagramSocket, atLeastOnce()).receive(Mockito.any(DatagramPacket.class));
	}

	@Test
	public void processInputWithMissingDataDelimeter() throws Exception {

		byte[] data = getInvalidDatagramData(new InputData());
		doAnswer(new DatagramPacketAnswer(data)).when(datagramSocket).receive(Mockito.any(DatagramPacket.class));

		InputControlServer inputControlServer = new InputControlServer(controller, datagramSocket);
		inputControlServer.processInput();

		verify(controller, never()).setDriveInput(Mockito.anyInt());
		verify(controller, never()).setHeadLiftInput(Mockito.anyInt());
		verify(controller, never()).setHeadTurnInput(Mockito.anyInt());
		verify(controller, never()).setTalkInput(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
		verify(controller, never()).setTurnInput(Mockito.anyInt());
	}

	@Test
	public void processInputWithLessThanExpectedNumberOfInputs() throws Exception {

		byte[] data = getDatagramWithSize(5);
		doAnswer(new DatagramPacketAnswer(data)).when(datagramSocket).receive(Mockito.any(DatagramPacket.class));

		InputControlServer inputControlServer = new InputControlServer(controller, datagramSocket);
		inputControlServer.processInput();

		verify(controller, never()).setDriveInput(Mockito.anyInt());
		verify(controller, never()).setHeadLiftInput(Mockito.anyInt());
		verify(controller, never()).setHeadTurnInput(Mockito.anyInt());
		verify(controller, never()).setTalkInput(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
		verify(controller, never()).setTurnInput(Mockito.anyInt());
	}

	@Test
	public void processInputWithMoreThanExpectedNumberOfInputs() throws Exception {

		byte[] data = getDatagramWithSize(7);
		doAnswer(new DatagramPacketAnswer(data)).when(datagramSocket).receive(Mockito.any(DatagramPacket.class));

		InputControlServer inputControlServer = new InputControlServer(controller, datagramSocket);
		inputControlServer.processInput();

		verify(controller, never()).setDriveInput(Mockito.anyInt());
		verify(controller, never()).setHeadLiftInput(Mockito.anyInt());
		verify(controller, never()).setHeadTurnInput(Mockito.anyInt());
		verify(controller, never()).setTalkInput(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
		verify(controller, never()).setTurnInput(Mockito.anyInt());
	}

	@Test
	public void processInputWithExpectedNumberOfInputs() throws Exception {

		int driveInput = 10;
		int turnInput = 11;
		int headTurnInput = 12;
		int headLiftInput = 13;
		boolean talkingInput = true;
		boolean mouthOpenInput = false;

		InputData inputData = new InputData().setDriveInput(driveInput).setTurnInput(turnInput)
				.setHeadTurnInput(headTurnInput).setHeadLiftInput(headLiftInput).setTalkingInput(talkingInput)
				.setMouthOpenInput(mouthOpenInput);

		byte[] data = getDatagramData(inputData);
		doAnswer(new DatagramPacketAnswer(data)).when(datagramSocket).receive(Mockito.any(DatagramPacket.class));

		InputControlServer inputControlServer = new InputControlServer(controller, datagramSocket);
		inputControlServer.processInput();

		verify(controller, times(1)).setDriveInput(intInputCaptor.capture());
		assertEquals(intInputCaptor.getValue(), Integer.valueOf(driveInput));

		verify(controller, times(1)).setHeadLiftInput(intInputCaptor.capture());
		assertEquals(intInputCaptor.getValue(), Integer.valueOf(headLiftInput));

		verify(controller, times(1)).setHeadTurnInput(intInputCaptor.capture());
		assertEquals(intInputCaptor.getValue(), Integer.valueOf(headTurnInput));

		verify(controller, times(1)).setTalkInput(intInputCaptor.capture(), secondInputCaptor.capture(), thirdInputCaptor.capture());
		assertEquals(intInputCaptor.getValue(), Integer.valueOf(talkingInput ? 1 : 0));
		assertEquals(secondInputCaptor.getValue(), Integer.valueOf(0));
		assertEquals(thirdInputCaptor.getValue(), Integer.valueOf(mouthOpenInput ? 1 : 0));

		verify(controller, times(1)).setTurnInput(intInputCaptor.capture());
		assertEquals(intInputCaptor.getValue(), Integer.valueOf(turnInput));
	}

	@DataProvider(name = "audioLevelInputs")
	public Object[][] audioLevelInputs() {
		return new Object[][] {
			{ 0, 0, 0 },
			{ 1, 0, 0 },
			{ 0, 1, 100 },
			{ -2, -1, 0 }
		};
	}

	@Test(dataProvider = "audioLevelInputs")
	public void audioLevelUpdate(Integer audioLevelBaseline, Integer currentAudioLevel, Integer expectedAudioLevel) throws Exception {

		InputData inputData = new InputData();

		byte[] data = getDatagramData(inputData);
		doAnswer(new DatagramPacketAnswer(data)).when(datagramSocket).receive(Mockito.any(DatagramPacket.class));

		InputControlServer inputControlServer = new InputControlServer(controller, datagramSocket);
		inputControlServer.audioLevelUpdate(audioLevelBaseline, currentAudioLevel);
		inputControlServer.processInput();

		verify(controller, times(1)).setTalkInput(intInputCaptor.capture(), secondInputCaptor.capture(), thirdInputCaptor.capture());
		assertEquals(secondInputCaptor.getValue(), expectedAudioLevel);
	}

	private byte[] getDatagramData(InputData inputData) {

		String dataMsg = String.format("%d,%d,%d,%d,%d,%d:?", inputData.getDriveInput(), inputData.getTurnInput(),
				inputData.getHeadLiftInput(), inputData.getHeadTurnInput(), inputData.getTalkingInput(),
				inputData.getMouthOpenInput());

		return dataMsg.getBytes();
	}

	private byte[] getInvalidDatagramData(InputData inputData) {

		String dataMsg = String.format("%d,%d,%d,%d,%d,%d", inputData.getDriveInput(), inputData.getTurnInput(),
				inputData.getHeadLiftInput(), inputData.getHeadTurnInput(), inputData.getTalkingInput(),
				inputData.getMouthOpenInput());

		return dataMsg.getBytes();
	}

	private byte[] getDatagramWithSize(int size) {

		StringBuilder dataBuilder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				dataBuilder.append(",");
			}
			dataBuilder.append("1");
		}
		dataBuilder.append(":?");
		return dataBuilder.toString().getBytes();
	}
}

class DatagramPacketAnswer implements Answer<DatagramPacket> {

	private byte[] datagramData;

	public DatagramPacketAnswer(byte[] datagramData) {
		this.datagramData = datagramData;
	}

	@Override
	public DatagramPacket answer(InvocationOnMock invocation) throws Throwable {
		Object[] arguments = invocation.getArguments();
		DatagramPacket dp = (DatagramPacket) arguments[0];
		dp.setData(datagramData);
		return null;
	}
}

class InputData {

	private int driveInput = 0;
	private int turnInput = 0;
	private int headLiftInput = 0;
	private int headTurnInput = 0;
	private int talkingInput = 0;
	private int mouthOpenInput = 0;

	/**
	 * Set drive input.
	 *
	 * @param driveInput
	 *            (-100 (reverse) to 100 (forward) - variable input range)
	 * @return Builder instance.
	 */
	public InputData setDriveInput(int driveInput) {
		this.driveInput = driveInput;
		return this;
	}

	/**
	 * Set turn input.
	 *
	 * @param turnInput
	 *            (-100 (left) to 100 (right) - variable input range)
	 * @return Builder instance.
	 */
	public InputData setTurnInput(int turnInput) {
		this.turnInput = turnInput;
		return this;
	}

	/**
	 * Head lift input.
	 *
	 * @param headLiftInput
	 *            (-100 (down) to 100 (up) - variable input range)
	 * @return Builder instance.
	 */
	public InputData setHeadLiftInput(int headLiftInput) {
		this.headLiftInput = headLiftInput;
		return this;
	}

	/**
	 * Set head turn input.
	 *
	 * @param headTurnInput
	 *            (-100 (left) to 100 (right) - variable input range)
	 * @return Builder instance.
	 */
	public InputData setHeadTurnInput(int headTurnInput) {
		this.headTurnInput = headTurnInput;
		return this;
	}

	/**
	 * Set the talking input.
	 *
	 * @param talking
	 *            True to talk, false otherwise.
	 * @return Builder instance.
	 */
	public InputData setTalkingInput(boolean talking) {
		talkingInput = talking ? 1 : 0;
		return this;
	}

	/**
	 * Set the mouth open input.
	 *
	 * @param mouthOpen
	 *            True to open mouth, false otherwise.
	 * @return Builder instance.
	 */
	public InputData setMouthOpenInput(boolean mouthOpen) {
		mouthOpenInput = mouthOpen ? 1 : 0;
		return this;
	}

	public int getDriveInput() {
		return driveInput;
	}

	public int getTurnInput() {
		return turnInput;
	}

	public int getHeadLiftInput() {
		return headLiftInput;
	}

	public int getHeadTurnInput() {
		return headTurnInput;
	}

	public int getTalkingInput() {
		return talkingInput;
	}

	public int getMouthOpenInput() {
		return mouthOpenInput;
	}
}