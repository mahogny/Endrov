package test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll
	{

	public static Test suite()
		{
		TestSuite suite = new TestSuite("Test for test");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestFrametime.class);
		//$JUnit-END$
		return suite;
		}

	}
