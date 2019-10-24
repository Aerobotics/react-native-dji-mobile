// @flow strict

// TODO: (Adam)
export type CreateWaypointMissionParameters = {
  autoFlightSpeed: number,
  maxFlightSpeed: number,
}

export const WaypointActionType = Object.freeze({
  startRecord: 'START_RECORD',
  stopRecord: 'STOP_RECORD',
});

export type Waypoint = {
  longitude: number,
  latitude: number,
  altitude: number,
  actions?: Array<{
    actionType: $Values<typeof WaypointActionType>,
    actionParam: number,
  }>,
}

export type Hotpoint = {
  longitude: number,
  latitude: number,
  altitude: number,
}
