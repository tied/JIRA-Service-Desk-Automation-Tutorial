package com.atlassian.plugins.tutorial.servicedesk.rulethen;

import com.atlassian.fugue.Option;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.tutorial.servicedesk.ruleif.UserEmailDomainIfCondition;
import com.atlassian.servicedesk.plugins.automation.api.configuration.ruleset.validation.ValidationResult;
import com.atlassian.servicedesk.plugins.automation.spi.rulethen.ThenActionValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Responsible for checking that the label entered by the user is a valid label.
 *
 */
public final class IssueLabelThenActionValidator implements ThenActionValidator
{
    private final static String ISSUE_LABEL_FIELD_NAME = "issueLabel";

    private final I18nHelper.BeanFactory i18nFactory;

    @Autowired
    public IssueLabelThenActionValidator(final I18nHelper.BeanFactory i18nFactory)
    {
        this.i18nFactory = i18nFactory;
    }

    /**
     * This method is invoked whenever a rule that contains an issue label then action is saved, or loaded. If
     * a FAILED result is returned, the error message contained in the result will be displayed to the user, and
     * any save operation will be blocked.
     */
    @Override
    public ValidationResult validate(final ThenActionValidationParam thenActionValidationParam)
    {
        final Option<String> configuredLabel =
                thenActionValidationParam.getConfiguration().getData().getValue(IssueLabelThenAction.ISSUE_LABEL_KEY);

        final ApplicationUser userToValidateWith = thenActionValidationParam.getUserToValidateWith();

        // For tutorial purposes, we just check the label is not blank
        if(configuredLabel.isEmpty() || isBlank(configuredLabel.get()))
        {
            return createResultWithFieldError(
                    userToValidateWith,
                    "tutorial.then.action.issue.label.error.missing");
        }

        return ValidationResult.PASSED();
    }

    private ValidationResult createResultWithFieldError(@Nonnull ApplicationUser user, @Nonnull String errorI18nKey)
    {
        final I18nHelper i18nHelper = i18nFactory.getInstance(user);

        Map<String, List<String>> errorList = newHashMap();
        errorList.put(ISSUE_LABEL_FIELD_NAME, newArrayList(i18nHelper.getText(errorI18nKey)));

        return ValidationResult.FAILED(errorList);
    }
}
