package com.lst.deploymentautomation;

import org.junit.BeforeClass;
import org.junit.Test;

import com.whitestein.lsps.common.ErrorException;
import com.whitestein.lsps.lang.exception.ValidationException;
import com.whitestein.lsps.test.LspsTestCaseWithExpressions;

public class SampleTest extends LspsTestCaseWithExpressions {

	@BeforeClass
	public static void init() throws Exception {
		// TODO set model name and version
		//modelName = "MyModel";
		//modelVersion = "1.0";
		//uploadModel();
		//startModelInstance();
	}

	@Test
	public void test1() throws ValidationException, ErrorException {
		// TODO write test
	}
}
