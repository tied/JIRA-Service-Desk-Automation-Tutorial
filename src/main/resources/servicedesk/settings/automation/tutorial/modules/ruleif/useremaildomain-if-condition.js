define("servicedesk/settings/automation/tutorial/modules/ruleif/useremaildomain-if-condition-form", [
    "servicedesk/jQuery",
    "servicedesk/underscore",
    "servicedesk/settings/automation/tutorial/modules/ruleif/useremaildomain-if-condition-model",
    "servicedesk/settings/automation/tutorial/modules/ruleif/useremaildomain-if-condition-view"
], function (
        $,
        _,
        UserEmailDomainModel,
        UserEmailDomainView
) {

    var userEmailDomainView = function(controller) {
        var template = ServiceDesk.Templates.Agent.Settings.Automation.Tutorial.Modules.RuleIf.serviceDeskUserEmailDomainIfConditionContainer;
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
                var emailDomain = config && config.emailDomain ? config.emailDomain : "";

                // Render the template
                $el.html(template());

                this.emailDomainView = new UserEmailDomainView({
                    model: new UserEmailDomainModel({
                        emailDomain: emailDomain
                    }),
                    el: $el.find(".automation-servicedesk-email-domain-if-condition-container")
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
                    emailDomain: $el.find('input').val()
                }
            },

            validate: function (deferred) {
                $el.find('.error').remove();
                var hasError = false;
                var emailDomainField = $el.find('input');
                var fieldErrors = {};

                if (!emailDomainField.val()) {
                    fieldErrors[emailDomainField.attr('name')] = AJS.I18n.getText('tutorial.if.condition.user.email.domain.error.missing');
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
                if (this.emailDomainView) {
                    this.emailDomainView.dispose && this.emailDomainView.dispose();
                }
            }
        }
    };

    return function(controller) {
        return userEmailDomainView(controller);
    };
});