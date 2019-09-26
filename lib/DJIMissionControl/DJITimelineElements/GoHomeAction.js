// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

type Parameters = {|
  autoConfirmLandingEnabled?: boolean,
|}

class GoHomeAction extends CustomTimelineElement {
  autoConfirmLandingEnabled: ?boolean;
  constructor(parameters: ?Parameters) {
    super('GoHomeAction');

    if (parameters != null) {
      const {
        autoConfirmLandingEnabled,
      } = parameters;
      this.autoConfirmLandingEnabled = autoConfirmLandingEnabled;
    }
  }

  getElementParameters() {
    const {
      autoConfirmLandingEnabled,
    } = this;

    return {
      autoConfirmLandingEnabled,
    };
  }

  checkValidity() {
  }
}

export default GoHomeAction;
