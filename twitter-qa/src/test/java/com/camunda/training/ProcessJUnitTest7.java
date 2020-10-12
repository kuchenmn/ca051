package com.camunda.training;

import org.assertj.core.api.Assertions;
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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class ProcessJUnitTest7 {
    @Mock
    TwitterService twitterService;

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Before
    public void setup() {
        init(rule.getProcessEngine());
        MockitoAnnotations.initMocks(this);
        Mocks.register("createTweetDelegateEx7", new CreateTweetDelegateEx7(twitterService));
    }

    @Test
    @Deployment(resources = "ex7.bpmn")
    public void testHappyPath() throws Exception {
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<>();
        variables.put("content", "Exercise 7 test");
        // Start process with Java API and variables
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcessEx7", variables);
        // Make assertions on the process instance
        assertThat(processInstance).task("ReviewTweetTask");
        assertThat(processInstance).task().hasCandidateGroup("management");
        complete(task(), withVariables("approved", true));

        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();

        // execute the job
        Assertions.assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);
        verify(twitterService).publishTweet(anyString());
        assertThat(processInstance).isEnded();
    }

}
