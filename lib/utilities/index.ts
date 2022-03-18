// @flow strict

import {
  Subject,
} from 'rxjs';

import PlatformEventEmitter from './platformEventEmitter';
import { DJIEvent } from '../../types';

const DJIEventSubject: Subject<DJIEvent> = new Subject();

PlatformEventEmitter.addListener('DJIEvent', evt => {
  DJIEventSubject.next(evt);
});

export {
  DJIEventSubject,
};
