package com.camunda.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("createTweetDelegateEx5")
public class CreateTweetDelegateEx5 implements JavaDelegate {
    private TwitterService twitterService;

    @Inject
    public CreateTweetDelegateEx5(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String content = (String) execution.getVariable("content");
        twitterService.publishTweet(content);
    }
}
