export type DJIEvent = { type: string; value: any }

export type LocationCoordinate3D = { latitude: number, longitude: number, altitude: number }
export type LocationCoordinate2D = { latitude: number, longitude: number }
export type VelocityVector = { x: number, y: number, z: number }
export type Attitude = { roll: number, pitch: number, yaw: number }
export type WaypointMissionState = 'NOT_SUPPORTED' | 'READY_TO_UPLOAD' | 'UPLOADING' | 'READY_TO_EXECUTE' | 'EXECUTING' | 'EXECUTION_PAUSED' | 'DISCONNECTED' | 'RECOVERING' | 'UNKNOWN'
export type WaypointMissionExecuteState = 'INITIALIZING' | 'MOVING' | 'CURVE_MODE_MOVING' | 'CURVE_MODE_TURNING' | 'BEGIN_ACTION' | 'DOING_ACTION' | 'FINISHED_ACTION' | 'RETURN_TO_FIRST_WAYPOINT' | 'PAUSED'
export interface WaypointMissionExecutionProgressEvent {
  targetWaypointIndex: number,
  isWaypointReached: boolean,
  executeState: WaypointMissionExecuteState,
}
export interface WaypointMissionUploadEvent {
  uploadedWaypointIndex: number,
  totalWaypointCount: number,
  isSummaryUploaded: boolean,
}
