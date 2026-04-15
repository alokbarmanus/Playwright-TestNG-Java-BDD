package com.playwrightproject.runner;

import com.playwrightproject.utils.JsonDataLoader;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CucumberOptions(
    features = "classpath:features",
    glue = {"com.playwrightproject.steps", "com.playwrightproject.hooks"},
    plugin = {"pretty", "html:target/cucumber-reports"}
)
public class CucumberTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider
    public Object[][] scenarios() {
        Object[][] baseScenarios = super.scenarios();
        List<Object[]> expandedScenarios = new ArrayList<>();

        for (Object[] scenario : baseScenarios) {
            PickleWrapper pickleWrapper = (PickleWrapper) scenario[0];
            Optional<String> dataFileTag = pickleWrapper.getPickle().getTags().stream()
                    .filter(tag -> tag.startsWith("@dataFile:"))
                    .findFirst();

            if (dataFileTag.isPresent()) {
                String rawPath = dataFileTag.get().substring("@dataFile:".length()).trim();
                String filePath = resolveDataFilePath(rawPath);
                int records = Math.max(1, JsonDataLoader.countRecords(filePath));
                for (int i = 0; i < records; i++) {
                    expandedScenarios.add(new Object[]{scenario[0], scenario[1]});
                }
            } else {
                expandedScenarios.add(new Object[]{scenario[0], scenario[1]});
            }
        }

        return expandedScenarios.toArray(new Object[0][0]);
    }

    @Override
    @Test(dataProvider = "scenarios", alwaysRun = true)
    public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        super.runScenario(pickleWrapper, featureWrapper);
    }

    private String resolveDataFilePath(String rawPath) {
        String env = System.getProperty("env", "dev");
        return rawPath.replace("${env}", env);
    }
}
