package com.grsdev7.videoconf.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;

@SpringBootTest
public class MediaProcessorIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldGiveUniqueBean() {

        MediaProcessor bean = applicationContext.getBean(MediaProcessor.class);
        Assertions.assertThat(bean).isNotNull();

        MediaProcessor bean1 = applicationContext.getBean(MediaProcessor.class);
        Assertions.assertThat(bean1).isNotEqualTo(bean);
    }
}
