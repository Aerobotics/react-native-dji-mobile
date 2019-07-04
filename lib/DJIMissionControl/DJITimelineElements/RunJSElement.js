// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

import DJIMissionControl, {
  getCallbackFuncId,
} from '..';



class RunJSElement extends CustomTimelineElement {
  callbackfunc: () => void;
  callbackFuncId: ?number;

  constructor(
    parameters: {|
      callbackfunc: () => void,
    |}
  ) {
    super('RunJSElement');

    const callbackFuncId = getCallbackFuncId();
    this.callbackFuncId = callbackFuncId;

    const {
      callbackfunc,
    } = parameters;

    // this.callbackfunc = callbackfunc;

    DJIMissionControl._scheduledJSFunctions[callbackFuncId] = callbackfunc;

    // this.checkValidity();
  }

  getElementParameters() {
    const {
      callbackFuncId,
    } = this;

    return {
      callbackFuncId,
    };
  }

  checkValidity() {
    // TODO:
  }

}

export default RunJSElement;
