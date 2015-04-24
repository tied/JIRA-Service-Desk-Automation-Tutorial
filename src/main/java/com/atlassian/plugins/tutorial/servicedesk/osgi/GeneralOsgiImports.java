package com.atlassian.plugins.tutorial.servicedesk.osgi;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

/**
 * This class is used to replace <component-import /> declarations in the atlassian-plugin.xml.
 * This class will be scanned by the atlassian spring scanner at compile time.
 * There is no situations where you ever need to create this class, it's here purely so that all the component-imports
 * are in the one place and not scattered throughout the code.
 */
@SuppressWarnings("UnusedDeclaration")
@Scanned
public class GeneralOsgiImports
{
    /******************************
    // Automation Engine
    ******************************/
    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.whenhandler.WhenHandlerProjectContextService whenHandlerProjectContextService;
    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.whenhandler.WhenHandlerRunInContextService whenHandlerRunInContextService;

    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.IssueMessageHelper issueMessageHelper;
    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.CommentMessageHelper commentMessageHelper;
    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.UserMessageHelper userMessageHelper;
    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.message.RuleMessageBuilderService ruleMessageBuilderService;

    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.execution.command.RuleExecutionCommandBuilderService ruleExecutionCommandBuilderService;

    @ComponentImport com.atlassian.servicedesk.plugins.automation.api.configuration.ruleset.input.BuilderService builderService;

    /******************************
    // JIRA
    ******************************/
    @ComponentImport com.atlassian.jira.issue.IssueManager issueManager;
    @ComponentImport com.atlassian.jira.security.PermissionManager permissionManager;
    @ComponentImport com.atlassian.jira.user.util.UserManager userManager;

    private GeneralOsgiImports()
    {
        throw new Error("This class should not be instantiated");
    }
}
