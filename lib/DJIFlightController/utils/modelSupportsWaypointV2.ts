import { ModelName } from '../../../types';

const WAYPOINT_V2_DRONE_MODELS = ['MATRICE_300_RTK'];

export const modelSupportsWaypointV2 = (modelName: ModelName) => {
  return WAYPOINT_V2_DRONE_MODELS.includes(modelName);
};
