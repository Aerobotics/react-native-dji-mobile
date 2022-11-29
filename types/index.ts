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

export type WaypointMissionV1State =
  | 'DISCONNECTED'
  | 'RECOVERING'
  | 'NOT_SUPPORTED'
  | 'READY_TO_UPLOAD'
  | 'UPLOADING'
  | 'READY_TO_EXECUTE'
  | 'EXECUTING'
  | 'EXECUTION_PAUSED'
  | 'UNKNOWN';

export type WaypointMissionV2State =
  | 'DISCONNECTED'
  | 'RECOVERING'
  | 'NOT_SUPPORTED'
  | 'READY_TO_UPLOAD'
  | 'UPLOADING'
  | 'READY_TO_EXECUTE'
  | 'EXECUTING' // For V2, this includes the progress states 'GO_HOME' & 'LANDING'
  | 'EXECUTION_PAUSED' // This has been merged with V1 on the Native side
  | 'UNKNOWN';

// TODO (Nick A): should we merge these on the wrapper side and expose a cleaner API?
export type WaypointMissionState =
  | WaypointMissionV1State
  | WaypointMissionV2State;

export type WaypointMissionV1ExecuteState =
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

export type WaypointMissionV2ExecuteState =
  | 'INITIALIZING'
  | 'GO_TO_FIRST_WAYPOINT'
  | 'MOVING'
  | 'PAUSED'
  | 'INTERRUPTED'
  | 'FINISHED'
  | 'GO_HOME'
  | 'LANDING'
  | 'RETURN_TO_FIRST_WAYPOINT'
  | 'UNKNOWN';

export type WaypointMissionExecuteState =
  | WaypointMissionV1ExecuteState
  | WaypointMissionV2ExecuteState;

export interface WaypointMissionExecutionProgressEvent {
  targetWaypointIndex: number;
  isWaypointReached: boolean;
  executeState: WaypointMissionExecuteState;
  totalWaypointCount?: number; // Waypoint Mission V2 doesn't return this out-the-box
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
    eventName: typeof FlightLogListenerEventNames;
    fileName: string;
  };
  type: string;
}

export interface MediaFileData {
  fileName: string;
  fileSizeInBytes: number;
  dateCreated: string;
}

export interface DJIDiagnostic {
  type: string;
  reason: string;
  solution: string;
  error: string;
}

export type SensorState =
  | 'UNKNOWN'
  | 'DISCONNECTED'
  | 'CALIBRATING'
  | 'CALIBRATION_FAILED'
  | 'DATA_EXCEPTION'
  | 'WARMING_UP'
  | 'IN_MOTION'
  | 'NORMAL_BIAS'
  | 'MEDIUM_BIAS'
  | 'LARGE_BIAS';

export interface IMUState {
  accelerometerState: SensorState;
  gyroscopeState: SensorState;
}

export type WhiteBalancePresets =
  | 'AUTO'
  | 'SUNNY'
  | 'CLOUDY'
  | 'WATER_SURFACE'
  | 'INDOOR_INCANDESCENT'
  | 'INDOOR_FLUORESCENT'
  | 'CUSTOM'
  | 'PRESET_NEUTRAL'
  | 'UNKNOWN';

export type CameraExposureSettings = {
  aperture?: number;
  iso?: number;
  shutterSpeed?: number;
  exposureValue?: number;
};

export type RemoteControllerFlightMode =
  | 'F'
  | 'A'
  | 'P'
  | 'S'
  | 'G'
  | 'M'
  | 'T'
  | 'UNKNOWN';

export type AircraftFlightMode =
  | 'MANUAL'
  | 'ATTI'
  | 'ATTI_COURSE_LOCK'
  | 'ATTI_HOVER'
  | 'HOVER'
  | 'GPS_BLAKE'
  | 'GPS_ATTI'
  | 'GPS_COURSE_LOCK'
  | 'GPS_HOME_LOCK'
  | 'GPS_HOT_POINT'
  | 'ASSISTED_TAKEOFF'
  | 'AUTO_TAKEOFF'
  | 'AUTO_LANDING'
  | 'ATTI_LANDING'
  | 'GPS_WAYPOINT'
  | 'GO_HOME'
  | 'CLICK_GO'
  | 'JOYSTICK'
  | 'GPS_ATTI_WRISTBAND'
  | 'CINEMATIC'
  | 'ATTI_LIMITED'
  | 'DRAW'
  | 'GPS_FOLLOW_ME'
  | 'ACTIVE_TRACK'
  | 'TAP_FLY'
  | 'PANO'
  | 'FARMING'
  | 'FPV'
  | 'GPS_SPORT'
  | 'GPS_NOVICE'
  | 'CONFIRM_LANDING'
  | 'TERRAIN_FOLLOW'
  | 'PALM_CONTROL'
  | 'QUICK_SHOT'
  | 'TRIPOD'
  | 'TRACK_SPOTLIGHT'
  | 'MOTORS_JUST_STARTED'
  | 'DETOUR'
  | 'TIME_LAPSE'
  | 'POI2'
  | 'OMNI_MOVING'
  | 'ADSB_AVOIDING'
  | 'SMART_TRACK'
  | 'MOTOR_STOP_LANDING'
  | 'UNKNOWN';

export type ModelName =
  | 'INSPIRE_1'
  | 'INSPIRE_1_PRO'
  | 'INSPIRE_1_RAW'
  | 'INSPIRE_2'
  | 'MATRICE_100'
  | 'PHANTOM_3_ADVANCED'
  | 'PHANTOM_3_PROFESSIONAL'
  | 'PHANTOM_3_STANDARD'
  | 'Phantom_3_4K'
  | 'Phantom_3_SE'
  | 'PHANTOM_4'
  | 'PHANTOM_4_PRO'
  | 'PHANTOM_4_ADVANCED'
  | 'PHANTOM_4_PRO_V2'
  | 'PHANTOM_4_RTK'
  | 'P_4_MULTISPECTRAL'
  | 'OSMO'
  | 'OSMO_MOBILE'
  | 'OSMO_MOBILE_2'
  | 'OSMO_PRO'
  | 'OSMO_RAW'
  | 'OSMO_PLUS'
  | 'MATRICE_600'
  | 'MATRICE_200'
  | 'MATRICE_210'
  | 'MATRICE_210_RTK'
  | 'MATRICE_200_V2'
  | 'MATRICE_210_V2'
  | 'MATRICE_210_RTK_V2'
  | 'MATRICE_300_RTK'
  | 'MATRICE_600_PRO'
  | 'A3'
  | 'N3'
  | 'UNKNOWN_AIRCRAFT'
  | 'UNKNOWN_HANDHELD'
  | 'MAVIC_PRO'
  | 'Spark'
  | 'MAVIC_AIR'
  | 'MAVIC_2_ZOOM'
  | 'MAVIC_2_PRO'
  | 'MAVIC_2'
  | 'MAVIC_2_ENTERPRISE'
  | 'MAVIC_2_ENTERPRISE_DUAL'
  | 'MAVIC_MINI'
  | 'DJI_MINI_2'
  | 'DJI_MINI_SE'
  | 'MAVIC_AIR_2'
  | 'DJI_AIR_2S'
  | 'MAVIC_2_ENTERPRISE_ADVANCED'
  | 'DISCONNECT';
