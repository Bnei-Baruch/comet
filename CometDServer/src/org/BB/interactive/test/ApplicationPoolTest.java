package org.BB.interactive.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.BB.interactive.Application;
import org.BB.interactive.ApplicationPool;
import org.junit.Test;

public class ApplicationPoolTest {

	@Test
	public void TestReadApplicationPool() throws IOException
	{
		ApplicationPool appPool = new ApplicationPool("src/org/BB/interactive/test/application.conf");
		assertEquals(appPool.size(), 4);
		Application app = appPool.getApp("1");
		assertEquals(app.id, "1");
		assertEquals(app.secretKey, "01234567890abcde01234567890abcde");
		assertEquals(app.ivParam, "fedcba9876543210fedcba9876543210");
		app = appPool.getApp("2");
		assertEquals(app.id, "2");
		assertEquals(app.secretKey, "01234567890abcde01234567890abcde");
		assertEquals(app.ivParam, "fedcba9876543210fedcba9876543210");
		app = appPool.getApp("1234");
		assertEquals(app.id, "1234");
		assertEquals(app.secretKey, "01234567890abcde01234567890abcde");
		assertEquals(app.ivParam, "fedcba9876543210fedcba9876543210");
		app = appPool.getApp("kuku");
		assertEquals(app.id, "kuku");
		assertEquals(app.secretKey, "01234567890abcde01234567890abcde");
		assertEquals(app.ivParam, "fedcba9876543210fedcba9876543210");
	}
}
