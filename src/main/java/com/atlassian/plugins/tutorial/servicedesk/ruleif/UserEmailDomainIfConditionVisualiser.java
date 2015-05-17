package com.atlassian.plugins.tutorial.servicedesk.ruleif;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.servicedesk.plugins.automation.spi.visualiser.RuleComponentVisualiser;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

/**
 * This visualiser is responsible for deciding what name and label to show for user email domain if condition rule
 * components. The name never changes, but the label displayed will show the email domain, if this has been configured.
 *
 */
public final class UserEmailDomainIfConditionVisualiser implements RuleComponentVisualiser
{
    private final I18nHelper i18nHelper;

    @Autowired
    public UserEmailDomainIfConditionVisualiser(final I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
    }

    /**
     * Returns the name to use for this if condition rule component. The name appears above the label, adjacent to the
     * rule component icon.
     */
    @Nonnull
    @Override
    public String getName(final RuleComponentVisualiserParam ruleComponentVisualiserParam)
    {
        return i18nHelper.getText("tutorial.if.condition.user.email.domain.name");
    }

    /**
     * Returns the label to use for this if condition rule component. The label appears below the name, and should
     * show at a glance the value of the configuration for this rule component. In our case, it will show the email
     * domain that has been configured by the user.
     *
     * If the email domain has not been configured, this will return {@code Option.none()}, which means no label is
     * displayed.
     *
     */
    @Nonnull
    @Override
    public Option<String> getLabel(@Nonnull final RuleComponentVisualiserParam ruleComponentVisualiserParam)
    {
        final Option<String> configuredEmailDomainOpt =
                ruleComponentVisualiserParam.ruleConfiguration().getValue(UserEmailDomainIfCondition.EMAIL_DOMAIN_KEY);


        if(configuredEmailDomainOpt.isDefined())
        {
            // displays: is "domain.com"
            return some(i18nHelper.getText("tutorial.if.condition.user.email.domain.is") +
                    " \"" +
                    configuredEmailDomainOpt.get() + "\"");
        }
        else
        {
            return none(String.class);
        }
    }
}
