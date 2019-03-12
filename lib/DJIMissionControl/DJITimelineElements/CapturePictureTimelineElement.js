// @flow strict

import {
  NativeModules,
} from 'react-native';

import {
  filter,
} from 'rxjs/operators';

import {
  CustomTimelineElement,
} from './CustomTimelineElement';

export class CapturePictureTimelineElement extends CustomTimelineElement {
  _gimbalAngle = 0;
  _capturedImageFilenames: String[] = [];

  constructor() {
    super('CapturePictureTimelineElement');
  }
}
