// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class RecordFlightDataElement extends CustomTimelineElement {
  parameters: {|
    stopRecordFlightData?: boolean,
    fileName?: string,
  |} = {};

  constructor(
    parameters?: {|
      stopRecordFlightData?: boolean,
      fileName: string,
    |}
  ) {
    super('RecordFlightData');

    if (parameters) {
      this.parameters = parameters;
      this.checkValidity();
    }

  }

  getElementParameters() {
    if (this.parameters.stopRecordFlightData !== true && this.parameters.fileName === undefined) {
      throw new Error('Please specify a log file name using the "fileName" parameter!');
    }
    return this.parameters;
  }

  checkValidity() {
    return;
  }
}

export default RecordFlightDataElement;
