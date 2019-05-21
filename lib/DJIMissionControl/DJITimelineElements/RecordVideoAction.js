// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class RecordVideoAction extends CustomTimelineElement {
  duration: ?number;
  stopRecord: ?boolean;

  constructor(
    parameters?: {|
      duration?: number,
      stopRecord?: boolean,
    |}
  ) {
    super('RecordVideoAction');

    if (parameters) {
      const {
        duration,
        stopRecord,
      } = parameters;

      this.duration = duration;
      this.stopRecord = stopRecord;

      this.checkValidity();
    }

  }

  getElementParameters() {
    const {
      duration,
      stopRecord,
    } = this;

    return {
      duration,
      stopRecord,
    };
  }

  checkValidity() {
    // TODO:
  }

}

export default RecordVideoAction;
