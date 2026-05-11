package com.InsuranceManagementSystem.AdminService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "smartsure.exchange";

    public static final String USER_REGISTERED_QUEUE = "user.registered";
    public static final String POLICY_PURCHASED_QUEUE = "policy.purchased";
    public static final String POLICY_EXPIRING_QUEUE = "policy.expiring";
    public static final String CLAIM_SUBMITTED_QUEUE = "claim.submitted";
    public static final String CLAIM_REVIEWED_QUEUE = "claim.reviewed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Queue policyPurchasedQueue() {
        return new Queue(POLICY_PURCHASED_QUEUE, true);
    }

    @Bean
    public Queue policyExpiringQueue() {
        return new Queue(POLICY_EXPIRING_QUEUE, true);
    }

    @Bean
    public Queue claimSubmittedQueue() {
        return new Queue(CLAIM_SUBMITTED_QUEUE, true);
    }

    @Bean
    public Queue claimReviewedQueue() {
        return new Queue(CLAIM_REVIEWED_QUEUE, true);
    }

    @Bean public Binding bindUserRegistered() { return BindingBuilder.bind(userRegisteredQueue()).to(exchange()).with(USER_REGISTERED_QUEUE); }
    @Bean public Binding bindPolicyPurchased() { return BindingBuilder.bind(policyPurchasedQueue()).to(exchange()).with(POLICY_PURCHASED_QUEUE); }
    @Bean public Binding bindPolicyExpiring() { return BindingBuilder.bind(policyExpiringQueue()).to(exchange()).with(POLICY_EXPIRING_QUEUE); }
    @Bean public Binding bindClaimSubmitted() { return BindingBuilder.bind(claimSubmittedQueue()).to(exchange()).with(CLAIM_SUBMITTED_QUEUE); }
    @Bean public Binding bindClaimReviewed() { return BindingBuilder.bind(claimReviewedQueue()).to(exchange()).with(CLAIM_REVIEWED_QUEUE); }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter());
        return template;
    }
}
