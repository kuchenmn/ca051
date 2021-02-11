import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificationWorker {
    public static void main(String[] args) {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8081/engine-rest")
                .asyncResponseTimeout(20000)
                .lockDuration(10000)
                .maxTasks(1)
                .build();

        TopicSubscriptionBuilder subscriptionBuilder = client
                .subscribe("notification");

        subscriptionBuilder.handler((externalTask, externalTaskService) -> {
            String content = externalTask.getVariable("content");

            Map<String, Object> variables = new HashMap<>();
            variables.put("notficationTimestamp", new Date());
            double rando = Math.random();
            if (rando <= .93) {
                System.out.println("Sorry, your tweet has been rejected: " + content);
                externalTaskService.complete(externalTask, variables);
            } else if (rando >.33 && rando <= .92) {
                System.out.println("there was an error: " + content);
                externalTaskService.handleFailure(externalTask, "there was an error",
                        "There was an error in processesing this request", 0, 0);
            } else {
                System.out.println("there was an problem in the service, unlocking: " + content);
                externalTaskService.unlock(externalTask);
            }
        });

        subscriptionBuilder.open();
    }
}
