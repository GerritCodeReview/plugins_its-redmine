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

import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.transaction.InvalidTransactionException;

public class RedmineClientAdapter {
  private static final Logger log = LoggerFactory.getLogger(RedmineClientAdapter.class);
  private RedmineManager mgr;

  public RedmineClientAdapter(String url, String apiKey) {
    this.mgr = RedmineManagerFactory.createWithApiKey(url, apiKey);
  }

  /**
   * Add a note on a existing issue in Redmine
   *
   * @param strIssueId a Redmine issue id string
   * @param comment    a generated comment from the information in Gerrit
   */
  public void addNotesIssue(String strIssueId, String comment) throws
      InvalidTransactionException, NumberFormatException {
    IssueManager issueManager = this.mgr.getIssueManager();
    int issueId = Integer.parseInt(strIssueId);
    try {
      Issue issue = issueManager.getIssueById(issueId, Include.watchers);
      issue.setNotes(comment);
      issueManager.update(issue);
    } catch (RedmineException e) {
      String error = "Issue:" + issueId + " could not be updated";
      log.error(error);
      throw new InvalidTransactionException(e.toString() + error);
    }
  }

  public String accessHealthCheck() {
    try {
      ProjectManager projectManager = this.mgr.getProjectManager();
      List<Project> list = projectManager.getProjects();
      return "{\"status\"=\"ok\"}";
    } catch (RedmineException e) {
      return "{\"status\"=\"error\"}";
    }
  }

  public String sysHealthCheck() {
    return "{\"status\"=\"not implemented\"}";
  }

  public boolean exits(String issueId) {
    IssueManager issueManager = this.mgr.getIssueManager();
    try {
      Issue issue = issueManager.getIssueById(Integer.parseInt(issueId), Include.watchers);
      return true;
    } catch (RedmineException e) {
      return false;
    }
  }
}
