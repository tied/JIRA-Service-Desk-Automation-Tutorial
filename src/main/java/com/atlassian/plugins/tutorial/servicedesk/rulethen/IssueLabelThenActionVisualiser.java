package com.atlassian.plugins.tutorial.servicedesk.rulethen;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.tutorial.servicedesk.ruleif.UserEmailDomainIfCondition;
import com.atlassian.servicedesk.plugins.automation.spi.visualiser.RuleComponentVisualiser;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

/**
 * This visualiser is responsible for deciding what name and label to show for issue label then action rule components.
 * The name never changes, but the label displayed will show the label, if this has been configured.
 *
 */
public final class IssueLabelThenActionVisualiser implements RuleComponentVisualiser
{
    private final I18nHelper i18nHelper;

    @Autowired
    public IssueLabelThenActionVisualiser(final I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
    }

    /**
     * Returns the name to use for this then action rule component. The name appears above the label, adjacent to the
     * rule component icon.
     */
    @Nonnull
    @Override
    public String getName(final RuleComponentVisualiserParam ruleComponentVisualiserParam)
    {
        return i18nHelper.getText("tutorial.then.action.issue.label.name");
    }

    /**
     * Returns the label to use for this then action rule component. The label appears below the name, and should
     * show at a glance the value of the configuration for this rule component. In our case, it will show the label
     * that has been configured by the user.
     *
     * If the label has not been configured, this will return {@code Option.none()}, which means no label is
     * displayed.
     *
     */
    @Nonnull
    @Override
    public Option<String> getLabel(@Nonnull final RuleComponentVisualiserParam ruleComponentVisualiserParam)
    {
        final Option<String> configuredLabelOpt =
                ruleComponentVisualiserParam.ruleConfiguration().getValue(IssueLabelThenAction.ISSUE_LABEL_KEY);

        if(configuredLabelOpt.isDefined())
        {
            return some("\"" + configuredLabelOpt.get() + "\"");
        }
        else
        {
            return none(String.class);
        }
    }
}
