package com.zeremonos.wastecollection.bdd;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "html:target/cucumber-reports/cucumber.html")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.zeremonos.wastecollection.bdd.steps")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
public class CucumberTestRunner {
}

