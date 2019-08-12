// @flow strict

import {
  Subject,
} from 'rxjs';

import PlatformEventEmitter from './platformEventEmitter';

const DJIEventSubject = new Subject();

PlatformEventEmitter.addListener('DJIEvent', evt => {
  DJIEventSubject.next(evt);
});

export {
  DJIEventSubject,
};
