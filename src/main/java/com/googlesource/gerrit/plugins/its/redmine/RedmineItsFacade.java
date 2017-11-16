// Copyright (C) 2013 The Android Open Source Project
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

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.its.base.its.ItsFacade;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.NotFoundException;


public class RedmineItsFacade implements ItsFacade {
  private static final int MAX_ATTEMPTS = 3;
  private static final String GERRIT_CONFIG_URL = "url";
  private static final String GERRIT_CONFIG_API_KEY = "apiKey";

  private Logger log = LoggerFactory.getLogger(RedmineItsFacade.class);

  private final String pluginName;
  private Config gerritConfig;
  private RedmineClient client;

  @Inject
  public RedmineItsFacade(@PluginName String pluginName,
      @GerritServerConfig Config gerritConfig) {
    this.pluginName = pluginName;
    this.gerritConfig = gerritConfig;
    log.trace("Initialize its-redmine to {} host", getUrl());
    try {
      client();
    } catch (Exception e) {
      log.error("Unable to connect to its-redmine: {}",e.getMessage());
    }
  }

  @Override
  public String healthCheck(final Check check) throws IOException {
      return execute(new Callable<String>(){
        @Override
        public String call() throws Exception {
          if (check.equals(Check.ACCESS))
            return healthCheckAccess();
          else
            return healthCheckSysinfo();
        }});
  }

  @Override
  public void addComment(final String issueId, final String comment) throws IOException {
    if (comment == null || comment.trim().isEmpty()) {
      return;
    }
    execute(new Callable<String>(){
      @Override
      public String call() throws Exception {
        try {
          client().updateIssue(issueId,comment);
        } catch (Exception e) {
          log.error("Error in add comment: {}", e.getMessage(), e);
          throw e;
        }
        return issueId;
      }
    });
  }

  @Override
  public void addRelatedLink(final String issueKey, final URL relatedUrl, String description)
      throws IOException {
    addComment(issueKey, "Related URL: " + createLinkForWebui(relatedUrl.toExternalForm(), description));
  }

  @Override
  public String createLinkForWebui(String url, String text) {
    String ret = url;
    if (text != null && ! text.equals(url)) {
        ret += " (" + text + ")";
    }
    return ret;
  }

  @Override
  public boolean exists(final String issueId) throws IOException {
    return execute(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
       return client().exists(issueId);
      }
    });
  }

  /**
   * @see com.googlesource.gerrit.plugins.hooks.its.ItsFacade#performAction(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void performAction(final String issueId, final String actionName)
      throws IOException {
    execute(new Callable<String>() {
      @Override
      public String call() throws RedmineException, IOException {
        doPerformAction(issueId, actionName);
        return issueId;
      }
    });
  }

  private void doPerformAction(final String issueKey, final String actionName)
      throws IOException, RedmineException {
    client().doPerformAction(issueKey,actionName);
  }

  private RedmineClient client() throws IOException {
    if (client == null) {
      try {
        log.debug("Connecting to redmine at URL " + getUrl());
        client = new RedmineClient(getUrl(),getApiKey());
      } catch (Exception e) {
        log.info("Unable to connect to " + getUrl());
        throw new IOException(e);
      }
    }
    return client;
  }

  private String getUrl() {
    final String url = gerritConfig.getString(pluginName, null, GERRIT_CONFIG_URL);
    return url;
  }

  private String getApiKey() {
    final String apiKey = gerritConfig.getString(pluginName, null, GERRIT_CONFIG_API_KEY);
    return apiKey;
  }

  private String healthCheckAccess() throws IOException {
    return execute(new Callable<String>() {
      @Override
      public String call() throws IOException {
        return client().healthCheckAccess();
      }
    });
  }

  private String healthCheckSysinfo() throws IOException {
    return execute(new Callable<String>() {
      @Override
      public String call() throws IOException {
        return client().healthCheckSysinfo(getUrl());
      }
    });
  }

  private <P> P execute(Callable<P> function) throws IOException {
    int attempt = 0;
    while (true) {
      try {
        return function.call();
      } catch (Exception ex) {
        if (isRecoverable(ex) && ++attempt < MAX_ATTEMPTS) {
          log.debug("Call failed - retrying, attempt {} of {}", attempt,
              MAX_ATTEMPTS);
          continue;
        }

        if (ex instanceof IOException) {
          throw ((IOException) ex);
        }
        throw new IOException(ex);
      }
    }
  }

  private boolean isRecoverable(Exception ex) {
    return false;
  }
}
