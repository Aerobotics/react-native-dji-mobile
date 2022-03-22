export type DJIEvent = { type: string; value: any }

export type LocationCoordinate3D = { latitude: number, longitude: number, altitude: number }
export type LocationCoordinate2D = { latitude: number, longitude: number }
export type VelocityVector = { x: number, y: number, z: number }
export type Attitude = { roll: number, pitch: number, yaw: number }
