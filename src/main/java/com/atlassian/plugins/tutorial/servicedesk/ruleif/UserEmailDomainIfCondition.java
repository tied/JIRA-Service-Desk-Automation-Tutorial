package com.atlassian.plugins.tutorial.servicedesk.ruleif;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.pocketknife.api.commons.error.AnError;
import com.atlassian.servicedesk.plugins.automation.api.execution.error.IfConditionError;
import com.atlassian.servicedesk.plugins.automation.api.execution.error.IfConditionErrorHelper;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.UserMessageHelper;
import com.atlassian.servicedesk.plugins.automation.spi.ruleif.IfCondition;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.fugue.Either.right;

/**
 * If condition that checks whether a user's email address belongs to a specified domain.
 *
 */
public final class UserEmailDomainIfCondition implements IfCondition
{
    static final String EMAIL_DOMAIN_KEY = "email.domain";

    private final UserMessageHelper userMessageHelper;
    private final IfConditionErrorHelper ifConditionErrorHelper;

    @Autowired
    public UserEmailDomainIfCondition(
            final UserMessageHelper userMessageHelper,
            final IfConditionErrorHelper ifConditionErrorHelper)
    {
        this.userMessageHelper = userMessageHelper;
        this.ifConditionErrorHelper = ifConditionErrorHelper;
    }

    /**
     * This method is invoked whenever a rule that contains a user email domain if condition is executed.
     * If this method returns anything other than an {@code Either.right(true)}, then rule execution halts, and any
     * then actions defined as part of the rule will not be invoked.
     *
     * @param ifConditionParam contains all the contextual information required by the if condition to do its job.
     * @return Either.left upon error, or an Either.right with a boolean indicating whether the condition has been met
     *         or not
     */
    @Override
    public Either<IfConditionError, Boolean> matches(final IfConditionParam ifConditionParam)
    {
        // Get the email domain we want to check for
        final Option<String> emailDomainOpt = ifConditionParam.getConfiguration().getData().getValue(EMAIL_DOMAIN_KEY);
        if(emailDomainOpt.isEmpty())
        {
            return ifConditionErrorHelper.error("No " + EMAIL_DOMAIN_KEY + " property in config data");
        }
        final String emailDomainToCheckFor = emailDomainOpt.get();

        // Get the email domain of the user that initiated the rule
        final Either<AnError, ApplicationUser> userEither = userMessageHelper.getUser(ifConditionParam.getMessage(), UserMessageHelper.CURRENT_USER_USER_PREFIX);
        if (userEither.isLeft())
        {
            return ifConditionErrorHelper.error(userEither.left().get());
        }
        final ApplicationUser userToCheck = userEither.right().get();
        final String userEmailDomain = getEmailDomain(userToCheck);

        // Return the match result
        return right(userEmailDomain.equalsIgnoreCase(emailDomainToCheckFor));
    }

    private String getEmailDomain(final ApplicationUser fromUser)
    {
        return fromUser.getEmailAddress().substring(
                fromUser.getEmailAddress().indexOf('@')
        );
    }
}
