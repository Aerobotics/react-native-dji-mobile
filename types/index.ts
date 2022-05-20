export interface DJIEvent {
  type: string;
  value: any;
}

export interface HomeLocationCoordinate3D {
  latitude?: number;
  longitude?: number;
  altitude?: number;
}

export interface LocationCoordinate3D {
  latitude: number;
  longitude: number;
  altitude: number;
}

export interface LocationCoordinate2D {
  latitude: number;
  longitude: number;
}

export interface VelocityVector {
  x: number;
  y: number;
  z: number;
}

export interface Attitude {
  roll: number;
  pitch: number;
  yaw: number;
}

export type WaypointMissionState =
  | 'NOT_SUPPORTED'
  | 'READY_TO_UPLOAD'
  | 'UPLOADING'
  | 'READY_TO_EXECUTE'
  | 'EXECUTING'
  | 'EXECUTION_PAUSED'
  | 'DISCONNECTED'
  | 'RECOVERING'
  | 'UNKNOWN';

export type WaypointMissionExecuteState =
  | 'INITIALIZING'
  | 'MOVING'
  | 'CURVE_MODE_MOVING'
  | 'CURVE_MODE_TURNING'
  | 'BEGIN_ACTION'
  | 'DOING_ACTION'
  | 'FINISHED_ACTION'
  | 'RETURN_TO_FIRST_WAYPOINT'
  | 'PAUSED'
  | 'UNKNOWN'; // this will never be returned by DJI, but is used as the initial value

export interface WaypointMissionExecutionProgressEvent {
  targetWaypointIndex: number;
  isWaypointReached: boolean;
  executeState: WaypointMissionExecuteState;
  totalWaypointCount: number;
}

export interface WaypointMissionUploadEvent {
  uploadedWaypointIndex: number;
  totalWaypointCount: number;
  isSummaryUploaded: boolean;
}

export const FlightLogListenerEventNames = Object.freeze({
  create: 'create',
  modify: 'modify',
});

export interface FlightLogListenerEvent {
  value: {
    eventName: typeof FlightLogListenerEventNames,
    fileName: string,
  };
  type: string;
}

export interface MediaFileData {
  fileName: string;
  fileSizeInBytes: number;
  dateCreated: string;
};

export interface DJIDiagnostic {
  type: string;
  reason: string;
  solution: string;
  error: string;
}

export type SensorState =
  | "UNKNOWN"
  | "DISCONNECTED"
  | "CALIBRATING"
  | "CALIBRATION_FAILED"
  | "DATA_EXCEPTION"
  | "WARMING_UP"
  | "IN_MOTION"
  | "NORMAL_BIAS"
  | "MEDIUM_BIAS"
  | "LARGE_BIAS"

export interface IMUState {
  accelerometerState: SensorState;
  gyroscopeState: SensorState;
}

export type WhiteBalancePresets =
  | "AUTO"
  | "SUNNY"
  | "CLOUDY"
  | "WATER_SURFACE"
  | "INDOOR_INCANDESCENT"
  | "INDOOR_FLUORESCENT"
  | "CUSTOM"
  | "PRESET_NEUTRAL"
  | "UNKNOWN"

export type CameraExposureSettings = {
  aperture?: number;
  iso?: number;
  shutterSpeed?: number;
  exposureValue?: number;
};

export type RemoteControllerFlightMode =
  | "F"
  | "A"
  | "P"
  | "S"
  | "G"
  | "M"
  | "T"
  | "UNKNOWN"
