package com.camunda.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("createTweetDelegateEx8")
public class CreateTweetDelegateEx8 implements JavaDelegate {
    private TwitterServiceEx8 twitterService;

    @Inject
    public CreateTweetDelegateEx8(TwitterServiceEx8 twitterService) {
        this.twitterService = twitterService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String content = (String) execution.getVariable("content");
        Long tweetId = twitterService.publishTweet(content);
        execution.setVariable("tweetId", tweetId);
    }
}
