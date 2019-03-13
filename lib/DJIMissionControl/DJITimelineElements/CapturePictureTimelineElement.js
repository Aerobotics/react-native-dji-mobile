// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class CapturePictureTimelineElement extends CustomTimelineElement {
  _gimbalAngle = 0;
  _capturedImageFilenames: String[] = [];

  constructor() {
    super('CapturePictureTimelineElement');
  }
}

export default CapturePictureTimelineElement;
