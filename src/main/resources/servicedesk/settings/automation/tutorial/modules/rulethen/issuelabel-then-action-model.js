define("servicedesk/settings/automation/tutorial/modules/rulethen/issue-label-then-action-model", [
    "servicedesk/backbone-brace"
], function (
        Brace
) {

    return Brace.Model.extend({
        namedAttributes: {
            issueLabel: String
        },
        defaults: {
            issueLabel: ""
        }
    });
});