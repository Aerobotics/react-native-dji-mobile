// @flow strict

import {
  NativeModules,
} from 'react-native';

import type {
  CreateWaypointMissionParameters,
  Waypoint,
} from './DJIMissionControlTypes';

import {
  DJIEventSubject,
} from '../utilities';

import {
  filter,
} from 'rxjs/operators';

import {
  CustomTimelineElement,
} from './DJITimelineElements/CustomTimelineElement';

import {
  CapturePictureTimelineElement,
} from './DJITimelineElements/CapturePictureTimelineElement';

import {
  WaypointMissionTimelineElement,
} from './DJITimelineElements/WaypointMissionTimelineElement';

const {
  DJIMissionControlWrapper,
} = NativeModules;

const DJIMissionControl = {
  WaypointMissionTimelineElement,
  CapturePictureTimelineElement,

  scheduleTimelineElement: async (element: CustomTimelineElement) => {
    await DJIMissionControlWrapper.scheduleCustomTimelineElement(element.elementName);
  },

  scheduleElement: async (element: CustomTimelineElement) => {
    const {
      elementName,
    } = element;

    const elementParameters = element.getElementParameters();

    if (elementName && elementParameters) {

      // This will return a rejected promise if the element is invalid, causing this function to return a rejected promise
      await element.checkValidity();

      await DJIMissionControlWrapper.scheduleElement(elementName, elementParameters);

    } else {
      if (!elementName) {
        throw Error(
          'Missing Element Name: Element missing "elementName" parameter. Elements must have unique '
          + 'elementName string.');
      } else if (!elementParameters) {
        throw Error(
          'Missing Element Parameters: getElementParameters returns undefined. Elements must have valid parameters for correct '
          + 'operation');
      }
    }
  },

  unscheduleEverything: async () => {
    await DJIMissionControlWrapper.unscheduleEverything();
  },

  startTimeline: async () => {
    await DJIMissionControlWrapper.startTimeline();
  },

  stopTimeline: async () => {
    await DJIMissionControlWrapper.stopTimeline();
  },

  startTimelineListener: async () => {
    await DJIMissionControlWrapper.startTimelineListener();
    // TODO: (Adam) need to change eventType (int value) to a readable string value
    return DJIEventSubject.pipe(filter(evt => evt.type === 'missionControlEvent')).asObservable();
  },
  stopTimelineListener: async () => {
    await DJIMissionControlWrapper.stopTimelineListener();
  },
};

export default DJIMissionControl;
