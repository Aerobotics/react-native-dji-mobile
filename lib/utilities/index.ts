// @flow strict

import {
  Observable,
  Subject,
} from 'rxjs';

import PlatformEventEmitter from './platformEventEmitter';
import { DJIEvent } from '../../types';
import { filter as $filter, map as $map } from 'rxjs/operators';

const DJIEventSubject: Subject<DJIEvent> = new Subject();

PlatformEventEmitter.addListener('DJIEvent', evt => {
  DJIEventSubject.next(evt);
});

const observeEvent = <T>(eventName: string): Observable<T> => {
  return DJIEventSubject.pipe($filter(evt =>  evt.type === eventName), $map(evt => evt.value));
}

export {
  DJIEventSubject,
  observeEvent,
};
