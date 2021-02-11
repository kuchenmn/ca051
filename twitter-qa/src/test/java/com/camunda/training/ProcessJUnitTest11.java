package com.camunda.training;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
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
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.taskService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask;

public class ProcessJUnitTest11 {
    @Mock
    TwitterService twitterService;

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();
    @Before
    public void setup() {
        init(rule.getProcessEngine());
        MockitoAnnotations.initMocks(this);
        Mocks.register("createTweetDelegateEx11", new CreateTweetDelegateEx9(twitterService));
    }

    @Test
    @Deployment(resources = "ex11.bpmn")
    public void testHappyPath() {
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<>();
        variables.put("content", "Exercise 8 test - " + System.currentTimeMillis());
        // Start process with Java API and variables
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcessEx11", variables);
        // Make assertions on the process instance
        assertThat(processInstance).isWaitingAt("ReviewTweetTask");
        List<Task> taskList = taskService()
                .createTaskQuery()
                .taskCandidateGroup("management")
                .processInstanceId(processInstance.getId())
                .list();
        assertThat(taskList).isNotNull();
        assertThat(taskList).hasSize(1);
        Task task = taskList.get(0);
        Map<String, Object> approvedMap = new HashMap<>();
        approvedMap.put("approved", true);
        taskService().complete(task.getId(), approvedMap);

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
    @Deployment(resources = "ex11.bpmn")
    public void testTweetRejected() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("approved", false);
        ProcessInstance processInstance = runtimeService()
                .createProcessInstanceByKey("TwitterQAProcessEx11")
                .setVariables(varMap)
                .startAfterActivity("ReviewTweetTask")
                .execute();

        assertThat(processInstance)
                .isWaitingAt("SendNotificationTask")
                .externalTask()
                .hasTopicName("notification");
        complete(externalTask());

        assertThat(processInstance).isEnded().hasPassed("TweetRejectedEndEvent");
    }

    @Test
    @Deployment(resources = "ex11.bpmn")
    public void testTweetSuperUser() {
        ProcessInstance processInstance = runtimeService()
                .createMessageCorrelation("superuserTweet11")
                .setVariable("content", "My Exercise 11 Tweet - " + System.currentTimeMillis())
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

    @Test
    @Deployment(resources= "ex11.bpmn")
    public void testTweetWithdrawn() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("content", "Test tweetWithdrawn message");
        ProcessInstance processInstance = runtimeService()
                .startProcessInstanceByKey("TwitterQAProcessEx11", varMap);
        assertThat(processInstance).isStarted().isWaitingAt("ReviewTweetTask");
        runtimeService()
                .createMessageCorrelation("tweetWithdrawn11")
                .processInstanceVariableEquals("content", "Test tweetWithdrawn message")
                .correlateWithResult();
        assertThat(processInstance).isEnded();
    }

}
