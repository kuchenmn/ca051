package com.camunda.training;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

public class ProcessJUnitTest12 {
    @Mock
    TwitterService twitterService;

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();
    @Before
    public void setup() {
        init(rule.getProcessEngine());
        MockitoAnnotations.initMocks(this);
        Mocks.register("createTweetDelegateEx12", new CreateTweetDelegateEx12(twitterService));
    }

    @Test
    @Deployment(resources = {"ex12Decision.dmn", "ex12.bpmn"})
    public void testHappyPath() {
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<>();
        variables.put("content", "camunda rocks");
        variables.put("email", "jane.doe@foo.com");
        // Start process with Java API and variables
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcessEx12", variables);
        // execute the job
        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();

        // execute the job
        assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = {"ex12Decision.dmn", "ex12.bpmn"})
    public void testTweetRejected() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("approved", false);
        ProcessInstance processInstance = runtimeService()
                .createProcessInstanceByKey("TwitterQAProcessEx12")
                .setVariables(varMap)
                .startAfterActivity("ReviewTweetTask")
                .execute();

        assertThat(processInstance).isEnded().hasPassed("TweetRejectedEndEvent");
    }

    @Test
    @Deployment(resources = {"ex12Decision.dmn", "ex12.bpmn"})
    public void testTweetSuperUser() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("approved", false);
        ProcessInstance processInstance = runtimeService()
                .createMessageCorrelation("superuserTweet")
                .setVariable("content", "My Exercise 12 Tweet - " + System.currentTimeMillis())
                .correlateWithResult()
                .getProcessInstance();

        assertThat(processInstance).isStarted();

        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();

        // execute the job
        assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);

        assertThat(processInstance).isEnded();
    }

//    @Test
    @Deployment(resources = {"ex12Decision.dmn", "ex12.bpmn"})
    public void testTweetWithdrawn() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("content", "Test tweetWithdrawn message");
        ProcessInstance processInstance = runtimeService()
                .startProcessInstanceByKey("TwitterQAProcessEx12", varMap);
        assertThat(processInstance).isStarted().isWaitingAt("ReviewTweetTask");
        runtimeService()
                .createMessageCorrelation("tweetWithdrawn")
                .processInstanceVariableEquals("content", "Test tweetWithdrawn message")
                .correlateWithResult();
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = {"ex12Decision.dmn", "ex12.bpmn"})
    public void testTweetFromMe() {
        Map<String, Object> variables = withVariables("email", "norbert.kuchenmeister@camunda.org", "content", "this should be published");
        DmnDecisionTableResult decisionResult = decisionService().evaluateDecisionTableByKey("tweetApproval", variables);

        assertThat(decisionResult.getFirstResult()).contains(entry("approved", true));
    }
}
