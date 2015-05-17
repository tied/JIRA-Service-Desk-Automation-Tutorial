define("servicedesk/settings/automation/tutorial/modules/rulethen/issue-label-then-action-form", [
    "servicedesk/jQuery",
    "servicedesk/underscore",
    "servicedesk/settings/automation/tutorial/modules/rulethen/issue-label-then-action-model",
    "servicedesk/settings/automation/tutorial/modules/rulethen/issue-label-then-action-view"
], function (
        $,
        _,
        IssueLabelModel,
        IssueLabelView
) {

    var issueLabelView = function(controller) {
        var template = ServiceDesk.Templates.Agent.Settings.Automation.Tutorial.Modules.RuleThen.serviceDeskIssueLabelThenActionContainer;
        var $el = $(controller.el);

        function onError(errors) {
            $el.find('.error').remove();
            _applyFieldErrors(errors.fieldErrors);
            _applyGlobalErrors(errors.globalErrors);
        }

        function onDestroy() {
            controller.off('destroy');
            controller.off('error');
        }

        function _applyFieldErrors(errors) {
            // If errors is an array
            _.each(errors, controller.renderFieldError)
        }

        function _applyGlobalErrors(errors) {
            for (var i = 0; i < errors.length; i++) {
                var thisError = errors[i];
                controller.renderGlobalError(thisError)
            }
        }

        controller.on('destroy', onDestroy.bind(this));
        controller.on('error', onError.bind(this));

        return {
            render: function(config, errors) {
                var issueLabel = config && config.issueLabel ? config.issueLabel : "";

                // Render the template
                $el.html(template());

                this.issueLabelView = new IssueLabelView({
                    model: new IssueLabelModel({
                        issueLabel: issueLabel
                    }),
                    el: $el.find(".automation-servicedesk-issue-label-then-action-container")
                }).render();

                if (errors) {
                    if (errors.fieldErrors) {
                        _applyFieldErrors(errors.fieldErrors);
                    }

                    if (errors.globalErrors) {
                        _applyGlobalErrors(errors.globalErrors);
                    }
                }

                return this;
            },

            serialize: function () {
                return {
                    issueLabel: $el.find('input').val()
                }
            },

            validate: function (deferred) {
                $el.find('.error').remove();
                var hasError = false;
                var issueLabelField = $el.find('input');
                var fieldErrors = {};

                if (!issueLabelField.val()) {
                    fieldErrors[issueLabelField.attr('name')] = AJS.I18n.getText('tutorial.then.action.issue.label.error.missing');
                    hasError = true;
                }

                if (hasError) {
                    _applyFieldErrors(fieldErrors);
                    deferred.reject();
                }
                else {
                    deferred.resolve();
                }
            },

            dispose: function() {
                if (this.issueLabelView) {
                    this.issueLabelView.dispose && this.issueLabelView.dispose();
                }
            }
        }
    };

    return function(controller) {
        return issueLabelView(controller);
    };
});