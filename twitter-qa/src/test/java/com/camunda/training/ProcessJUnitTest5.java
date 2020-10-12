package com.camunda.training;

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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class ProcessJUnitTest5 {
    @Mock
    TwitterService twitterService;

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();
    @Before
    public void setup() {
        init(rule.getProcessEngine());
        MockitoAnnotations.initMocks(this);
        Mocks.register("createTweetDelegateEx5", new CreateTweetDelegateEx5(twitterService));
    }

    @Test
    @Deployment(resources = "ex5.bpmn")
    public void testHappyPath() throws Exception {
        // Create a HashMap to put in variables for the process instance
        Map<String, Object> variables = new HashMap<>();
//        variables.put("approved", true);
        variables.put("content", "Exercise 5 test");
        // Start process with Java API and variables
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcessEx5", variables);

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
        assertThat(task).hasCandidateGroup("management");
        Map<String, Object> approvedMap = new HashMap<>();
        approvedMap.put("approved", true);
        taskService().complete(task.getId(), approvedMap);

//        shorter version
//        assertThat(processInstance).task("ReviewTweetTask");
//        assertThat(processInstance).task().hasCandidateGroup("management");
//        complete(task(), withVariables("approved", true));

        verify(twitterService).publishTweet(anyString());
        assertThat(processInstance).isEnded();
    }

}
