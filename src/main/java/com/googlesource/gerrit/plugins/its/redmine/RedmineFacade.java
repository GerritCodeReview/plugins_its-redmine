// Copyright (C) 2015 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.its.redmine;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.its.base.its.ItsFacade;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.InvalidTransactionException;

public class RedmineFacade implements ItsFacade {
  private static final Logger log = LoggerFactory.getLogger(RedmineFacade.class);
  private static final String GERRIT_CONFIG_URL = "host";
  private static final String GERRIT_CONFIG_API = "apikey";
  private static final int MAX_ATTEMPTS = 3;
  private final Pattern commentPattern;
  private final Pattern issuePattern;

  private Config config;
  private String pluginName;
  private RedmineClientAdapter clientAdapter;

  @Inject
  public RedmineFacade(@PluginName String pluginName,
                       @GerritServerConfig Config config) {
    this.pluginName = pluginName;
    this.config = config;
    this.commentPattern = Pattern.compile("\\(\\p{all}*\\)");
    this.issuePattern = Pattern.compile("[0-9]*");
    connect();
  }

  @Override
  public String healthCheck(final ItsFacade.Check check) throws IOException {
    return execute(new Callable<String>() {
      @Override
      public String call() {
        if (check.equals(Check.ACCESS)) {
          return clientAdapter.accessHealthCheck();
        } else {
          return clientAdapter.sysHealthCheck();
        }
      }
    });
  }

  @Override
  public void addComment(final String issueId, final String comment) throws IOException {
      execute(new Callable<String>() {
        @Override
        public String call() {
          final String strIssueId = sanitizeStrIssueId(issueId);
          if (!strIssueId.isEmpty()) {
            try {
              clientAdapter.addNotesIssue(strIssueId, comment);
              return "{\"status\"=\"ok\"}";
            } catch (InvalidTransactionException e) {
              log.error("Invalid transaction :" + e.toString());
              return "{\"status\"=\"error\"}";
            }
          }
          return "{\"status\"=\"error\"}";
        }
      });
  }

  @Override
  public boolean exists(final String issueId) throws IOException {
    return execute(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        String isssueStr = sanitizeStrIssueId(issueId);
        boolean exist = false;
        if (!isssueStr.isEmpty()) {
          exist = clientAdapter.exits(isssueStr);
        }
        return exist;
      }
    });
  }

  private String sanitizeStrIssueId(String issueId) {
    Matcher m = issuePattern.matcher(issueId);
    if (m.find()) {
      return m.group();
    }
    return "";
  }

  private String getApi() {
    return config.getString(pluginName, null, GERRIT_CONFIG_API);
  }

  private String getUrl() {
    return config.getString(pluginName, null, GERRIT_CONFIG_URL);
  }

  public void connect() {
    clientAdapter = new RedmineClientAdapter(getUrl(), getApi());
  }

  private <T> T execute(Callable<T> function) throws IOException {
    int attempt = 0;
    while (true) {
      try {
        return function.call();
      } catch (Exception e) {
        if (isRecoverable(e) && ++attempt < MAX_ATTEMPTS) {
          log.debug("Function call failed");
          log.debug("Will retry , attempt " + attempt);
          continue;
        }
        if (e instanceof IOException) {
          throw ((IOException) e);
        } else {
          throw new IOException(e);
        }
      }
    }
  }

  private boolean isRecoverable(Exception e) {
    return false;
  }

  @Override
  public String createLinkForWebui(String url, String text) {
    return null;
  }

  @Override
  public void performAction(String issueId, String actionName) throws IOException {
  }

  @Override
  public void addRelatedLink(String issueId, URL relatedUrl, String description) throws IOException {
  }
}
