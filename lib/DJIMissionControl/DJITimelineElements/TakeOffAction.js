// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class TakeOffAction extends CustomTimelineElement {
  constructor() {
    super('TakeOffAction');
  }

  getElementParameters() {
    return {};
  }

  checkValidity() {
  }
}

export default TakeOffAction;
