/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import static org.testng.Assert.assertFalse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests methods in the {@link HostConfigurationUtils} class.
 */
@Test(groups = TestGroup.INTEGRATION)
public class HostConfigurationUtilsTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HostConfigurationUtilsTest.class);

  public void testHostRequest() {
    final List<Configuration> configurations = HostConfigurationUtils.getConfiguration(System.getProperty("opengamma.engine.host"));
    assertFalse(configurations.isEmpty());
    for (Configuration configuration : configurations) {
      s_logger.info("Found \"{}\" at \"{}\"", configuration.getDescription(), configuration.getURI());
    }
  }

}
