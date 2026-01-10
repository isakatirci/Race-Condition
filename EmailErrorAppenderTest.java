package com.example.demo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailErrorAppenderTest {

    private EmailErrorAppender appender;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    @Mock
    private EmailService emailService;

    @Mock
    private ILoggingEvent loggingEvent;

    @BeforeEach
    void setUp() {
        EmailErrorAppender.resetForTesting();
        appender = new EmailErrorAppender();
        appender.setApplicationContext(applicationContext);
        appender.start();

        // Varsayılan mock davranışları - lenient kullanıyoruz çünkü her testte hepsi çağrılmayabilir
        lenient().when(applicationContext.getEnvironment()).thenReturn(environment);
        lenient().when(environment.getProperty("error.email.recipient")).thenReturn("test@example.com");
        lenient().when(environment.getProperty("error.email.enabled")).thenReturn("true");
        lenient().when(applicationContext.getBean(EmailService.class)).thenReturn(emailService);
    }

    @AfterEach
    void tearDown() {
        appender.stop();
        EmailErrorAppender.resetForTesting();
    }

    @Test
    void append_ShouldSendEmail_WhenEventIsError() {
        // Arrange
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);
        when(loggingEvent.getLoggerName()).thenReturn("com.example.TestLogger");
        when(loggingEvent.getFormattedMessage()).thenReturn("Test Error Message");
        when(loggingEvent.getThreadName()).thenReturn("main");
        
        // Act
        appender.append(loggingEvent);

        // Assert
        verify(emailService, timeout(2000)).sendEmail(
                eq("test@example.com"), 
                contains("TestLogger"), 
                contains("Test Error Message")
        );
    }

    @Test
    void append_ShouldNotSendEmail_WhenLevelIsNotError() {
        // Arrange
        when(loggingEvent.getLevel()).thenReturn(Level.INFO);

        // Act
        appender.append(loggingEvent);

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void append_ShouldIncludeExceptionInfo_WhenThrowablePresent() {
        // Arrange
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);
        when(loggingEvent.getLoggerName()).thenReturn("com.example.TestLogger");
        when(loggingEvent.getFormattedMessage()).thenReturn("Error with exception");
        when(loggingEvent.getThreadName()).thenReturn("main");
        
        IThrowableProxy throwableProxy = new ThrowableProxy(new RuntimeException("Test Exception"));
        when(loggingEvent.getThrowableProxy()).thenReturn(throwableProxy);

        // Act
        appender.append(loggingEvent);

        // Assert
        verify(emailService, timeout(2000)).sendEmail(
                anyString(), 
                contains("RuntimeException"), 
                contains("Test Exception")
        );
    }

    @Test
    void append_ShouldRespectRateLimit() {
        // Arrange
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);
        when(loggingEvent.getLoggerName()).thenReturn("com.example.TestLogger");
        when(loggingEvent.getFormattedMessage()).thenReturn("Rate Limit Test");
        when(loggingEvent.getThreadName()).thenReturn("main");

        // Act
        // Hızlıca iki kez çağır
        appender.append(loggingEvent);
        appender.append(loggingEvent);

        // Assert
        // Sadece bir kez gönderilmeli (Rate limit: 1 sn)
        verify(emailService, timeout(2000).times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void append_ShouldNotSend_WhenRecipientNotConfigured() {
        // Arrange
        // Bu test için özel stubbing
        when(environment.getProperty("error.email.recipient")).thenReturn(null);
        System.clearProperty("error.email.recipient"); 
        
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);

        // Act
        appender.append(loggingEvent);

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
    
    @Test
    void append_ShouldIncludeMDC_WhenPresent() {
        // Arrange
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);
        when(loggingEvent.getLoggerName()).thenReturn("com.example.TestLogger");
        when(loggingEvent.getFormattedMessage()).thenReturn("MDC Test");
        when(loggingEvent.getThreadName()).thenReturn("main");
        when(loggingEvent.getMDCPropertyMap()).thenReturn(Map.of("userId", "12345"));

        // Act
        appender.append(loggingEvent);

        // Assert
        verify(emailService, timeout(2000)).sendEmail(
                anyString(), 
                anyString(), 
                contains("userId: 12345")
        );
    }
}
