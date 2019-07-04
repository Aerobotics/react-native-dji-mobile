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
  filter as $filter,
  map as $map,
} from 'rxjs/operators';

import CustomTimelineElement from './DJITimelineElements/CustomTimelineElement';

import TakeOffAction from './DJITimelineElements/TakeOffAction';

import GoToAction from './DJITimelineElements/GoToAction';

import GoHomeAction from './DJITimelineElements/GoHomeAction';

import GimbalAttitudeAction from './DJITimelineElements/GimbalAttitudeAction';

import RecordVideoAction from './DJITimelineElements/RecordVideoAction';

import ShootPhotoAction from './DJITimelineElements/ShootPhotoAction';

import WaypointMissionTimelineElement from './DJITimelineElements/WaypointMissionTimelineElement';

import VirtualStickTimelineElement from './DJITimelineElements/VirtualStickTimelineElement';

import RecordFlightDataElement from './DJITimelineElements/RecordFlightDataElement';

import RunJSElement from './DJITimelineElements/RunJSElement';

import HotpointAction from './DJITimelineElements/HotpointAction';

import AircraftYawAction from './DJITimelineElements/AircraftYawAction';


const {
  DJIMissionControlWrapper,
} = NativeModules;

const eventTypes = Object.freeze({
  [-1]: {
    name: 'DJIMissionControlTimelineEventUnknown',
    description: 'Unknown event type. This is a default value if no other event is matching.',
  },
  [0]: {
    name: 'DJIMissionControlTimelineEventStarted',
    description: 'Timeline successfully started.',
  },
  [1]: {
    name : 'DJIMissionControlTimelineEventStartError',
    description: 'Timeline failed to start.',
  },
  [2]: {
    name : 'DJIMissionControlTimelineEventProgressed',
    description: 'Timeline element progressed.',
  },
  [3]: {
    name : 'DJIMissionControlTimelineEventPaused',
    description: 'Timeline successfully paused.',
  },
  [4]: {
    name : 'DJIMissionControlTimelineEventPauseError',
    description: 'Timeline failed to be paused.',
  },
  [5]: {
    name : 'DJIMissionControlTimelineEventResumed',
    description: 'Timeline successfully resumed.',
  },
  [6]: {
    name : 'DJIMissionControlTimelineEventResumeError',
    description: 'Timeline failed to resume.',
  },
  [7]: {
    name : 'DJIMissionControlTimelineEventStopped',
    description: 'Timeline Stopped successfully.',
  },
  [8]: {
    name : 'DJIMissionControlTimelineEventStopError',
    description: 'Timeline failed to stop and is still continuing in its previous state.',
  },
  [9]: {
    name : 'DJIMissionControlTimelineEventFinished',
    description: 'Timeline completed its execution normally.',
  },
});

let callbackFuncIdIncrementor = 0;
export const getCallbackFuncId = () => {
  return callbackFuncIdIncrementor++;
};

DJIEventSubject.subscribe(evt => {
  if (evt.type === 'RunJSElementEvent') {
    console.log(evt);
    const {
      callbackFuncId,
    } = evt.value;

    const callbackFunc = DJIMissionControl._scheduledJSFunctions[callbackFuncId];
    if (callbackFunc) {
      callbackFunc();
      delete DJIMissionControl._scheduledJSFunctions[callbackFuncId];
    }
  }
});

const DJIMissionControl = {
  TakeOffAction,
  GoToAction,
  GoHomeAction,
  GimbalAttitudeAction,
  RecordVideoAction,
  ShootPhotoAction,
  HotpointAction,
  AircraftYawAction,
  WaypointMissionTimelineElement,
  VirtualStickTimelineElement,
  RecordFlightDataElement,
  RunJSElement,

  _scheduledJSFunctions: [],

  scheduleElement: async (element: CustomTimelineElement) => {
    const {
      elementName,
    } = element;
    const elementParameters = element.getElementParameters();

    if (elementName && elementParameters) {

      // This will return a rejected promise if the element is invalid, causing this function to return a rejected promise
      await element.checkValidity();

      try {
        await DJIMissionControlWrapper.scheduleElement(elementName, elementParameters);
      } catch (err) {
        throw Error(err);
      }

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
    // TODO: Wait until the timeline has stopped before returning
  },

  startGoHome: async () => {
    await DJIMissionControlWrapper.startGoHome();
  },

  startTimelineListener: async () => {
    await DJIMissionControlWrapper.startTimelineListener();
    // TODO: (Adam) need to change eventType (int value) to a readable string value
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'missionControlEvent'),
      // $map(evt => {
      //   evt.test = eventTypes[evt.value.eventType].name;
      //   return evt;
      // })
    ).asObservable();
  },

  // TODO: (Adam) should this be stopAllTimelineListeners ?
  stopTimelineListener: async () => {
    await DJIMissionControlWrapper.stopTimelineListener();
  },
};

export default DJIMissionControl;
