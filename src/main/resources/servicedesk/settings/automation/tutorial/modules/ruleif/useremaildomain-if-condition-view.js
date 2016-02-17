
define("servicedesk/settings/automation/tutorial/modules/ruleif/useremaildomain-if-condition-view", [
    "servicedesk/jQuery",
    "servicedesk/underscore",
    "servicedesk/backbone-brace",
    "servicedesk/shared/mixin/form/form-mixin"
], function (
        $,
        _,
        Brace,
        FormMixin
) {
    return Brace.View.extend({
        template: ServiceDesk.Templates.Agent.Settings.Automation.Tutorial.Modules.RuleIf.drawUserEmailDomainForm,
        mixins: [FormMixin],

        dispose: function() {
            this.undelegateEvents();
            this.stopListening();
        },

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        }
    });
});