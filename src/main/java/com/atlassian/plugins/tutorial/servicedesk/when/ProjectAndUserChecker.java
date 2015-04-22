package com.atlassian.plugins.tutorial.servicedesk.when;

import com.atlassian.fugue.Either;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.pocketknife.api.commons.error.AnError;
import com.atlassian.servicedesk.plugins.automation.api.execution.context.project.ProjectContext;
import com.atlassian.servicedesk.plugins.automation.api.execution.context.user.InContextFunction;
import com.atlassian.servicedesk.plugins.automation.api.execution.whenhandler.WhenHandlerContext;
import com.atlassian.servicedesk.plugins.automation.api.execution.whenhandler.WhenHandlerProjectContextService;
import com.atlassian.servicedesk.plugins.automation.api.execution.whenhandler.WhenHandlerRunInContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class ProjectAndUserChecker
{
    private static final Logger LOG = LoggerFactory.getLogger(ProjectAndUserChecker.class);

    @Autowired
    private WhenHandlerProjectContextService whenHandlerProjectContextService;
    @Autowired
    private WhenHandlerRunInContextService whenHandlerRunInContextService;
    @Autowired
    private PermissionManager permissionManager;

    /**
     * Checks whether a when handler context matches a given project.
     */
    public boolean isApplicableProject(@Nonnull WhenHandlerContext context,
                                       @Nonnull Project project)
    {
        final Either<AnError, ProjectContext> applicationProjectContext = whenHandlerProjectContextService.getApplicationProjectContext(context);
        if (applicationProjectContext.isLeft())
        {
            LOG.debug("Unable to fetch project context for given when handler context: " + context.toString());
            return false;
        }

        final List<Project> projects = applicationProjectContext.right().get().getProjects();
        if (projects.isEmpty())
        {
            return true;
        }
        else
        {
            return projects.contains(project);
        }
    }


    /**
     * Can the passed user browse the given issue?
     */
    public boolean canBrowseIssue(@Nonnull WhenHandlerContext context,
                                  @Nonnull final Issue issue)
    {
        return whenHandlerRunInContextService.executeInContext(context, new InContextFunction<Boolean>()
        {
            @Override
            public Boolean run(final ApplicationUser user)
            {
                return permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, user);
            }
        });
    }
}
