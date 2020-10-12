package com.camunda.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("createTweetDelegateEx11")
public class CreateTweetDelegateEx11 implements JavaDelegate {
    private TwitterService twitterService;

    @Inject
    public CreateTweetDelegateEx11(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String content = (String) execution.getVariable("content");
        twitterService.publishTweet(content);
    }
}
