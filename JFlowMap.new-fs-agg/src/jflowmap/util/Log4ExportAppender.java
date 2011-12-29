/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.util;

import java.util.LinkedList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class Log4ExportAppender extends AppenderSkeleton {

  private long maxNumOfMessages = 1000000;
  private final LinkedList<Pair<Long, String>> messageEvents = Lists.newLinkedList();

  public Log4ExportAppender() {
    setLayout(new PatternLayout("%d{HH:mm.ss,SSS} %-5p - %m%n"));
  }

  public static Log4ExportAppender createAndSetup() {
    Log4ExportAppender app = new Log4ExportAppender();
    setup(app);
    return app;
  }

  public static void setup(Log4ExportAppender app) {
    Logger rootLogger = Logger.getRootLogger();
    rootLogger.addAppender(app);
  }

  /**
   * @param timestamp The number of milliseconds elapsed from 1/1/1970.
   */
  public Iterable<String> getMessagesAfter(final long timestamp) {
    return messagesOf(Iterables.filter(messageEvents, new Predicate<Pair<Long, String>>() {
      @Override
      public boolean apply(Pair<Long, String> input) {
        return input.first() > timestamp;
      }
    }));
  }

  public Iterable<String> getMessages() {
    return messagesOf(messageEvents);
  }

  public static Iterable<String> messagesOf(Iterable<Pair<Long, String>> messages) {
    return Iterables.transform(messages, new Function<Pair<Long, String>, String>() {
      @Override
      public String apply(Pair<Long, String> from) {
        return from.second();
      }
    });
  }

  @Override
  protected void append(LoggingEvent ev) {
    if (layout == null) {
      errorHandler.error("No layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);
      return;
    }

    addMessage(ev.timeStamp, layout.format(ev));

    // Boilerplate code: handle exceptions if not handled by layout object
    if (layout.ignoresThrowable()) {
      String[] messages = ev.getThrowableStrRep();
      if (messages != null) {
        for (String msg : messages) {
          addMessage(ev.timeStamp, msg);
        }
      }
    }
  }

  private void addMessage(long timestamp, String msg) {
    messageEvents.add(Pair.of(timestamp, msg));
    applySizeRestriction();
  }

  private void applySizeRestriction() {
    while (!messageEvents.isEmpty()  &&   messageEvents.size() > maxNumOfMessages) {
      messageEvents.remove();
    }
  }

  public void setMaxNumOfMessages(long maxNumOfMessages) {
    this.maxNumOfMessages = maxNumOfMessages;
    applySizeRestriction();
  }

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

}
