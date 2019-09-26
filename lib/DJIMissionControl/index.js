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
  type Observer,
} from 'rxjs';

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
  'UNKNOWN': {
    name: 'DJIMissionControlTimelineEventUnknown',
    shortName: 'Unknown',
    description: 'Unknown event type. This is a default value if no other event is matching.',
  },
  [0]: {
    name: 'DJIMissionControlTimelineEventStarted',
    shortName: 'Started',
    description: 'Timeline successfully started.',
  },
  [1]: {
    name : 'DJIMissionControlTimelineEventStartError',
    shortName: 'StartError',
    description: 'Timeline failed to start.',
  },
  [2]: {
    name : 'DJIMissionControlTimelineEventProgressed',
    shortName: 'Progressed',
    description: 'Timeline element progressed.',
  },
  [3]: {
    name : 'DJIMissionControlTimelineEventPaused',
    shortName: 'Paused',
    description: 'Timeline successfully paused.',
  },
  [4]: {
    name : 'DJIMissionControlTimelineEventPauseError',
    shortName: 'PauseError',
    description: 'Timeline failed to be paused.',
  },
  [5]: {
    name : 'DJIMissionControlTimelineEventResumed',
    shortName: 'Resumed',
    description: 'Timeline successfully resumed.',
  },
  [6]: {
    name : 'DJIMissionControlTimelineEventResumeError',
    shortName: 'ResumeError',
    description: 'Timeline failed to resume.',
  },
  [7]: {
    name : 'DJIMissionControlTimelineEventStopped',
    shortName: 'Stopped',
    description: 'Timeline Stopped successfully.',
  },
  [8]: {
    name : 'DJIMissionControlTimelineEventStopError',
    shortName: 'StopError',
    description: 'Timeline failed to stop and is still continuing in its previous state.',
  },
  [9]: {
    name : 'DJIMissionControlTimelineEventFinished',
    shortName: 'Finished',
    description: 'Timeline completed its execution normally.',
  },
});

let callbackFuncIdIncrementor = 0;
export const getCallbackFuncId = () => {
  return callbackFuncIdIncrementor++;
};

DJIEventSubject.subscribe(evt => {
  if (evt.type === 'RunJSElementEvent') {
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

  pauseTimeline: async () => {
    await DJIMissionControlWrapper.pauseTimeline();
  },

  resumeTimeline: async () => {
    await DJIMissionControlWrapper.resumeTimeline();
  },

  stopTimeline: async () => {
    let timelineObserver: Observer;

    try {
      await new Promise(async (resolve, reject) => {

        const timeoutId = setTimeout(() => { // Fail after a set timeout period
          reject();
        }, 5000);

        timelineObserver = (await DJIMissionControl.startTimelineListener()).subscribe(evt => {
          if (evt.value.timelineIndex === -1) { // General timeline event
            if (evt.value.eventType === 'Stopped') { // Finished event
              clearTimeout(timeoutId);
              resolve();
            }
          }
        });
        DJIMissionControlWrapper.stopTimeline();

      });
      timelineObserver ? timelineObserver.unsubscribe() : null;
      return;

    } catch (err) {
      timelineObserver ? timelineObserver.unsubscribe() : null;
      throw Error('Failed to stop timeline');
    }
  },

  setCurrentTimelineMarker: async (currentTimelineMarker: number) => {
    if (!Number.isInteger(currentTimelineMarker) || currentTimelineMarker < 0) {
      throw Error('currentTimelineMarker must be a positive integer');
    } else {
      await DJIMissionControlWrapper.setCurrentTimelineMarker(currentTimelineMarker);
    }
  },

  startGoHome: async () => {
    await DJIMissionControlWrapper.startGoHome();
  },

  startTimelineListener: async () => {
    await DJIMissionControlWrapper.startTimelineListener();
    // TODO: (Adam) need to change eventType (int value) to a readable string value
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'missionControlEvent'),
      $map(evt => {
        // NOTE: DO NOT mutate the evt object, the pipe runs for each observable, and if the evt object is mutated for the first observable,
        // the next observable's pipe gets the mutated object, resulting in broken outputs!
        const updatedEvent = {
          value: {
            ...evt.value,
          },
        };
        const eventType = eventTypes[updatedEvent.value.eventType];
        if (eventType == null) {
          updatedEvent.value.eventType = eventTypes.UNKNOWN.shortName;
        } else {
          updatedEvent.value.eventType = eventType.shortName;
        }
        return updatedEvent;
      })
    ).asObservable();
  },

  // TODO: (Adam) should this be stopAllTimelineListeners ?
  stopTimelineListener: async () => {
    await DJIMissionControlWrapper.stopTimelineListener();
  },
};

export default DJIMissionControl;
