package com.atlassian.plugins.tutorial.servicedesk.when;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.servicedesk.plugins.automation.api.execution.command.RuleExecutionCommand;
import com.atlassian.servicedesk.plugins.automation.api.execution.command.RuleExecutionCommandBuilder;
import com.atlassian.servicedesk.plugins.automation.api.execution.command.RuleExecutionCommandBuilderService;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.RuleMessage;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.RuleMessageBuilder;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.RuleMessageBuilderService;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.IssueMessageHelper;
import com.atlassian.servicedesk.plugins.automation.api.execution.whenhandler.WhenHandlerContext;
import com.atlassian.servicedesk.plugins.automation.spi.rulewhen.event.EventWhenHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fires off rules when an issue is created
 */
public final class AssigneeChangedEventWhenHandler implements EventWhenHandler<IssueEvent>
{
    private static final Logger LOG = LoggerFactory.getLogger(AssigneeChangedEventWhenHandler.class);

    @Autowired
    private IssueMessageHelper issueMessageHelper;
    @Autowired
    private RuleMessageBuilderService ruleMessageBuilderService;
    @Autowired
    private RuleExecutionCommandBuilderService ruleExecutionCommandBuilderService;
    @Autowired
    private ProjectAndUserChecker projectAndUserChecker;

    @Autowired
    private IssueManager issueManager;
    @Autowired
    private UserManager userManager;

    @Override
    public Class<IssueEvent> getEventClass()
    {
        return IssueEvent.class;
    }

    /**
     * This method is invoked whenever an IssueEvent is fired.
     *
     * @param contexts contains the context for each assignee changed when handler that is configured as part of a rule
     * @param event the event that was fired
     * @return a list of rule execution commands; a rule execution will be performed for each member of this list
     */
    @Override
    public List<RuleExecutionCommand> handleEvent(
            final @Nonnull List<WhenHandlerContext> contexts,
            final @Nonnull IssueEvent event)
    {
        // If the assignee of the issue has not changed, we don't want to trigger any rule executions.
        // We do this by returning an empty list.
        if (!hasAssigneeChanged(event))
        {
            return Collections.emptyList();
        }

        // Here, we create the message that will be passed to ifs and thens. This message contains any contextual
        // information they need to do their job.
        final RuleMessage messageForIfsAndThens = createRuleMessage(event);

        // Now we need to build up our list of rule execution commands. We create a rule execution for each provided
        // when handler context.
        final RuleExecutionCommandBuilder ruleExecutionStub = ruleExecutionCommandBuilderService.builder()
                .requestSynchronousExecution(false)
                .ruleMessage(messageForIfsAndThens);

        final List<RuleExecutionCommand> ruleExecutions = new ArrayList<RuleExecutionCommand>();
        for (final WhenHandlerContext context : contexts)
        {
            if (projectAndUserAllowed(context, event))
            {
                RuleExecutionCommand command = ruleExecutionStub.ruleReference(context.getRuleReference()).build();
                ruleExecutions.add(command);
            }
        }

        return ruleExecutions;
    }

    public boolean hasAssigneeChanged(final IssueEvent event)
    {
        ChangeItemBean changeItem = getChangeItem(IssueFieldConstants.ASSIGNEE, event);
        if (changeItem == null)
        {
            return false;
        }

        return !StringUtils.defaultString(changeItem.getFrom()).equals(StringUtils.defaultString(changeItem.getTo()));
    }

    /**
     * Returns the change item bean that is associated to this field name in the context of this event.
     * Returns null if the field has not changed in the context of this event or if the issue has just been created
     *
     * @param fieldName the field name to look for
     * @param event     the event
     * @return a ChangeItemBean or null.
     */
    protected ChangeItemBean getChangeItem(final String fieldName, final IssueEvent event)
    {
        if (event.getChangeLog() == null)
        {
            return null;
        }

        ChangeHistory history = new ChangeHistory(event.getChangeLog(), issueManager, userManager);

        for (ChangeItemBean changeItem : history.getChangeItemBeans())
        {
            if (changeItem.getField().equals(fieldName))
            {
                return changeItem;
            }
        }
        return null;
    }

    /**
     * The rule message is what is passed to the ifs and thens. When we defined our when handler module in
     * atlassian-plugin.xml, we stated that we provide both the issue and the user, so we need to populate the rule
     * message with both here.
     */
    private RuleMessage createRuleMessage(final IssueEvent event)
    {
        final RuleMessageBuilder builder = ruleMessageBuilderService.builder();

        populateBuilderWithIssue(builder, event);
        populateBuilderWithUser(builder, event);

        return builder.build();
    }

    private void populateBuilderWithIssue(final RuleMessageBuilder toPopulate, IssueEvent event)
    {
        issueMessageHelper.setIssueData(toPopulate, event.getIssue());
    }

    private void populateBuilderWithUser(final RuleMessageBuilder toPopulate, IssueEvent event)
    {
        final ApplicationUser user = ApplicationUsers.from(event.getUser());
        if (user != null)
        {
            toPopulate.put("userKey", user.getKey());
        }
    }

    /**
     * Checks whether the project the when handler has been configured in is the same project that issue is in, and
     * also checks that the configured user has browse permissions for the issue.
     */
    private boolean projectAndUserAllowed(final @Nonnull WhenHandlerContext context, final @Nonnull IssueEvent event)
    {
        final Issue issue = event.getIssue();

        // check project context first
        if (!projectAndUserChecker.isApplicableProject(context, issue.getProjectObject()))
        {
            return false;
        }

        // check view permission
        if (!projectAndUserChecker.canBrowseIssue(context, issue))
        {
            return false;
        }

        return true;
    }
}
