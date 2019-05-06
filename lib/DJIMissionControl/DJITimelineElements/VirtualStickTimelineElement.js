// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class VirtualStickTimelineElement extends CustomTimelineElement {

  constructor() {
    super('VirtualStickTimelineElement');
  }

  getElementParameters() {
    return {};
  }

  checkValidity() {
    return;
  }

}

export default VirtualStickTimelineElement;
