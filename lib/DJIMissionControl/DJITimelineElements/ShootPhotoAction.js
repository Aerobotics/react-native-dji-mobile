// @flow strict

import CustomTimelineElement from './CustomTimelineElement';

class ShootPhotoAction extends CustomTimelineElement {
  count: number;
  interval: number;
  wait: boolean;
  stopShoot: boolean;

  constructor(
    parameters?: {|
      count?: number,
      interval?: number,
      wait?: boolean,
      stopShoot?: boolean,
    |}
  ) {
    super('ShootPhotoAction');

    if (parameters) {
      const {
        count,
        interval,
        wait,
        stopShoot,
      } = parameters;

      this.count = count;
      this.interval = interval;
      this.wait = wait;
      this.stopShoot = stopShoot;

      this.checkValidity();
    }

  }

  getElementParameters() {
    const {
      count,
      interval,
      wait,
      stopShoot,
    } = this;

    return {
      count,
      interval,
      wait,
      stopShoot,
    };

    // if (stopShoot != null) {

    // } else  if (count != null && interval != null && wait != null) {
    //   return {
    //     count,
    //     interval,
    //     wait,
    //   };

    // } else if (stopShoot != null) {
    //   return {
    //     stopShoot: true,
    //   };

    // } else {
    //   return {
    //     singleImage: true,
    //   };
    // }
  }

  checkValidity() {
    const {
      count,
      interval,
      wait,
      stopShoot,
    } = this;

    if (count != null && interval != null && wait != null) {
      if (typeof count != 'number' || count <= 0) {
        throw Error('Invalid photo count: Ensure count is a positive number');
      } else if (typeof interval != 'number' || interval < 1) { // TODO: (Adam) should we limit the interval time to 1 second?
        throw Error('Invalid photo count: Ensure count is a positive number greater than 1');
      } else if (stopShoot != null) {
        throw Error('stopShoot must be added without any parameters');
      }
    }
  }

}

export default ShootPhotoAction;
