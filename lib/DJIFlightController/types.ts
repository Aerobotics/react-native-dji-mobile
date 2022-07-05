import { LocationCoordinate3D } from '../../types';

export type RemoteControllerFlightModes =
  | 'P'
  | 'A'
  | 'S'
  | 'G'
  | 'M'
  | 'F'
  | 'T'
  | 'UNKNOWN';

export type WaypointMissionHeadingMode =
  | 'AUTO'
  | 'USING_INITIAL_DIRECTION'
  | 'CONTROL_BY_REMOTE_CONTROLLER'
  | 'USING_WAYPOINT_HEADING'
  | 'TOWARD_POINT_OF_INTEREST';

export type WaypointMissionGotoWaypointMode = 'SAFELY' | 'POINT_TO_POINT';

export type WaypointMissionFlightPathMode = 'NORMAL' | 'CURVED';

export type WaypointMissionFinishedAction =
  | 'NO_ACTION'
  | 'GO_HOME'
  | 'AUTO_LAND'
  | 'GO_FIRST_WAYPOINT'
  | 'CONTINUE_UNTIL_END';

export interface Waypoint extends LocationCoordinate3D {
  heading?: number;
  speed?: number;
  cornerRadiusInMeters?: number;
  actions?: [
    {
      actionType: string;
      actionParam: number;
    },
  ];
  shootPhotoDistanceInterval?: number;
  shootPhotoTimeInterval?: number;
}

export interface WaypointMissionParameters {
  autoFlightSpeed: number;
  maxFlightSpeed: number;
  waypoints: Waypoint[];
  headingMode?: WaypointMissionHeadingMode;
  heading?: number;
  goToWaypointMode?: WaypointMissionGotoWaypointMode;
  flightPathMode?: WaypointMissionFlightPathMode;
  finishedAction?: WaypointMissionFinishedAction;
}

// TODO: make the actions interface more generic
export interface WaypointV2 extends LocationCoordinate3D {
  cornerRadiusInMeters?: number;
  shootPhotoDistanceInterval?: number;
  headingMode?: WaypointMissionV2HeadingMode;
}

export type WaypointMissionV2HeadingMode =
  | 'AUTO'
  | 'FIXED'
  | 'MANUAL'
  | 'WAYPOINT_CUSTOM'
  | 'TOWARD_POINT_OF_INTEREST'
  | 'GIMBAL_YAW_FOLLOW'
  | 'UNKNOWN';

export type WaypointMissionV2FlightPathMode =
  | 'CURVATURE_CONTINUOUS_PASSED'
  | 'GOTO_POINT_CURVE_AND_STOP'
  | 'GOTO_POINT_STRAIGHT_LINE_AND_STOP'
  | 'COORDINATE_TURN'
  | 'GOTO_FIRST_POINT_ALONG_STRAIGHT_LINE'
  | 'STRAIGHT_OUT'
  | 'UNKNOWN';

export interface WaypointMissionV2Parameters {
  autoFlightSpeed: number;
  maxFlightSpeed: number;
  waypoints: WaypointV2[];
  heading?: number; // not implemented yet
  goToWaypointMode?: WaypointMissionGotoWaypointMode; // Same as V1 mission
  flightPathMode?: WaypointMissionV2FlightPathMode;
  finishedAction?: WaypointMissionFinishedAction; // Same as V1 mission
}
