package com.atlassian.plugins.tutorial.servicedesk.rulethen;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.pocketknife.api.commons.error.AnError;
import com.atlassian.servicedesk.plugins.automation.api.execution.error.ThenActionError;
import com.atlassian.servicedesk.plugins.automation.api.execution.error.ThenActionErrorHelper;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.RuleMessage;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.IssueMessageHelper;
import com.atlassian.servicedesk.plugins.automation.spi.rulethen.ThenAction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import javax.annotation.Nonnull;

import static com.atlassian.fugue.Either.right;
import static com.atlassian.fugue.Option.some;

/**
 * Adds the label configured in its rule component to the JIRA issue which triggered the rule execution.
 *
 */
public final class IssueLabelThenAction implements ThenAction
{
    static final String ISSUE_LABEL_KEY = "issueLabel";

    private final IssueMessageHelper issueMessageHelper;
    private final ThenActionErrorHelper thenActionErrorHelper;
    private final LabelService labelService;

    @Autowired
    public IssueLabelThenAction(
            @Nonnull final IssueMessageHelper issueMessageHelper,
            @Nonnull final ThenActionErrorHelper thenActionErrorHelper,
            @Nonnull final LabelService labelService)
    {
        this.issueMessageHelper = issueMessageHelper;
        this.thenActionErrorHelper = thenActionErrorHelper;
        this.labelService = labelService;
    }

    /**
     * Retrieves the label to be added and the issue to add the label to from the supplied {@code thenActionParam}, and
     * adds this label to the issue.
     *
     * If there is any kind of issue or exception, returns a ThenActionError, otherwise returns the provided rule
     * message unmodified.
     */
    @Override
    public Either<ThenActionError, RuleMessage> invoke(final ThenActionParam thenActionParam)
    {
        // Get the label we want to add to the issue
        final Option<String> labelOpt = thenActionParam.getConfiguration().getData().getValue(ISSUE_LABEL_KEY);
        if(labelOpt.isEmpty())
        {
            return thenActionErrorHelper.error("No " + ISSUE_LABEL_KEY + " property in config data");
        }
        final String labelToAdd = labelOpt.get();

        // Get the issue to which we're adding the label
        final Either<AnError, Issue> issueEither = issueMessageHelper.getIssue(thenActionParam.getMessage());
        if (issueEither.isLeft())
        {
            // We don't perform any task if we can't get the issue from the rule message
            return thenActionErrorHelper.error(issueEither.left().get());
        }
        final Issue issueToAddLabelTo = issueEither.right().get();

        final User userAddingLabel = thenActionParam.getUser().getDirectoryUser();
        try
        {
            Option<ErrorCollection> addLabelErrors = addLabelToIssue(labelToAdd, issueToAddLabelTo, userAddingLabel);
            if(addLabelErrors.isDefined())
            {
                return thenActionErrorHelper.error(
                        createErrorMessage(
                                labelToAdd,
                                issueToAddLabelTo,
                                toPrintable(addLabelErrors.get())
                        )
                );
            }
        }
        catch (Exception e)
        {
            return thenActionErrorHelper.error(
                    createErrorMessage(
                            labelToAdd,
                            issueToAddLabelTo,
                            e.getMessage()
                    )
            );
        }

        return right(thenActionParam.getMessage());
    }

    private Option<ErrorCollection> addLabelToIssue(final String labelToAdd, final Issue issueToAddLabelTo, final User userAddingLabel)
    {
        final LabelService.AddLabelValidationResult validationResult =
                labelService.validateAddLabel(userAddingLabel, issueToAddLabelTo.getId(), labelToAdd);
        if(!validationResult.isValid())
        {
            return some(validationResult.getErrorCollection());
        }

        labelService.addLabel(userAddingLabel, validationResult, false);
        return Option.none(ErrorCollection.class);
    }

    private String createErrorMessage(final String labelBeingAdded, final Issue issueToAddLabelTo, final String errorMessage)
    {
        return String.format(
                "Unable to add label '%s' to issue with key '%s' due to the following: %s",
                labelBeingAdded,
                issueToAddLabelTo != null ? issueToAddLabelTo.getKey() : "null",
                errorMessage);
    }

    private String toPrintable(final ErrorCollection errorCollection)
    {
        final StringBuilder errorMessage = new StringBuilder();
        for(final Iterator<String> errorIter = errorCollection.getErrorMessages().iterator(); errorIter.hasNext();)
        {
            errorMessage.append(errorIter.next()).append(";");
            if(errorIter.hasNext())
            {
                errorMessage.append(" ");
            }
        }

        return errorMessage.toString();
    }
}
