package com.atlassian.plugins.tutorial.servicedesk.ruleif;

import com.atlassian.fugue.Option;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.servicedesk.plugins.automation.api.configuration.ruleset.validation.ValidationResult;
import com.atlassian.servicedesk.plugins.automation.spi.ruleif.IfConditionValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Responsible for checking that the email domain entered by the user is present and valid.
 *
 */
public final class UserEmailDomainIfConditionValidator implements IfConditionValidator
{
    private final static String EMAIL_DOMAIN_FIELD_NAME = "emailDomain";

    private final I18nHelper.BeanFactory i18nFactory;

    @Autowired
    public UserEmailDomainIfConditionValidator(final I18nHelper.BeanFactory i18nFactory)
    {
        this.i18nFactory = i18nFactory;
    }

    /**
     * This method is invoked whenever a rule that contains a user email domain if condition is saved, or loaded. If
     * a FAILED result is returned, the error message contained in the result will be displayed to the user, and
     * any save operation will be blocked.
     */
    @Override
    public ValidationResult validate(final IfConditionValidationParam ifConditionValidationParam)
    {
        final Option<String> configuredEmailDomainOpt =
                ifConditionValidationParam.getConfiguration().getData().getValue(UserEmailDomainIfCondition.EMAIL_DOMAIN_KEY);

        final ApplicationUser userToValidateWith = ifConditionValidationParam.getUserToValidateWith();

        if(configuredEmailDomainOpt.isEmpty() || isBlank(configuredEmailDomainOpt.get()))
        {
            return createResultWithFieldError(
                    userToValidateWith,
                    "tutorial.if.condition.user.email.domain.error.missing");
        }

        if(configuredEmailDomainOpt.get().contains("@"))
        {
            return createResultWithFieldError(
                    userToValidateWith,
                    "tutorial.if.condition.user.email.domain.error.invalid");
        }

        return ValidationResult.PASSED();
    }

    private ValidationResult createResultWithFieldError(@Nonnull ApplicationUser user, @Nonnull String errorI18nKey)
    {
        final I18nHelper i18nHelper = i18nFactory.getInstance(user);

        Map<String, List<String>> errorList = newHashMap();
        errorList.put(EMAIL_DOMAIN_FIELD_NAME, newArrayList(i18nHelper.getText(errorI18nKey)));

        return ValidationResult.FAILED(errorList);
    }
}
