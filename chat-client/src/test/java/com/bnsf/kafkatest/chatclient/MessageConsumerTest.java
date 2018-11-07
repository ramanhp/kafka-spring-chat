package com.bnsf.kafkatest.chatclient;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.bnsf.kafkatest.chatclient.MessageConsumerTest.CHAT_TOPIC;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class MessageConsumerTest {

    public static final String CHAT_TOPIC =  "all.messages.test";
    @Autowired
    private MessageListener listener;

    @Test
    public void testMessageReceived() throws Exception {
        template.send(CHAT_TOPIC, "Test Message");
        template.flush();
        assertTrue(this.listener.latch.await(60, TimeUnit.SECONDS));
    }

    @Autowired
    private KafkaTemplate<Integer, String> template;

    @Configuration
    @EnableKafka
    public static class Config {

        @Bean
        public KafkaEmbedded kafkaEmbedded() {
            return new KafkaEmbedded(1, true, 1, CHAT_TOPIC);
        }

        @Bean
        public ConsumerFactory<Integer, String> createConsumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaEmbedded().getBrokersAsString());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(createConsumerFactory());
            return factory;
        }

        @Bean
        public MessageListener listener() {
            return new MessageListener();
        }

        @Bean
        public ProducerFactory<Integer, String> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaEmbedded().getBrokersAsString());
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<Integer, String> kafkaTemplate() {
            return new KafkaTemplate<Integer, String>(producerFactory());
        }
    }

}

class MessageListener {
    public final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = CHAT_TOPIC)
    public void listen(String foo) {
        this.latch.countDown();
    }
}
