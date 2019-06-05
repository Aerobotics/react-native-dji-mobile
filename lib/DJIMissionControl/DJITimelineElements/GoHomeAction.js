// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class GoHomeAction extends CustomTimelineElement {
  constructor() {
    super('GoHomeAction');
  }

  getElementParameters() {
    return {};
  }

  checkValidity() {
  }
}

export default GoHomeAction;