package com.yarg.robotpiserver.control;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class PWMControllerTest {

	I2CBus bus;
	I2CDevice servoDriver;
	PWMController controller;
	ArgumentCaptor<Integer> writeAddressCaptor;
	ArgumentCaptor<Byte> writeDataCaptor;

	@BeforeMethod
	public void setupMocks() {
		bus = mock(I2CBus.class);
		servoDriver = mock(I2CDevice.class);
		writeAddressCaptor = ArgumentCaptor.forClass(Integer.class);
		writeDataCaptor = ArgumentCaptor.forClass(Byte.class);
	}

	@AfterMethod
	public void teardown() {
		if (controller != null) {
			controller.quickStop();
		}
	}

	@Test
	public void stopPWMController() throws Exception {

		doNothing().when(servoDriver).write(Mockito.anyInt(), Mockito.anyByte());

		controller = new PWMController(null, servoDriver);
		verify(servoDriver, times(0)).write(Mockito.anyInt(), Mockito.anyByte());

		controller.stopPWMController();
		int numberOfCalls = 5 * 4; // Five calls and each call triggers 4 write
		// operations.
		verify(servoDriver, times(numberOfCalls)).write(Mockito.anyInt(), Mockito.anyByte());
	}

	@Test
	public void setPWM() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setPWM(1, 1, 1);
		int numberOfCalls = 1 * 4; // One call and each call triggers 4 write
		// operations.
		verify(servoDriver, times(numberOfCalls)).write(Mockito.anyInt(), Mockito.anyByte());
	}

	@DataProvider(name = "DriveInputTestValues")
	public Object[][] getDriveInputTestValues() {
		return new Object[][] {
			{ 0, 8, new Integer[] { 62, 63, 64, 65, 66, 67, 68, 69 }, new Byte[] { 0, 0, -1, 15, 0, 0, 0, 0 } },
			{ 101, 8, new Integer[] { 62, 63, 64, 65, 66, 67, 68, 69 }, new Byte[] { 0, 0, -1, 15, 0, 0, 40, 0 } },
			{ -101, 8, new Integer[] { 62, 63, 64, 65, 66, 67, 68, 69 }, new Byte[] { 0, 0, 0, 0, 0, 0, 40, 0 } },
			{ 1, 8, new Integer[] { 62, 63, 64, 65, 66, 67, 68, 69 }, new Byte[] { 0, 0, -1, 15, 0, 0, 40, 0 } },
			{ -1, 8, new Integer[] { 62, 63, 64, 65, 66, 67, 68, 69 }, new Byte[] { 0, 0, 0, 0, 0, 0, 40, 0 } } };
	}

	@Test(dataProvider = "DriveInputTestValues")
	public void setDriveInput(int driveInputValue, int expectedNumberOfSetPWMCalls, Integer[] expectedWriteAddresses,
			Byte[] expectedDataValues) throws Exception {

		if (expectedWriteAddresses.length != expectedDataValues.length) {
			throw new IllegalArgumentException(
					"Expected write address and expected data arrays MUST be the same length.");
		}

		controller = new PWMController(null, servoDriver);
		controller.setDriveInput(driveInputValue);

		verify(servoDriver, times(expectedNumberOfSetPWMCalls)).write(writeAddressCaptor.capture(),
				writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		Integer actualAddress;
		Integer expectedAddress;
		Byte actualData;
		Byte expectedData;

		for (int i = 0; i < expectedWriteAddresses.length; i++) {
			actualAddress = actualWriteAddressValues.get(i);
			expectedAddress = expectedWriteAddresses[i];

			actualData = actualDataValues.get(i);
			expectedData = expectedDataValues[i];

			assertEquals(actualAddress, expectedAddress,
					String.format("WriteAddress [%d] does not match expected.", i));
			assertEquals(actualData, expectedData, String.format("Data [%d] does not match expected.", i));
		}
	}

	@Test
	public void consecutiveDriveInputValues() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setDriveInput(10);
		controller.setDriveInput(10);
		controller.setDriveInput(10);
		controller.setDriveInput(10);
		controller.setDriveInput(10);

		verify(servoDriver, times(40)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedWriteAddressValues = Arrays.asList(62, 63, 64, 65, 66, 67, 68, 69, 62, 63, 64, 65, 66, 67,
				68, 69, 62, 63, 64, 65, 66, 67, 68, 69, 62, 63, 64, 65, 66, 67, 68, 69, 62, 63, 64, 65, 66, 67, 68, 69);
		List<Byte> expectedDataValues = Arrays.asList(new Byte[] { 0, 0, -1, 15, 0, 0, 40, 0, 0, 0, -1, 15, 0, 0, 81, 0,
				0, 0, -1, 15, 0, 0, 122, 0, 0, 0, -1, 15, 0, 0, -93, 0, 0, 0, -1, 15, 0, 0, -52, 0 });

		assertEquals(actualWriteAddressValues, expectedWriteAddressValues, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedDataValues, "Data values do not match expected.");
	}

	@DataProvider(name = "ServoPwmValues")
	public Object[][] servoPwmValues() {
		return new Object[][] { { 0, -100.0f, 100.0f, 0 }, { 1, -100.0f, 100.0f, 1 }, { -1, -100.0f, 100.0f, -1 },
			{ 0, 0.0f, 0.0f, 0 }, { 100, -100.0f, 100.0f, 100 }, { 101, -100.0f, 100.0f, 100 },
			{ -100, -100.0f, 100.0f, -100 }, { -101, -100.0f, 100.0f, -100 }, };
	}

	@Test(dataProvider = "ServoPwmValues")
	public void calculateServoPwmValue(int inputValue, float minPwmValue, float maxPwmValue, int expectedValue)
			throws Exception {

		controller = new PWMController(null, null);
		int actualValue = controller.calculateServoPWMValue(inputValue, minPwmValue, maxPwmValue);
		assertEquals(actualValue, expectedValue);
	}

	@Test
	public void setTurnInput() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setTurnInput(0);

		verify(servoDriver, times(4)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedWriteAddressValues = Arrays.asList(58, 59, 60, 61);
		List<Byte> expectedDataValues = Arrays.asList(new Byte[] { 0, 0, 69, 1 });

		assertEquals(actualWriteAddressValues, expectedWriteAddressValues, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedDataValues, "Data values do not match expected.");
	}

	@Test
	public void setHeadLiftInput() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setHeadLiftInput(0);

		verify(servoDriver, times(4)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedWriteAddressValues = Arrays.asList(54, 55, 56, 57);
		List<Byte> expectedDataValues = Arrays.asList(new Byte[] { 0, 0, 19, 1 });

		assertEquals(actualWriteAddressValues, expectedWriteAddressValues, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedDataValues, "Data values do not match expected.");
	}

	@Test
	public void setHeadTurnInput() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setHeadTurnInput(0);

		verify(servoDriver, times(4)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedWriteAddressValues = Arrays.asList(50, 51, 52, 53);
		List<Byte> expectedDataValues = Arrays.asList(new Byte[] { 0, 0, 69, 1 });

		assertEquals(actualWriteAddressValues, expectedWriteAddressValues, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedDataValues, "Data values do not match expected.");
	}

	@DataProvider(name = "talkInputValues")
	public Object[][] talkInputValues() {
		return new Object[][] {
			{ 0, 0, 0, new Byte[] { 0, 0, 84, 1 } },
			{ 2, 2, 2, new Byte[] { 0, 0, 84, 1 } },
			{ -1, -1, -1, new Byte[] { 0, 0, 84, 1 } },
			{ 0, 0, 1, new Byte[] { 0, 0, -6, 0 } },
			{ 1, 1, 1, new Byte[] { 0, 0, -6, 0 } },
			{ 1, 0, 0, new Byte[] { 0, 0, 84, 1 } },
			{ 1, 1, 0, new Byte[] { 0, 0, 83, 1 } },
			{ 1, 50, 0, new Byte[] { 0, 0, 39, 1 } },
			{ 1, 100, 0, new Byte[] { 0, 0, -6, 0 } }
		};
	}

	@Test(dataProvider = "talkInputValues")
	public void setTalkInput(int isTalking, int talkingLevel, int openMouth, Byte[] expectedDataValues)
			throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setTalkInput(isTalking, talkingLevel, openMouth);

		verify(servoDriver, times(4)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedAddresses = Arrays.asList(new Integer[] { 46, 47, 48, 49 });
		List<Byte> expectedData = Arrays.asList(expectedDataValues);

		assertEquals(actualWriteAddressValues, expectedAddresses, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedData, "Data values do not match expected.");
	}

	@Test
	public void setPWMFreq() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.setPWMFreq(PWMController.HERTZ);

		verify(servoDriver, times(4)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedAddresses = Arrays.asList(new Integer[] {0, 254, 0, 0});
		List<Byte> expectedData = Arrays.asList(new Byte[] {16, 121, 0, -128});

		assertEquals(actualWriteAddressValues, expectedAddresses, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedData, "Data values do not match expected.");
	}

	@Test
	public void init() throws Exception {

		Mockito.when(bus.getDevice(Mockito.anyInt())).thenReturn(servoDriver);

		controller = new PWMController(bus, null);
		controller.init();

		verify(servoDriver, times(5)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());

		List<Integer> actualWriteAddressValues = writeAddressCaptor.getAllValues();
		List<Byte> actualDataValues = writeDataCaptor.getAllValues();

		List<Integer> expectedAddresses = Arrays.asList(new Integer[] {0, 0, 254, 0, 0});
		List<Byte> expectedData = Arrays.asList(new Byte[] {0, 16, 121, 0, -128});

		assertEquals(actualWriteAddressValues, expectedAddresses, "WriteAddresses do not match expected.");
		assertEquals(actualDataValues, expectedData, "Data values do not match expected.");
	}

	@Test
	public void initWithoutRaspberryPi() throws Exception {

		controller = new PWMController(null, servoDriver);
		controller.init();

		verify(servoDriver, times(0)).write(writeAddressCaptor.capture(), writeDataCaptor.capture());
	}
}
