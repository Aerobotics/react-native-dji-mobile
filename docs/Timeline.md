# Timeline


# Timeline Actions

## GimbalAttitudeAction
```javascript
  const gimbalAtittudeAction = new DJIMissionControl.GimbalAttitudeAction({
    pitch: -90,
    roll: 0,
    yaw: 0,
    completionTime: 5,
  });
```

**Note:** Roll and yaw value are currently ignored as the gimbal mode cannot be changed

---

## ShootPhotoAction
```javascript
const shootPhotoAction = new DJIMissionControl.ShootPhotoAction({
  count: 4,
  interval: 2,
  wait: true,
  stopShoot: false,
});
```

**Note:** If count, interval, and wait do not all appear together and stopShoot is excluded, the photo action will either be a single photo, or a stop shoot action.
### Parameters
- count: A number indicating the number of photos to capture
- interval: A number indicating the number of seconds to wait between photo captures
- wait: (iOS Only) A boolean indicating if the action should finish if all the photos are shot (true) or immediately after starting the action (false).
- stopShoot: A boolean indicating whether the action should stop any currently running photo interval actions.

---

## VirtualStickTimelineElement

### Parameters
- endTrigger: [`string`] This can be a string of value `none`, `timer`, or `ultrasonic`.
- timerEndTime: [`number`] When endTrigger is `timer`, this specifies how many seconds the element should run for.
- ultrasonicEndDistance: [`number`] When endTrigger is `ultrasonic`, this specifies how many metres above an obstacle the element should end at.
- ultrasonicDecreaseVerticalThrottleWithDistance: [`boolean`] (**NOT YET IMPLEMENTED**) When endTrigger is `ultrasonic` and this value is `true`, the drone's vertical throttle velocity will decrease as the ultrasonic sensor distance measurement decreases.
- enableObstacleAvoidance [`boolean`]: Optional
