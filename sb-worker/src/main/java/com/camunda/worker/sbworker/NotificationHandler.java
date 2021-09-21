package com.camunda.worker.sbworker;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@ExternalTaskSubscription("notification")
public class NotificationHandler implements ExternalTaskHandler {

    /**
     * Has been executed for each fetched and locked task
     *
     * @param externalTask        the context is represented of
     * @param externalTaskService to interact with fetched and locked tasks
     */
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String content = externalTask.getVariable("content");

        Map<String, Object> variables = new HashMap<>();
        variables.put("notficationTimestamp", new Date());
        double rando = Math.random();
        if (rando >= .73) {
            Logger.getLogger("notificationHandler").log(Level.INFO, "Sorry, your tweet has been rejected: " + content);
            externalTaskService.complete(externalTask, variables);
        } else if (rando >.33 && rando <= .72) {
            System.out.println("there was an error: " + content);
            externalTaskService.handleFailure(externalTask, "there was an error",
                    "There was an error in processesing this request", 0, 60000);
        } else {
            System.out.println("there was an problem in the service, unlocking: " + content);
            externalTaskService.unlock(externalTask);
        }
    }
}
