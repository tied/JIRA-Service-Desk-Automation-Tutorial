define("servicedesk/settings/automation/tutorial/modules/ruleif/useremaildomain-if-condition-model", [
    "servicedesk/backbone-brace"
], function (
        Brace
) {

    return Brace.Model.extend({
        namedAttributes: {
            emailDomain: String
        },
        defaults: {
            emailDomain: ""
        }
    });
});