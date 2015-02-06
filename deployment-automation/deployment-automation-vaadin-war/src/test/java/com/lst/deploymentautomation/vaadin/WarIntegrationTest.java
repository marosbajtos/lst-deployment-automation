/*
 *********************************************************************

 $Id$


 Copyright (c) 2007-2014 Whitestein Technologies AG,
 Riedstrasse 13, CH-6330 Cham, Switzerland.
 All rights reserved.

 This software is confidential and proprietary information of
 Whitestein Technologies AG.
 You shall not disclose this confidential information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with Whitestein Technologies AG.
 The use of this file in source or binary form requires a written
 license from Whitestein Technologies AG.
 *********************************************************************
 */
package com.lst.deploymentautomation.vaadin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import org.junit.Test;

import com.whitestein.lsps.common.test.PackagingTestHelper;

public class WarIntegrationTest {

	@Test
	public void testWarLibs() throws ZipException, IOException {
		File targetDir = new File(new File(".").getAbsoluteFile().getParentFile(), "target");
		File[] warFiles = targetDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".war");
			}
		});

		assertEquals("Invalid number of war files", 1, warFiles.length);

		File warFile = warFiles[0];

		PackagingTestHelper helper = new PackagingTestHelper();
		helper.loadFiles(warFile, "WEB-INF/lib/");
		helper.loadPatterns("libs.txt");

		List<String> invalidFiles = helper.getInvalidFiles();
		if (!invalidFiles.isEmpty()) {
			fail("Files " + invalidFiles + " do not match any pattern");
		}

		List<String> missing = helper.getInvalidPatterns();
		if (!missing.isEmpty()) {
			fail("Patterns " + missing + " are not matched by any file");
		}
	}
}
