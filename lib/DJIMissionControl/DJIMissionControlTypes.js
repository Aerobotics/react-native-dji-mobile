// @flow strict

// TODO: (Adam)
export type CreateWaypointMissionParameters = {
  autoFlightSpeed: number,
  maxFlightSpeed: number,
}

export type Waypoint = {
  longitude: number,
  latitude: number,
  altitude: number,
  cornerRadiusInMeters?: number,
}

export type Hotpoint = {
  longitude: number,
  latitude: number,
  altitude: number,
}
