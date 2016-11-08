@PLUGIN@-specific actions
=========================

Add-velocity-comment action can send a message to Redmine and is implemented using the action add-comment

##use-case1
[rule "myrule"]
    event-type = comment-added
    action = add-velocity-comment inline $author-name uploaded change $patch-set-number ($commit-message) at $change-url 

Will create a new note in Redmine but the **commit-message** will be formated to contain only the header

##use-case2
[rule "myrule2"]
    event-type = patchset-created
    patch-set-number = 1
    action = add-velocity-comment inline $author-name uploaded change $patch-set-number ($commit-message) at $change-url
The [basic actions][basic-actions] are available.

[basic-actions]: config-rulebase-common.md#actions



[Back to @PLUGIN@ documentation index][index]

[index]: index.html
